package com.bazaryar.app.network

import com.bazaryar.app.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object SessionManager {
    @Volatile var accessToken: String? = null
    @Volatile var userId: String? = null
    fun isLoggedIn() = accessToken != null
    fun clear() {
        accessToken = null
        userId = null
    }
}

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

private class SupabaseHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val builder = chain.request().newBuilder()
            .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json")
        val token = SessionManager.accessToken
        builder.header("Authorization", "Bearer ${token ?: BuildConfig.SUPABASE_ANON_KEY}")
        return chain.proceed(builder.build())
    }
}

object ApiClient {

    private fun httpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(SupabaseHeaderInterceptor())
            .addInterceptor(logging)
            .build()
    }

    private val contentType = "application/json".toMediaType()

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL}/auth/v1/")
            .client(httpClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AuthApi::class.java)
    }

    val restApi: PostgrestApi by lazy {
        Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL}/rest/v1/")
            .client(httpClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PostgrestApi::class.java)
    }
}
