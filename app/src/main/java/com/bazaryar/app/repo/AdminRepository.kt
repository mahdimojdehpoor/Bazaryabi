package com.bazaryar.app.repo

import com.bazaryar.app.model.*
import com.bazaryar.app.network.ApiClient

data class MarketerSummary(
    val marketer: Marketer,
    val vendors: List<Vendor>,
    val totalMonthlyIncome: Long,
    val totalMarketerShare: Long,
    val totalAdminShare: Long
)

class AdminRepository {

    suspend fun getMarketerSummaries(): List<MarketerSummary> {
        val marketers = ApiClient.restApi.getMarketers()
        val vendors = ApiClient.restApi.getAllVendors()
        return marketers.map { m ->
            val theirs = vendors.filter { it.marketerId == m.id }
            MarketerSummary(
                marketer = m, vendors = theirs,
                totalMonthlyIncome = theirs.sumOf { it.planPrice },
                totalMarketerShare = theirs.sumOf { it.marketerCommission },
                totalAdminShare = theirs.sumOf { it.planPrice - it.marketerCommission }
            )
        }
    }

    suspend fun getPendingApprovals(): List<Profile> = ApiClient.restApi.getPendingProfiles()
    suspend fun getAllVendors(): List<Vendor> = ApiClient.restApi.getAllVendors()
    suspend fun getAllCustomers(): List<Profile> = ApiClient.restApi.getProfilesByRole("eq.customer")

    suspend fun approve(profile: Profile) {
        when (profile.role) {
            Role.MARKETER -> ApiClient.restApi.createMarketer(
                marketer = Marketer(id = profile.id, referralCode = generateReferralCode(), fullName = profile.fullName)
            )
            Role.VENDOR -> ApiClient.restApi.createVendor(
                vendor = Vendor(id = profile.id, businessName = profile.fullName ?: "کسب‌وکار جدید")
            )
        }
        ApiClient.restApi.updateProfileStatus(idFilter = "eq.${profile.id}", body = ApprovalUpdate(approvalStatus = "approved"))
    }

    suspend fun reject(profile: Profile) {
        ApiClient.restApi.updateProfileStatus(idFilter = "eq.${profile.id}", body = ApprovalUpdate(approvalStatus = "rejected"))
    }

    /** یادآوری/قطع دسترسی: تغییر وضعیت حساب بازاریاب یا کاسب */
    suspend fun setAccountStatus(profileId: String, suspended: Boolean) {
        ApiClient.restApi.updateProfileStatus(
            idFilter = "eq.$profileId",
            body = ApprovalUpdate(accountStatus = if (suspended) "suspended" else "active")
        )
    }

    private fun generateReferralCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return "MKT" + (1..5).map { chars.random() }.joinToString("")
    }
}
