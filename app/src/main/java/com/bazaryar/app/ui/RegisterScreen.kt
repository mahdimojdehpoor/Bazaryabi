package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bazaryar.app.model.Role
import com.bazaryar.app.vm.AppViewModel
import com.bazaryar.app.vm.Screen

@Composable
fun RegisterScreen(vm: AppViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var noCriminalRecord by remember { mutableStateOf(false) }

    val needsCriminalCheck = vm.registerRole != Role.CUSTOMER

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("ثبت‌نام - ${roleLabel(vm.registerRole)}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(firstName, { firstName = it }, label = { Text("نام") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(lastName, { lastName = it }, label = { Text("نام خانوادگی") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(email, { email = it }, label = { Text("ایمیل") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            password, { password = it }, label = { Text("رمز عبور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (needsCriminalCheck) {
            Spacer(Modifier.height(10.dp))
            Row {
                Checkbox(checked = noCriminalRecord, onCheckedChange = { noCriminalRecord = it })
                Text("اعلام می‌کنم سوءپیشینه کیفری ندارم", modifier = Modifier.padding(top = 12.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        vm.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(8.dp)) }

        val canSubmit = firstName.isNotBlank() && lastName.isNotBlank() &&
                email.isNotBlank() && password.length >= 6 &&
                (!needsCriminalCheck || noCriminalRecord)

        Button(
            onClick = { vm.register(firstName.trim(), lastName.trim(), email.trim(), password, noCriminalRecord) },
            enabled = !vm.isLoading && canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (vm.isLoading) "..." else "ثبت‌نام") }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { vm.goTo(Screen.LOGIN) }) { Text("قبلاً ثبت‌نام کردی؟ وارد شو") }
        TextButton(onClick = { vm.goTo(Screen.HOME) }) { Text("بازگشت") }
    }
}

private fun roleLabel(role: String) = when (role) {
    Role.MARKETER -> "بازاریاب"
    Role.VENDOR -> "کاسب"
    else -> "مشتری"
}
