package com.example.hitproduct.common.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Central configuration for notification templates by event type.
 */
object NotificationConfig {
    /**
     * Template containing title and body for a notification.
     */
    data class Template(val title: String, val body: String)

    /**
     * Generate notification template based on event type and data payload.
     * @param type Event type (e.g. "invite", "trip", "sos", "auth" )
     * @param data Raw data map from message payload
     */
    fun getTemplate(type: String, data: Map<String, String>?): Template {

        return when (type) {
            // 1. Lời mời ghép đôi
            "pair_request" -> Template(
                title = "Bạn có lời mời ghép đôi 💌",
                body  = "Mở ứng dụng để xem ai vừa mời bạn nhé!"
            )

            // 2. Ghép đôi thành công
            "pair_success" -> Template(
                title = "Ghép đôi thành công 💖",
                body  = "Chúc mừng! Hai bạn giờ đã là một đôi hoàn hảo"
            )

            // 3. Chọn ngày bắt đầu
            "start_date_selected" -> {
                val date = data?.get("startDate") ?: ""
                Template(
                    title = "Ngày bắt đầu đã được chọn 🌅",
                    body  = if (date.isNotBlank())
                        "Hãy nhớ: ngày khởi hành là $date"
                    else
                        "Mở ứng dụng để xem ngày khởi hành"
                )
            }

            // 4. Cho pet ăn
            "pet_fed" -> Template(
                title = "Pet vừa được cho ăn 🍲",
                body  = "Mở ứng dụng để kiểm tra tình trạng pet nhé!"
            )

            // 5a. Thêm ghi chú
            "note_added" -> Template(
                title = "Có kỷ niệm mới ✨",
                body  = "Mở ứng dụng để xem ghi chú vừa thêm"
            )
            // 5b. Cập nhật ghi chú
            "note_updated" -> Template(
                title = "Ghi chú vừa được chỉnh sửa ✍️",
                body  = "Mở ứng dụng để xem nội dung cập nhật"
            )
            // 5c. Xoá ghi chú
            "note_deleted" -> Template(
                title = "Ghi chú đã bị xoá 💔",
                body  = "Ghi chú đó đã bị xoá nhưng kỷ niệm vẫn còn"
            )

            // 6. Tin nhắn mới
            "chat_message" -> Template(
                title = "Bạn có tin nhắn mới 💬",
                body  = "Mở ứng dụng để đọc tin nhắn"
            )

            // Mặc định
            else -> Template(
                title = "Thông báo mới 🍯",
                body  = "Mở ứng dụng để xem chi tiết"
            )
        }    }
}

// Extensions for NotificationManager and creating a default channel

/**
 * Extension property to get NotificationManager from Context
 */
val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

/**
 * Create a default notification channel for general notifications.
 * Call this once in Application.onCreate().
 */
fun Context.createNotificationChannel() {
    val defaultChannel = NotificationChannel(
        "default_channel",               // ID channel
        "Thông báo chung",               // Tên hiển thị
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Kênh thông báo chung của app"
    }
    notificationManager.createNotificationChannel(defaultChannel)
}
