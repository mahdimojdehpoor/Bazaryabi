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

    // ---------- تخفیف‌ها ----------
    @GET("discounts")
    suspend fun getDiscountsByVendor(@Query("vendor_id") vendorIdFilter: String): List<Discount>

    @POST("discounts")
    suspend fun createDiscount(
        @Header("Prefer") prefer: String = "return=representation",
        @Body discount: Discount
    ): List<Discount>

    @PATCH("discounts")
    suspend fun updateDiscountActive(
        @Query("id") idFilter: String,
        @Body body: DiscountActiveUpdate
    )

    @DELETE("discounts")
    suspend fun deleteDiscount(@Query("id") idFilter: String)

    // ---------- لینک‌های فضای مجازی ----------
    @GET("vendor_social_links")
    suspend fun getSocialLinksByVendor(@Query("vendor_id") vendorIdFilter: String): List<VendorSocialLink>

    @POST("vendor_social_links")
    suspend fun createSocialLink(
        @Header("Prefer") prefer: String = "return=representation",
        @Body link: VendorSocialLink
    ): List<VendorSocialLink>

    @DELETE("vendor_social_links")
    suspend fun deleteSocialLink(@Query("id") idFilter: String)

    // ---------- تبلیغ / محتوا ----------
    @GET("vendor_posts")
    suspend fun getPostsByVendor(@Query("vendor_id") vendorIdFilter: String): List<VendorPost>

    @POST("vendor_posts")
    suspend fun createPost(
        @Header("Prefer") prefer: String = "return=representation",
        @Body post: VendorPost
    ): List<VendorPost>

    @DELETE("vendor_posts")
    suspend fun deletePost(@Query("id") idFilter: String)

    // ---------- دفترچه تلفن ----------
    @GET("vendor_contacts")
    suspend fun getContactsByVendor(@Query("vendor_id") vendorIdFilter: String): List<VendorContact>

    @POST("vendor_contacts")
    suspend fun createContact(
        @Header("Prefer") prefer: String = "return=representation",
        @Body contact: VendorContact
    ): List<VendorContact>

    @DELETE("vendor_contacts")
    suspend fun deleteContact(@Query("id") idFilter: String)

    // ---------- حسابداری ----------
    @GET("vendor_transactions")
    suspend fun getTransactionsByVendor(@Query("vendor_id") vendorIdFilter: String): List<VendorTransaction>

    @POST("vendor_transactions")
    suspend fun createTransaction(
        @Header("Prefer") prefer: String = "return=representation",
        @Body tx: VendorTransaction
    ): List<VendorTransaction>

    @DELETE("vendor_transactions")
    suspend fun deleteTransaction(@Query("id") idFilter: String)

    // ---------- دنبال‌کردن (مشتری) ----------
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
