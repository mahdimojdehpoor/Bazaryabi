package com.bazaryar.app.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazaryar.app.model.*
import com.bazaryar.app.repo.*
import kotlinx.coroutines.launch

enum class Screen {
    HOME, LOGIN, REGISTER, FORGOT_PASSWORD, PENDING_APPROVAL, SUSPENDED,
    DASHBOARD_ADMIN, DASHBOARD_MARKETER, DASHBOARD_VENDOR, DASHBOARD_CUSTOMER
}

class AppViewModel : ViewModel() {

    private val authRepo = AuthRepository()
    private val adminRepo = AdminRepository()
    private val marketerRepo = MarketerRepository()
    private val customerRepo = CustomerRepository()
    private val vendorRepo = VendorRepository()
    private val secretaryRepo = SecretaryRepository()

    var screen by mutableStateOf(Screen.HOME)
        private set
    var registerRole by mutableStateOf(Role.CUSTOMER)
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    var marketerSummaries by mutableStateOf<List<MarketerSummary>>(emptyList()); private set
    var pendingProfiles by mutableStateOf<List<Profile>>(emptyList()); private set
    var allVendors by mutableStateOf<List<Vendor>>(emptyList()); private set
    var allCustomers by mutableStateOf<List<Profile>>(emptyList()); private set

    var myVendors by mutableStateOf<List<Vendor>>(emptyList()); private set
    var myCommission by mutableStateOf(0L); private set

    var vendorDirectory by mutableStateOf<List<Vendor>>(emptyList()); private set
    var followedVendorIds by mutableStateOf<Set<String>>(emptySet()); private set

    var myDiscounts by mutableStateOf<List<Discount>>(emptyList()); private set
    var mySocialLinks by mutableStateOf<List<VendorSocialLink>>(emptyList()); private set
    var myPosts by mutableStateOf<List<VendorPost>>(emptyList()); private set
    var myContacts by mutableStateOf<List<VendorContact>>(emptyList()); private set
    var myTransactions by mutableStateOf<List<VendorTransaction>>(emptyList()); private set

    var mySecretaries by mutableStateOf<List<Profile>>(emptyList()); private set

    fun goTo(target: Screen) {
        errorMessage = null; infoMessage = null; screen = target
    }

    fun login(email: String, password: String) {
        errorMessage = null; isLoading = true
        viewModelScope.launch {
            when (val result = authRepo.login(email, password)) {
                is LoginResult.Success -> {
                    when {
                        result.accountStatus == "suspended" -> screen = Screen.SUSPENDED
                        result.approvalStatus == "pending" -> screen = Screen.PENDING_APPROVAL
                        result.approvalStatus == "rejected" -> {
                            errorMessage = "درخواست شما توسط مدیریت رد شده است"
                            authRepo.logout()
                        }
                        else -> { routeByRole(result.role); refreshCurrentDashboard() }
                    }
                }
                is LoginResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun register(firstName: String, lastName: String, email: String, password: String, noCriminalRecord: Boolean) {
        errorMessage = null; isLoading = true
        viewModelScope.launch {
            when (val result = authRepo.register(registerRole, firstName, lastName, email, password, noCriminalRecord)) {
                is SimpleResult.Success -> { infoMessage = "ثبت‌نام انجام شد. حالا وارد شوید."; screen = Screen.LOGIN }
                is SimpleResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun forgotPassword(email: String) {
        errorMessage = null; isLoading = true
        viewModelScope.launch {
            when (val result = authRepo.forgotPassword(email)) {
                is SimpleResult.Success -> { infoMessage = "لینک بازیابی رمز به ایمیلت ارسال شد."; screen = Screen.LOGIN }
                is SimpleResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun deleteAccount() {
        errorMessage = null; isLoading = true
        viewModelScope.launch {
            when (val result = authRepo.deleteAccount()) {
                is SimpleResult.Success -> screen = Screen.HOME
                is SimpleResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun logout() { authRepo.logout(); screen = Screen.HOME }

    fun refreshCurrentDashboard() {
        viewModelScope.launch {
            isLoading = true
            try {
                when (screen) {
                    Screen.DASHBOARD_ADMIN -> {
                        marketerSummaries = adminRepo.getMarketerSummaries()
                        pendingProfiles = adminRepo.getPendingApprovals()
                        allVendors = adminRepo.getAllVendors()
                        allCustomers = adminRepo.getAllCustomers()
                        loadSecretaries()
                    }
                    Screen.DASHBOARD_MARKETER -> {
                        myVendors = marketerRepo.getMyVendors()
                        myCommission = marketerRepo.getTotalCommission()
                        loadSecretaries()
                    }
                    Screen.DASHBOARD_CUSTOMER -> {
                        vendorDirectory = customerRepo.getVendorDirectory()
                        followedVendorIds = customerRepo.getMyFollowedVendorIds()
                    }
                    Screen.DASHBOARD_VENDOR -> { refreshVendorData(); loadSecretaries() }
                    else -> {}
                }
            } catch (e: Exception) { errorMessage = e.message }
            isLoading = false
        }
    }

    private suspend fun refreshVendorData() {
        myDiscounts = vendorRepo.getMyDiscounts()
        mySocialLinks = vendorRepo.getMySocialLinks()
        myPosts = vendorRepo.getMyPosts()
        myContacts = vendorRepo.getMyContacts()
        myTransactions = vendorRepo.getMyTransactions()
    }

    fun approveProfile(profile: Profile) = viewModelScope.launch { adminRepo.approve(profile); refreshCurrentDashboard() }
    fun rejectProfile(profile: Profile) = viewModelScope.launch { adminRepo.reject(profile); refreshCurrentDashboard() }
    fun toggleSuspend(profileId: String, suspend: Boolean) =
        viewModelScope.launch { adminRepo.setAccountStatus(profileId, suspend); refreshCurrentDashboard() }

    fun toggleFollow(vendorId: String) {
        viewModelScope.launch {
            if (followedVendorIds.contains(vendorId)) customerRepo.unfollow(vendorId)
            else customerRepo.follow(vendorId)
            followedVendorIds = customerRepo.getMyFollowedVendorIds()
        }
    }

    fun addDiscount(title: String, description: String?, percent: Double?, validUntil: String?) {
        viewModelScope.launch { vendorRepo.addDiscount(title, description, percent, validUntil); myDiscounts = vendorRepo.getMyDiscounts() }
    }
    fun toggleDiscountActive(id: String, active: Boolean) {
        viewModelScope.launch { vendorRepo.toggleDiscount(id, active); myDiscounts = vendorRepo.getMyDiscounts() }
    }
    fun deleteDiscount(id: String) {
        viewModelScope.launch { vendorRepo.deleteDiscount(id); myDiscounts = vendorRepo.getMyDiscounts() }
    }

    fun addSocialLink(label: String, url: String) {
        viewModelScope.launch { vendorRepo.addSocialLink(label, url); mySocialLinks = vendorRepo.getMySocialLinks() }
    }
    fun deleteSocialLink(id: String) {
        viewModelScope.launch { vendorRepo.deleteSocialLink(id); mySocialLinks = vendorRepo.getMySocialLinks() }
    }

    fun addPost(title: String, content: String?) {
        viewModelScope.launch { vendorRepo.addPost(title, content); myPosts = vendorRepo.getMyPosts() }
    }
    fun deletePost(id: String) {
        viewModelScope.launch { vendorRepo.deletePost(id); myPosts = vendorRepo.getMyPosts() }
    }

    fun addContact(fullName: String, phone: String?, socialLink: String?, notes: String?) {
        viewModelScope.launch { vendorRepo.addContact(fullName, phone, socialLink, notes); myContacts = vendorRepo.getMyContacts() }
    }
    fun deleteContact(id: String) {
        viewModelScope.launch { vendorRepo.deleteContact(id); myContacts = vendorRepo.getMyContacts() }
    }

    fun addTransaction(type: String, amount: Long, description: String?, occurredAt: String) {
        viewModelScope.launch { vendorRepo.addTransaction(type, amount, description, occurredAt); myTransactions = vendorRepo.getMyTransactions() }
    }
    fun deleteTransaction(id: String) {
        viewModelScope.launch { vendorRepo.deleteTransaction(id); myTransactions = vendorRepo.getMyTransactions() }
    }

    fun loadSecretaries() {
        viewModelScope.launch {
            try { mySecretaries = secretaryRepo.getMySecretaries() } catch (e: Exception) { errorMessage = e.message }
        }
    }

    fun addSecretary(firstName: String, lastName: String, email: String, password: String) {
        errorMessage = null; isLoading = true
        viewModelScope.launch {
            try {
                secretaryRepo.addSecretary(firstName, lastName, email, password)
                infoMessage = "منشی با موفقیت اضافه شد"
                loadSecretaries()
            } catch (e: Exception) {
                errorMessage = e.message ?: "خطا در افزودن منشی"
            }
            isLoading = false
        }
    }

    fun revokeSecretary(id: String) {
        viewModelScope.launch {
            when (val result = secretaryRepo.revokeSecretary(id)) {
                is SimpleResult.Success -> loadSecretaries()
                is SimpleResult.Error -> errorMessage = result.message
            }
        }
    }

    private fun routeByRole(role: String) {
        screen = when (role) {
            Role.ADMIN -> Screen.DASHBOARD_ADMIN
            Role.MARKETER -> Screen.DASHBOARD_MARKETER
            Role.VENDOR -> Screen.DASHBOARD_VENDOR
            else -> Screen.DASHBOARD_CUSTOMER
        }
    }
}
