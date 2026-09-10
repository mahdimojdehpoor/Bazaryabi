package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bazaryar.app.model.Profile
import com.bazaryar.app.vm.AppViewModel

@Composable
fun SecretaryManagementSection(vm: AppViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadSecretaries() }

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("منشی‌های من", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("منشی می‌تواند با ایمیل و رمز خودش وارد شود و همان اطلاعات شما را ببیند.")
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("افزودن منشی جدید", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(firstName, { firstName = it }, label = { Text("نام") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(lastName, { lastName = it }, label = { Text("نام خانوادگی") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(email, { email = it }, label = { Text("ایمیل منشی") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    password, { password = it }, label = { Text("رمز عبور منشی") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        vm.addSecretary(firstName.trim(), lastName.trim(), email.trim(), password)
                        firstName = ""; lastName = ""; email = ""; password = ""
                    },
                    enabled = !vm.isLoading && firstName.isNotBlank() && email.isNotBlank() && password.length >= 6
                ) { Text("افزودن منشی") }
            }
        }

        Spacer(Modifier.height(12.dp))
        vm.mySecretaries.forEach { s -> SecretaryRow(s, vm) }
    }
}

@Composable
private fun SecretaryRow(profile: Profile, vm: AppViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("${profile.firstName ?: ""} ${profile.lastName ?: ""}", fontWeight = FontWeight.Bold)
                Text(if (profile.accountStatus == "active") "فعال" else "دسترسی قطع شده")
            }
            if (profile.accountStatus == "active") {
                TextButton(onClick = { vm.revokeSecretary(profile.id) }) {
                    Text("حذف دسترسی", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
