package com.bazaryar.app.repo

import com.bazaryar.app.model.Profile
import com.bazaryar.app.model.Role
import com.bazaryar.app.model.SignUpRequest
import com.bazaryar.app.network.ApiClient
import com.bazaryar.app.network.SessionManager

class SecretaryRepository {

    suspend fun getMySecretaries(): List<Profile> {
        val myId = SessionManager.userId ?: return emptyList()
        return ApiClient.restApi.getSecretariesByOwner(ownerIdFilter = "eq.$myId")
    }

    suspend fun addSecretary(firstName: String, lastName: String, email: String, password: String) {
        val myId = SessionManager.userId ?: return
        val meta = mapOf(
            "role" to Role.SECRETARY,
            "first_name" to firstName,
            "last_name" to lastName,
            "owner_id" to myId
        )
        ApiClient.authApi.signUp(SignUpRequest(email, password, meta))
    }

    /** حذف واقعی حساب نیاز به دسترسی سرور دارد؛ به‌جای آن دسترسی منشی قطع می‌شود */
    suspend fun revokeSecretary(profileId: String) {
        ApiClient.restApi.updateProfileStatus(
            idFilter = "eq.$profileId",
            body = com.bazaryar.app.model.ApprovalUpdate(accountStatus = "suspended")
        )
    }
}
