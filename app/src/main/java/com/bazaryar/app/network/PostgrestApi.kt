package com.bazaryar.app.network

import com.bazaryar.app.model.*
import retrofit2.http.*

interface PostgrestApi {

    @GET("profiles")
    suspend fun getMyProfile(@Query("id") idFilter: String): List<Profile>

    @GET("profiles")
    suspend fun getPendingProfiles(@Query("approval_status") filter: String = "eq.pending"): List<Profile>

    @GET("profiles")
    suspend fun getProfilesByRole(@Query("role") roleFilter: String): List<Profile>

    @PATCH("profiles")
    suspend fun updateProfileStatus(
        @Query("id") idFilter: String,
        @Body body: ApprovalUpdate
    )

    @GET("marketers")
    suspend fun getMarketers(@Query("select") select: String = "*"): List<Marketer>

    @POST("marketers")
    suspend fun createMarketer(
        @Header("Prefer") prefer: String = "return=representation",
        @Body marketer: Marketer
    ): List<Marketer>

    @GET("vendors")
    suspend fun getAllVendors(@Query("select") select: String = "*"): List<Vendor>

    @GET("vendors")
    suspend fun getVendorsByMarketer(@Query("marketer_id") marketerIdFilter: String): List<Vendor>

    @GET("vendors")
    suspend fun getActiveVendorsDirectory(
        @Query("subscription_status") filter: String = "eq.active"
    ): List<Vendor>

    @POST("vendors")
    suspend fun createVendor(
        @Header("Prefer") prefer: String = "return=representation",
        @Body vendor: Vendor
    ): List<Vendor>

    @GET("discounts")
    suspend fun getDiscountsByVendor(@Query("vendor_id") vendorIdFilter: String): List<Discount>

    @GET("vendor_social_links")
    suspend fun getSocialLinksByVendor(@Query("vendor_id") vendorIdFilter: String): List<VendorSocialLink>

    @GET("customer_vendor_follows")
    suspend fun getMyFollows(@Query("customer_id") customerIdFilter: String): List<Follow>

    @POST("customer_vendor_follows")
    suspend fun follow(@Body body: Follow)

    @DELETE("customer_vendor_follows")
    suspend fun unfollow(
        @Query("customer_id") customerIdFilter: String,
        @Query("vendor_id") vendorIdFilter: String
    )
}
