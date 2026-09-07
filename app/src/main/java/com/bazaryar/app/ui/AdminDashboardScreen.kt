package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bazaryar.app.model.Profile
import com.bazaryar.app.model.Vendor
import com.bazaryar.app.repo.MarketerSummary
import com.bazaryar.app.vm.AppViewModel

private enum class AdminTab { REQUESTS, MARKETERS, VENDORS, CUSTOMERS }

@Composable
fun AdminDashboardScreen(vm: AppViewModel) {
    var tab by remember { mutableStateOf(AdminTab.REQUESTS) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("پنل مدیریت", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { vm.logout() }) { Text("خروج") }
        }
        Spacer(Modifier.height(8.dp))

        ScrollableTabRow(selectedTabIndex = tab.ordinal) {
            Tab(tab == AdminTab.REQUESTS, { tab = AdminTab.REQUESTS }, text = { Text("درخواست‌ها (${vm.pendingProfiles.size})") })
            Tab(tab == AdminTab.MARKETERS, { tab = AdminTab.MARKETERS }, text = { Text("بازاریاب‌ها") })
            Tab(tab == AdminTab.VENDORS, { tab = AdminTab.VENDORS }, text = { Text("کاسب‌ها") })
            Tab(tab == AdminTab.CUSTOMERS, { tab = AdminTab.CUSTOMERS }, text = { Text("مشتری‌ها") })
        }
        Spacer(Modifier.height(12.dp))

        if (vm.isLoading) {
            Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }

        when (tab) {
            AdminTab.REQUESTS -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.pendingProfiles) { p -> PendingCard(p, vm) }
            }
            AdminTab.MARKETERS -> {
                val totalAdminIncome = vm.marketerSummaries.sumOf { it.totalAdminShare }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("درآمد کل مدیریت این دوره", fontWeight = FontWeight.Bold)
                        Text("${"%,d".format(totalAdminIncome)} تومان")
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vm.marketerSummaries) { s -> MarketerCard(s, vm) }
                }
            }
            AdminTab.VENDORS -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.allVendors) { v -> VendorAdminCard(v, vm) }
            }
            AdminTab.CUSTOMERS -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(vm.allCustomers) { c ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("${c.firstName ?: ""} ${c.lastName ?: ""}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingCard(profile: Profile, vm: AppViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("${profile.firstName ?: ""} ${profile.lastName ?: ""}", fontWeight = FontWeight.Bold)
            Text("نقش: ${roleFa(profile.role)}")
            Row(Modifier.padding(top = 8.dp)) {
                Button(onClick = { vm.approveProfile(profile) }) { Text("تایید") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { vm.rejectProfile(profile) }) { Text("رد") }
            }
        }
    }
}

@Composable
private fun MarketerCard(summary: MarketerSummary, vm: AppViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(summary.marketer.fullName ?: "بازاریاب", fontWeight = FontWeight.Bold)
            Text("کد معرف: ${summary.marketer.referralCode}")
            Text("تعداد کاسب‌ها: ${summary.vendors.size}")
            Text("سهم بازاریاب: ${"%,d".format(summary.totalMarketerShare)} تومان")
            Text("سهم مدیریت: ${"%,d".format(summary.totalAdminShare)} تومان")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.toggleSuspend(summary.marketer.id, true) }) { Text("قطع دسترسی") }
        }
    }
}

@Composable
private fun VendorAdminCard(vendor: Vendor, vm: AppViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(vendor.businessName.ifBlank { "کاسب" }, fontWeight = FontWeight.Bold)
            Text("وضعیت اشتراک: ${statusFa(vendor.subscriptionStatus)}")
            Text("پایان اشتراک: ${vendor.subscriptionEnd ?: "نامشخص"}")
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { vm.toggleSuspend(vendor.id, true) }) { Text("قطع دسترسی") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { vm.toggleSuspend(vendor.id, false) }) { Text("فعال‌سازی") }
            }
        }
    }
}

private fun roleFa(role: String) = when (role) { "marketer" -> "بازاریاب"; "vendor" -> "کاسب"; else -> role }
private fun statusFa(s: String) = when (s) { "active" -> "فعال"; "pending" -> "در انتظار"; "expired" -> "منقضی"; else -> s }
