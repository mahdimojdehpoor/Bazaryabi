package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bazaryar.app.vm.AppViewModel

// تخفیف/دفترچه‌تلفن/پیامک/حسابداری در فاز بعد اضافه می‌شود
@Composable
fun VendorDashboardScreen(vm: AppViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("پنل کاسب", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { vm.logout() }) { Text("خروج") }
        }
        Spacer(Modifier.height(20.dp))
        Text("خوش آمدید! امکانات تخفیف، دفترچه تلفن، پیامک و حسابداری در فاز بعد اضافه می‌شود.")
    }
}
