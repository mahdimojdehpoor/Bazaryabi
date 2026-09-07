package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bazaryar.app.vm.AppViewModel
import com.bazaryar.app.vm.Screen

@Composable
fun ForgotPasswordScreen(vm: AppViewModel) {
    var email by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("فراموشی رمز عبور", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("ایمیل حساب خودت رو وارد کن، لینک بازیابی رمز براش ارسال می‌شود.")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it }, label = { Text("ایمیل") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        vm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(8.dp)) }
        Button(
            onClick = { vm.forgotPassword(email.trim()) },
            enabled = !vm.isLoading && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (vm.isLoading) "..." else "ارسال لینک بازیابی") }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { vm.goTo(Screen.LOGIN) }) { Text("بازگشت به ورود") }
    }
}
