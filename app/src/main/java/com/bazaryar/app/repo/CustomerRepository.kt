package com.bazaryar.app.repo

import com.bazaryar.app.model.Discount
import com.bazaryar.app.model.Follow
import com.bazaryar.app.model.Vendor
import com.bazaryar.app.model.VendorSocialLink
import com.bazaryar.app.network.ApiClient
import com.bazaryar.app.network.SessionManager

class CustomerRepository {

    suspend fun getVendorDirectory(): List<Vendor> = ApiClient.restApi.getActiveVendorsDirectory()

    suspend fun getMyFollowedVendorIds(): Set<String> {
        val myId = SessionManager.userId ?: return emptySet()
        return ApiClient.restApi.getMyFollows(customerIdFilter = "eq.$myId").map { it.vendorId }.toSet()
    }

    suspend fun follow(vendorId: String) {
        val myId = SessionManager.userId ?: return
        ApiClient.restApi.follow(Follow(customerId = myId, vendorId = vendorId))
    }

    suspend fun unfollow(vendorId: String) {
        val myId = SessionManager.userId ?: return
        ApiClient.restApi.unfollow(customerIdFilter = "eq.$myId", vendorIdFilter = "eq.$vendorId")
    }

    suspend fun getVendorDiscounts(vendorId: String): List<Discount> =
        ApiClient.restApi.getDiscountsByVendor(vendorIdFilter = "eq.$vendorId")

    suspend fun getVendorSocialLinks(vendorId: String): List<VendorSocialLink> =
        ApiClient.restApi.getSocialLinksByVendor(vendorIdFilter = "eq.$vendorId")
}
