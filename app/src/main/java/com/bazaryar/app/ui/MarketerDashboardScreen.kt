package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bazaryar.app.model.Vendor
import com.bazaryar.app.vm.AppViewModel

@Composable
fun MarketerDashboardScreen(vm: AppViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("کاسب‌های من", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { vm.logout() }) { Text("خروج") }
        }
        Spacer(Modifier.height(12.dp))

        if (vm.isLoading && vm.myVendors.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("پورسانت این دوره", fontWeight = FontWeight.Bold)
                Text("${"%,d".format(vm.myCommission)} تومان")
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.myVendors) { v -> VendorCard(v) }
        }
    }
}

@Composable
private fun VendorCard(vendor: Vendor) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(vendor.businessName.ifBlank { "کاسب" }, fontWeight = FontWeight.Bold)
            Text("وضعیت: ${statusFa(vendor.subscriptionStatus)}")
            Text("پورسانت شما: ${"%,d".format(vendor.marketerCommission)} تومان")
        }
    }
}

private fun statusFa(s: String) = when (s) { "active" -> "فعال"; "pending" -> "در انتظار"; "expired" -> "منقضی"; else -> s }
