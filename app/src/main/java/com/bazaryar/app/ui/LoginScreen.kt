package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bazaryar.app.vm.AppViewModel
import com.bazaryar.app.vm.Screen

@Composable
fun LoginScreen(vm: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("ورود", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(email, { email = it }, label = { Text("ایمیل") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            password, { password = it }, label = { Text("رمز عبور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        vm.infoMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(8.dp)) }
        vm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(8.dp)) }

        Button(
            onClick = { vm.login(email.trim(), password) },
            enabled = !vm.isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (vm.isLoading) "..." else "ورود") }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { vm.goTo(Screen.REGISTER) }) { Text("حساب نداری؟ ثبت‌نام کن") }
        TextButton(onClick = { vm.goTo(Screen.FORGOT_PASSWORD) }) { Text("رمز عبور را فراموش کردی؟") }
        TextButton(onClick = { vm.goTo(Screen.HOME) }) { Text("بازگشت") }
    }
}
