package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bazaryar.app.model.Vendor
import com.bazaryar.app.vm.AppViewModel

@Composable
fun MarketerDashboardScreen(vm: AppViewModel) {
    var showSecretaries by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("کاسب‌های من", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { vm.logout() }) { Text("خروج") }
        }
        Row {
            TextButton(onClick = { showSecretaries = !showSecretaries }) {
                Text(if (showSecretaries) "بستن مدیریت منشی‌ها" else "مدیریت منشی‌ها")
            }
            DeleteAccountButton(vm)
        }
        Spacer(Modifier.height(12.dp))

        if (showSecretaries) {
            SecretaryManagementSection(vm)
            Spacer(Modifier.height(20.dp))
        }

        if (vm.isLoading && vm.myVendors.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("پورسانت این دوره", fontWeight = FontWeight.Bold)
                Text("${"%,d".format(vm.myCommission)} تومان")
            }
        }
        Spacer(Modifier.height(16.dp))

        vm.myVendors.forEach { v -> VendorCard(v) }
    }
}

@Composable
private fun VendorCard(vendor: Vendor) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(vendor.businessName.ifBlank { "کاسب" }, fontWeight = FontWeight.Bold)
            Text("وضعیت: ${statusFa(vendor.subscriptionStatus)}")
            Text("پورسانت شما: ${"%,d".format(vendor.marketerCommission)} تومان")
        }
    }
}

private fun statusFa(s: String) = when (s) { "active" -> "فعال"; "pending" -> "در انتظار"; "expired" -> "منقضی"; else -> s }
