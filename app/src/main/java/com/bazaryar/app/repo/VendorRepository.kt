package com.bazaryar.app.repo

import com.bazaryar.app.model.*
import com.bazaryar.app.network.ApiClient
import com.bazaryar.app.network.SessionManager

class VendorRepository {

    private fun myId() = SessionManager.userId ?: ""

    // تخفیف‌ها
    suspend fun getMyDiscounts(): List<Discount> =
        ApiClient.restApi.getDiscountsByVendor(vendorIdFilter = "eq.${myId()}")

    suspend fun addDiscount(title: String, description: String?, percent: Double?, validUntil: String?) {
        ApiClient.restApi.createDiscount(
            discount = Discount(vendorId = myId(), title = title, description = description, percent = percent, validUntil = validUntil)
        )
    }

    suspend fun toggleDiscount(id: String, active: Boolean) {
        ApiClient.restApi.updateDiscountActive(idFilter = "eq.$id", body = DiscountActiveUpdate(active))
    }

    suspend fun deleteDiscount(id: String) {
        ApiClient.restApi.deleteDiscount(idFilter = "eq.$id")
    }

    // لینک‌های فضای مجازی
    suspend fun getMySocialLinks(): List<VendorSocialLink> =
        ApiClient.restApi.getSocialLinksByVendor(vendorIdFilter = "eq.${myId()}")

    suspend fun addSocialLink(label: String, url: String) {
        ApiClient.restApi.createSocialLink(link = VendorSocialLink(vendorId = myId(), platformLabel = label, url = url))
    }

    suspend fun deleteSocialLink(id: String) {
        ApiClient.restApi.deleteSocialLink(idFilter = "eq.$id")
    }

    // تبلیغ / محتوا
    suspend fun getMyPosts(): List<VendorPost> =
        ApiClient.restApi.getPostsByVendor(vendorIdFilter = "eq.${myId()}")

    suspend fun addPost(title: String, content: String?) {
        ApiClient.restApi.createPost(post = VendorPost(vendorId = myId(), title = title, content = content))
    }

    suspend fun deletePost(id: String) {
        ApiClient.restApi.deletePost(idFilter = "eq.$id")
    }

    // دفترچه تلفن
    suspend fun getMyContacts(): List<VendorContact> =
        ApiClient.restApi.getContactsByVendor(vendorIdFilter = "eq.${myId()}")

    suspend fun addContact(fullName: String, phone: String?, socialLink: String?, notes: String?) {
        ApiClient.restApi.createContact(
            contact = VendorContact(vendorId = myId(), fullName = fullName, phone = phone, socialLink = socialLink, notes = notes)
        )
    }

    suspend fun deleteContact(id: String) {
        ApiClient.restApi.deleteContact(idFilter = "eq.$id")
    }

    // حسابداری
    suspend fun getMyTransactions(): List<VendorTransaction> =
        ApiClient.restApi.getTransactionsByVendor(vendorIdFilter = "eq.${myId()}")

    suspend fun addTransaction(type: String, amount: Long, description: String?, occurredAt: String) {
        ApiClient.restApi.createTransaction(
            tx = VendorTransaction(vendorId = myId(), type = type, amount = amount, description = description, occurredAt = occurredAt)
        )
    }

    suspend fun deleteTransaction(id: String) {
        ApiClient.restApi.deleteTransaction(idFilter = "eq.$id")
    }
}
