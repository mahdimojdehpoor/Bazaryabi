package com.bazaryar.app.repo

import com.bazaryar.app.model.Profile
import com.bazaryar.app.model.Role
import com.bazaryar.app.model.SignUpRequest
import com.bazaryar.app.network.ApiClient
import com.bazaryar.app.network.SessionManager

class SecretaryRepository {

    private val authRepo = AuthRepository()

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

    /** حذف کامل و واقعی حساب منشی از Supabase Auth، مستقیم از داخل اپ */
    suspend fun revokeSecretary(profileId: String): SimpleResult {
        return authRepo.deleteOtherAccount(profileId)
    }
}
