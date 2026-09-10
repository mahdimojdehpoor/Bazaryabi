package com.bazaryar.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bazaryar.app.model.Discount
import com.bazaryar.app.model.VendorContact
import com.bazaryar.app.model.VendorPost
import com.bazaryar.app.model.VendorSocialLink
import com.bazaryar.app.model.VendorTransaction
import com.bazaryar.app.util.SmsSender
import com.bazaryar.app.vm.AppViewModel

private enum class VendorTab { DISCOUNTS, CONTACTS, SOCIAL, POSTS, ACCOUNTING, SMS, SECRETARIES }

@Composable
fun VendorDashboardScreen(vm: AppViewModel) {
    var tab by remember { mutableStateOf(VendorTab.DISCOUNTS) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("پنل کاسب", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { vm.logout() }) { Text("خروج") }
        }
        Spacer(Modifier.height(8.dp))

        ScrollableTabRow(selectedTabIndex = tab.ordinal) {
            Tab(tab == VendorTab.DISCOUNTS, { tab = VendorTab.DISCOUNTS }, text = { Text("تخفیف‌ها") })
            Tab(tab == VendorTab.CONTACTS, { tab = VendorTab.CONTACTS }, text = { Text("دفترچه تلفن") })
            Tab(tab == VendorTab.SOCIAL, { tab = VendorTab.SOCIAL }, text = { Text("فضای مجازی") })
            Tab(tab == VendorTab.POSTS, { tab = VendorTab.POSTS }, text = { Text("تبلیغ") })
            Tab(tab == VendorTab.ACCOUNTING, { tab = VendorTab.ACCOUNTING }, text = { Text("حسابداری") })
            Tab(tab == VendorTab.SMS, { tab = VendorTab.SMS }, text = { Text("پیامک") })
        }
        Spacer(Modifier.height(12.dp))

        if (vm.isLoading) {
            Box(Modifier.fillMaxWidth().padding(12.dp)) { CircularProgressIndicator() }
        }

        when (tab) {
            VendorTab.DISCOUNTS -> DiscountsTab(vm)
            VendorTab.CONTACTS -> ContactsTab(vm)
            VendorTab.SOCIAL -> SocialLinksTab(vm)
            VendorTab.POSTS -> PostsTab(vm)
            VendorTab.ACCOUNTING -> AccountingTab(vm)
            VendorTab.SMS -> SmsTab(vm)
        }
    }
}

// ---------------- تخفیف‌ها ----------------
@Composable
private fun DiscountsTab(vm: AppViewModel) {
    var title by remember { mutableStateOf("") }
    var percent by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("تخفیف جدید", fontWeight = FontWeight.Bold)
                OutlinedTextField(title, { title = it }, label = { Text("عنوان تخفیف") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(percent, { percent = it }, label = { Text("درصد تخفیف") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        vm.addDiscount(title, null, percent.toDoubleOrNull(), null)
                        title = ""; percent = ""
                    },
                    enabled = title.isNotBlank()
                ) { Text("افزودن") }
            }
        }
        Spacer(Modifier.height(12.dp))
        vm.myDiscounts.forEach { d -> DiscountRow(d, vm) }
    }
}

@Composable
private fun DiscountRow(d: Discount, vm: AppViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(d.title, fontWeight = FontWeight.Bold)
                Text("${d.percent?.toInt() ?: 0}٪ - ${if (d.isActive) "فعال" else "غیرفعال"}")
            }
            Row {
                TextButton(onClick = { d.id?.let { vm.toggleDiscountActive(it, !d.isActive) } }) {
                    Text(if (d.isActive) "غیرفعال کن" else "فعال کن")
                }
                TextButton(onClick = { d.id?.let { vm.deleteDiscount(it) } }) { Text("حذف") }
            }
        }
    }
}

// ---------------- دفترچه تلفن ----------------
@Composable
private fun ContactsTab(vm: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var social by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("مشتری جدید", fontWeight = FontWeight.Bold)
                OutlinedTextField(name, { name = it }, label = { Text("نام") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(phone, { phone = it }, label = { Text("شماره تلفن") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(social, { social = it }, label = { Text("لینک فضای مجازی (اختیاری)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        vm.addContact(name, phone.ifBlank { null }, social.ifBlank { null }, null)
                        name = ""; phone = ""; social = ""
                    },
                    enabled = name.isNotBlank()
                ) { Text("افزودن به دفترچه") }
            }
        }
        Spacer(Modifier.height(12.dp))
        vm.myContacts.forEach { c -> ContactRow(c, vm) }
    }
}

@Composable
private fun ContactRow(c: VendorContact, vm: AppViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(c.fullName, fontWeight = FontWeight.Bold)
                c.phone?.let { Text(it) }
            }
            TextButton(onClick = { c.id?.let { vm.deleteContact(it) } }) { Text("حذف") }
        }
    }
}

// ---------------- فضای مجازی ----------------
@Composable
private fun SocialLinksTab(vm: AppViewModel) {
    var label by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("این لینک‌ها در پنل مشتری‌های شما دیده می‌شود", modifier = Modifier.padding(bottom = 8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                OutlinedTextField(label, { label = it }, label = { Text("عنوان (مثلاً اینستاگرام)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(url, { url = it }, label = { Text("لینک") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.addSocialLink(label, url); label = ""; url = "" },
                    enabled = label.isNotBlank() && url.isNotBlank()
                ) { Text("افزودن") }
            }
        }
        Spacer(Modifier.height(12.dp))
        vm.mySocialLinks.forEach { l -> SocialLinkRow(l, vm) }
    }
}

@Composable
private fun SocialLinkRow(l: VendorSocialLink, vm: AppViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(l.platformLabel, fontWeight = FontWeight.Bold)
                Text(l.url)
            }
            TextButton(onClick = { l.id?.let { vm.deleteSocialLink(it) } }) { Text("حذف") }
        }
    }
}

// ---------------- تبلیغ / محتوا ----------------
@Composable
private fun PostsTab(vm: AppViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("این محتوا در پنل مشتری‌های شما نمایش داده می‌شود", modifier = Modifier.padding(bottom = 8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("عنوان تبلیغ") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(content, { content = it }, label = { Text("متن") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.addPost(title, content.ifBlank { null }); title = ""; content = "" },
                    enabled = title.isNotBlank()
                ) { Text("انتشار") }
            }
        }
        Spacer(Modifier.height(12.dp))
        vm.myPosts.forEach { p -> PostRow(p, vm) }
    }
}

@Composable
private fun PostRow(p: VendorPost, vm: AppViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(p.title, fontWeight = FontWeight.Bold)
                p.content?.let { Text(it) }
            }
            TextButton(onClick = { p.id?.let { vm.deletePost(it) } }) { Text("حذف") }
        }
    }
}

// ---------------- حسابداری ----------------
@Composable
private fun AccountingTab(vm: AppViewModel) {
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(true) }

    val totalIncome = vm.myTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = vm.myTransactions.filter { it.type == "expense" }.sumOf { it.amount }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("مانده حساب: ${"%,d".format(totalIncome - totalExpense)} تومان", fontWeight = FontWeight.Bold)
                Text("جمع درآمد: ${"%,d".format(totalIncome)}  |  جمع هزینه: ${"%,d".format(totalExpense)}")
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Row {
                    FilterChip(selected = isIncome, onClick = { isIncome = true }, label = { Text("درآمد") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = !isIncome, onClick = { isIncome = false }, label = { Text("هزینه") })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(amount, { amount = it }, label = { Text("مبلغ (تومان)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(desc, { desc = it }, label = { Text("شرح") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val a = amount.toLongOrNull() ?: return@Button
                        vm.addTransaction(if (isIncome) "income" else "expense", a, desc.ifBlank { null }, todayIso())
                        amount = ""; desc = ""
                    },
                    enabled = amount.toLongOrNull() != null
                ) { Text("ثبت") }
            }
        }
        Spacer(Modifier.height(12.dp))
        vm.myTransactions.forEach { t -> TransactionRow(t, vm) }
    }
}

@Composable
private fun TransactionRow(t: VendorTransaction, vm: AppViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(if (t.type == "income") "درآمد" else "هزینه", fontWeight = FontWeight.Bold)
                Text("${"%,d".format(t.amount)} تومان")
                t.description?.let { Text(it) }
            }
            TextButton(onClick = { t.id?.let { vm.deleteTransaction(it) } }) { Text("حذف") }
        }
    }
}

private fun todayIso(): String {
    val c = java.util.Calendar.getInstance()
    return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
}

// ---------------- پیامک ----------------
@Composable
private fun SmsTab(vm: AppViewModel) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (!hasPermission) {
            Text("برای ارسال پیامک نیاز به اجازه دسترسی داری")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { launcher.launch(Manifest.permission.SEND_SMS) }) { Text("اجازه بده") }
            return
        }

        OutlinedTextField(message, { message = it }, label = { Text("متن پیامک") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val numbers = vm.myContacts.mapNotNull { it.phone }
                SmsSender.sendToMany(numbers, message)
            },
            enabled = message.isNotBlank() && vm.myContacts.any { !it.phone.isNullOrBlank() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ارسال به همه مشتری‌ها (${vm.myContacts.count { !it.phone.isNullOrBlank() }} نفر)") }

        Spacer(Modifier.height(16.dp))
        Text("ارسال تکی:", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        vm.myContacts.filter { !it.phone.isNullOrBlank() }.forEach { c ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(c.fullName)
                    TextButton(
                        onClick = { c.phone?.let { SmsSender.sendTo(it, message) } },
                        enabled = message.isNotBlank()
                    ) { Text("ارسال") }
                }
            }
        }
    }
}
