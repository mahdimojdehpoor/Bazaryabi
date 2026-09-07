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

    var screen by mutableStateOf(Screen.HOME)
        private set
    var registerRole by mutableStateOf(Role.CUSTOMER)
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    // ادمین
    var marketerSummaries by mutableStateOf<List<MarketerSummary>>(emptyList()); private set
    var pendingProfiles by mutableStateOf<List<Profile>>(emptyList()); private set
    var allVendors by mutableStateOf<List<Vendor>>(emptyList()); private set
    var allCustomers by mutableStateOf<List<Profile>>(emptyList()); private set

    // بازاریاب
    var myVendors by mutableStateOf<List<Vendor>>(emptyList()); private set
    var myCommission by mutableStateOf(0L); private set

    // مشتری
    var vendorDirectory by mutableStateOf<List<Vendor>>(emptyList()); private set
    var followedVendorIds by mutableStateOf<Set<String>>(emptySet()); private set

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
                    }
                    Screen.DASHBOARD_MARKETER -> {
                        myVendors = marketerRepo.getMyVendors()
                        myCommission = marketerRepo.getTotalCommission()
                    }
                    Screen.DASHBOARD_CUSTOMER -> {
                        vendorDirectory = customerRepo.getVendorDirectory()
                        followedVendorIds = customerRepo.getMyFollowedVendorIds()
                    }
                    else -> {}
                }
            } catch (e: Exception) { errorMessage = e.message }
            isLoading = false
        }
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

    private fun routeByRole(role: String) {
        screen = when (role) {
            Role.ADMIN -> Screen.DASHBOARD_ADMIN
            Role.MARKETER -> Screen.DASHBOARD_MARKETER
            Role.VENDOR -> Screen.DASHBOARD_VENDOR
            else -> Screen.DASHBOARD_CUSTOMER
        }
    }
}
