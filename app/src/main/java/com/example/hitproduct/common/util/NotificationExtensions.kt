package com.example.hitproduct.common.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

// Extension để lấy NotificationManager
val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

fun Context.createNotificationChannel() {
    val channel = NotificationChannel(
        "default_channel",               // ID channel
        "Thông báo chung",               // Tên hiển thị
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Kênh thông báo chung của app"
    }
    notificationManager.createNotificationChannel(channel)
}