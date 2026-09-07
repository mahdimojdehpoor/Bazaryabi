package com.bazaryar.app.network

import com.bazaryar.app.model.AuthResponse
import com.bazaryar.app.model.LoginRequest
import com.bazaryar.app.model.RecoverRequest
import com.bazaryar.app.model.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest
    ): AuthResponse

    @POST("signup")
    suspend fun signUp(@Body body: SignUpRequest)

    @POST("recover")
    suspend fun recoverPassword(@Body body: RecoverRequest)
}
