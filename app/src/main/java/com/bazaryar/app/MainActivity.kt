package com.bazaryar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bazaryar.app.ui.*
import com.bazaryar.app.vm.AppViewModel
import com.bazaryar.app.vm.Screen

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (vm.screen) {
                        Screen.HOME -> HomeScreen(vm)
                        Screen.LOGIN -> LoginScreen(vm)
                        Screen.REGISTER -> RegisterScreen(vm)
                        Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(vm)
                        Screen.PENDING_APPROVAL -> PendingApprovalScreen(vm)
                        Screen.SUSPENDED -> SuspendedScreen(vm)
                        Screen.DASHBOARD_ADMIN -> AdminDashboardScreen(vm)
                        Screen.DASHBOARD_MARKETER -> MarketerDashboardScreen(vm)
                        Screen.DASHBOARD_VENDOR -> VendorDashboardScreen(vm)
                        Screen.DASHBOARD_CUSTOMER -> CustomerDashboardScreen(vm)
                    }
                }
            }
        }
    }
}
