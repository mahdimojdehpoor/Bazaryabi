package com.bazaryar.app.repo

import com.bazaryar.app.model.Vendor
import com.bazaryar.app.network.ApiClient
import com.bazaryar.app.network.SessionManager

class MarketerRepository {
    suspend fun getMyVendors(): List<Vendor> {
        val ownerId = SessionManager.effectiveOwnerId ?: return emptyList()
        return ApiClient.restApi.getVendorsByMarketer(marketerIdFilter = "eq.$ownerId")
    }

    suspend fun getTotalCommission(): Long =
        getMyVendors().filter { it.subscriptionStatus == "active" }.sumOf { it.marketerCommission }
}
