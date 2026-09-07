package com.bazaryar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bazaryar.app.model.Role
import com.bazaryar.app.vm.AppViewModel
import com.bazaryar.app.vm.Screen

@Composable
fun HomeScreen(vm: AppViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("بازاریار", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Text("انتخاب کن که به کدام بخش می‌روی")
        Spacer(Modifier.height(24.dp))

        RoleButton("بازاریاب") { vm.registerRole = Role.MARKETER; vm.goTo(Screen.LOGIN) }
        Spacer(Modifier.height(12.dp))
        RoleButton("کاسب") { vm.registerRole = Role.VENDOR; vm.goTo(Screen.LOGIN) }
        Spacer(Modifier.height(12.dp))
        RoleButton("مشتری") { vm.registerRole = Role.CUSTOMER; vm.goTo(Screen.LOGIN) }

        Spacer(Modifier.height(32.dp))
        TextButton(onClick = { vm.goTo(Screen.LOGIN) }) {
            Text("ورود مدیر")
        }
    }
}

@Composable
private fun RoleButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}
