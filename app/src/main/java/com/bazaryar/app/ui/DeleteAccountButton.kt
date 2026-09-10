package com.bazaryar.app.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.bazaryar.app.vm.AppViewModel

@Composable
fun DeleteAccountButton(vm: AppViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    TextButton(onClick = { showDialog = true }) {
        Text("حذف حساب", color = MaterialTheme.colorScheme.error)
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("حذف حساب") },
            text = { Text("مطمئنی می‌خوای حساب و همه اطلاعاتش برای همیشه حذف بشه؟ این کار قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false; vm.deleteAccount() }) {
                    Text("بله، حذف کن", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("انصراف") }
            }
        )
    }
}
