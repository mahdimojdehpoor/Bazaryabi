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
fun CustomerDashboardScreen(vm: AppViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("لیست کاسب‌ها", style = MaterialTheme.typography.headlineSmall)
            Row {
                DeleteAccountButton(vm)
                TextButton(onClick = { vm.logout() }) { Text("خروج") }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (vm.isLoading && vm.vendorDirectory.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.vendorDirectory) { v -> VendorPublicCard(v, vm) }
        }
    }
}

@Composable
private fun VendorPublicCard(vendor: Vendor, vm: AppViewModel) {
    val isFollowed = vm.followedVendorIds.contains(vendor.id)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(vendor.businessName.ifBlank { "کاسب" }, fontWeight = FontWeight.Bold)
            vendor.address?.let { Text("آدرس: $it") }
            vendor.description?.let { Text(it) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.toggleFollow(vendor.id) }) {
                Text(if (isFollowed) "دنبال می‌کنی ✓" else "دنبال کردن")
            }
        }
    }
}
