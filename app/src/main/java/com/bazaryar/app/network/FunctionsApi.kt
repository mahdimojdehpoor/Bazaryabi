package com.bazaryar.app.network

import com.bazaryar.app.model.DeleteAccountRequest
import com.bazaryar.app.model.SimpleResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FunctionsApi {
    @POST("delete-account")
    suspend fun deleteAccount(@Body body: DeleteAccountRequest = DeleteAccountRequest()): SimpleResponse
}
