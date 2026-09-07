package com.bazaryar.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object Role {
    const val ADMIN = "admin"
    const val MARKETER = "marketer"
    const val VENDOR = "vendor"
    const val CUSTOMER = "customer"
}

@Serializable
data class Profile(
    val id: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val role: String,
    @SerialName("no_criminal_record") val noCriminalRecord: Boolean = false,
    @SerialName("approval_status") val approvalStatus: String = "approved",
    @SerialName("account_status") val accountStatus: String = "active"
)

@Serializable
data class Marketer(
    val id: String,
    @SerialName("referral_code") val referralCode: String,
    @SerialName("commission_percent") val commissionPercent: Double = 40.0,
    @SerialName("full_name") val fullName: String? = null
)

@Serializable
data class Vendor(
    val id: String,
    @SerialName("business_name") val businessName: String = "",
    val address: String? = null,
    val description: String? = null,
    @SerialName("marketer_id") val marketerId: String? = null,
    @SerialName("plan_price") val planPrice: Long = 500000,
    @SerialName("marketer_commission") val marketerCommission: Long = 200000,
    @SerialName("subscription_status") val subscriptionStatus: String = "pending",
    @SerialName("subscription_end") val subscriptionEnd: String? = null
)

@Serializable
data class Discount(
    val id: String? = null,
    @SerialName("vendor_id") val vendorId: String,
    val title: String,
    val description: String? = null,
    val percent: Double? = null,
    @SerialName("valid_until") val validUntil: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class DiscountActiveUpdate(@SerialName("is_active") val isActive: Boolean)

@Serializable
data class VendorSocialLink(
    val id: String? = null,
    @SerialName("vendor_id") val vendorId: String,
    @SerialName("platform_label") val platformLabel: String,
    val url: String
)

@Serializable
data class VendorPost(
    val id: String? = null,
    @SerialName("vendor_id") val vendorId: String,
    val title: String,
    val content: String? = null
)

@Serializable
data class VendorContact(
    val id: String? = null,
    @SerialName("vendor_id") val vendorId: String,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    @SerialName("social_link") val socialLink: String? = null,
    val notes: String? = null
)

@Serializable
data class VendorTransaction(
    val id: String? = null,
    @SerialName("vendor_id") val vendorId: String,
    val type: String, // income یا expense
    val amount: Long,
    val description: String? = null,
    @SerialName("occurred_at") val occurredAt: String? = null
)

@Serializable
data class Follow(
    @SerialName("customer_id") val customerId: String,
    @SerialName("vendor_id") val vendorId: String
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class SignUpRequest(val email: String, val password: String, val data: Map<String, String>)

@Serializable
data class RecoverRequest(val email: String)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    val user: SupabaseUser
)

@Serializable
data class SupabaseUser(val id: String, val email: String? = null)

@Serializable
data class ApprovalUpdate(
    @SerialName("approval_status") val approvalStatus: String? = null,
    @SerialName("account_status") val accountStatus: String? = null
)
