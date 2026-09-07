package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bazaryar.app.vm.AppViewModel

@Composable
fun SuspendedScreen(vm: AppViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(
            "دسترسی حساب شما توسط مدیریت قطع شده است.\nبرای اطلاع از دلیل، با پشتیبانی تماس بگیرید.",
            textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = { vm.logout() }) { Text("بازگشت") }
    }
}
