package com.bazaryar.app.repo

import com.bazaryar.app.model.LoginRequest
import com.bazaryar.app.model.RecoverRequest
import com.bazaryar.app.model.SignUpRequest
import com.bazaryar.app.network.ApiClient
import com.bazaryar.app.network.SessionManager

sealed class LoginResult {
    data class Success(val role: String, val approvalStatus: String, val accountStatus: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class SimpleResult {
    object Success : SimpleResult()
    data class Error(val message: String) : SimpleResult()
}

class AuthRepository {

    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val auth = ApiClient.authApi.login(body = LoginRequest(email, password))
            SessionManager.accessToken = auth.accessToken
            SessionManager.userId = auth.user.id

            val profile = ApiClient.restApi.getMyProfile(idFilter = "eq.${auth.user.id}").firstOrNull()
            if (profile == null) {
                SessionManager.clear()
                return LoginResult.Error("پروفایل کاربر پیدا نشد")
            }
            LoginResult.Success(profile.role, profile.approvalStatus, profile.accountStatus)
        } catch (e: retrofit2.HttpException) {
            val body = try { e.response()?.errorBody()?.string() } catch (ex: Exception) { null }
            LoginResult.Error(
                when {
                    body?.contains("Email not confirmed", ignoreCase = true) == true ->
                        "ایمیل تایید نشده است. از مدیر بخواه گزینه Confirm email را در Supabase خاموش کند."
                    body?.contains("Invalid login credentials", ignoreCase = true) == true ->
                        "ایمیل یا رمز عبور اشتباه است"
                    else -> "خطای ورود: ${body ?: e.message()}"
                }
            )
        } catch (e: Exception) {
            LoginResult.Error("خطای شبکه: ${e.message ?: "اتصال برقرار نشد (احتمالاً فیلترینگ)"}")
        }
    }

    suspend fun register(
        role: String, firstName: String, lastName: String,
        email: String, password: String, noCriminalRecord: Boolean
    ): SimpleResult {
        return try {
            val meta = mapOf(
                "role" to role,
                "first_name" to firstName,
                "last_name" to lastName,
                "no_criminal_record" to noCriminalRecord.toString()
            )
            ApiClient.authApi.signUp(SignUpRequest(email, password, meta))
            SimpleResult.Success
        } catch (e: Exception) {
            SimpleResult.Error(e.message ?: "خطا در ثبت‌نام")
        }
    }

    suspend fun forgotPassword(email: String): SimpleResult {
        return try {
            ApiClient.authApi.recoverPassword(RecoverRequest(email))
            SimpleResult.Success
        } catch (e: Exception) {
            SimpleResult.Error(e.message ?: "خطا در ارسال ایمیل بازیابی")
        }
    }

    fun logout() = SessionManager.clear()
}
