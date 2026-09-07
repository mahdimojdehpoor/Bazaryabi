package com.bazaryar.app.util

import android.telephony.SmsManager

/** ارسال پیامک از طریق خط سیم‌کارت خود گوشی (نه سرویس آنلاین) */
object SmsSender {

    fun sendTo(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
    }

    fun sendToMany(phoneNumbers: List<String>, message: String) {
        phoneNumbers.filter { it.isNotBlank() }.forEach { sendTo(it, message) }
    }
}
