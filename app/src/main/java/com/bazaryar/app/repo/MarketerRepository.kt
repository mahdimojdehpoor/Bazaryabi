package com.bazaryar.app.repo

import com.bazaryar.app.model.Vendor
import com.bazaryar.app.network.ApiClient
import com.bazaryar.app.network.SessionManager

class MarketerRepository {
    suspend fun getMyVendors(): List<Vendor> {
        val marketerId = SessionManager.userId ?: return emptyList()
        return ApiClient.restApi.getVendorsByMarketer(marketerIdFilter = "eq.$marketerId")
    }

    suspend fun getTotalCommission(): Long =
        getMyVendors().filter { it.subscriptionStatus == "active" }.sumOf { it.marketerCommission }
}
