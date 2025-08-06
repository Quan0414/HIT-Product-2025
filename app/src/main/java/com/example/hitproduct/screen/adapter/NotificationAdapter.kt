package com.example.hitproduct.screen.adapter

import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.data.model.notification.Notification
import java.time.Instant

class NotificationAdapter(
    private val items: MutableList<Notification> = mutableListOf()
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    fun submitList(newNotifications: List<Notification>) {
        items.clear()
        items.addAll(newNotifications)
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val type = view.findViewById<ImageView>(R.id.ivNotiType)
        private val content = view.findViewById<TextView>(R.id.tvContent)
        private val time = view.findViewById<TextView>(R.id.tvTime)

        fun bind(notification: Notification) {
            when (notification.type) {
                "feed_pet" -> type.setImageResource(R.drawable.ic_noti_home)
                "love_note" -> type.setImageResource(R.drawable.ic_noti_note)
                "chat_message" -> type.setImageResource(R.drawable.ic_noti_mess)
                "answer_question" -> type.setImageResource(R.drawable.ic_noti_home)
                else -> type.setImageResource(R.drawable.ic_noti_setting)
            }

            content.text = notification.content

            val relativeTime = formatRelativeTime(notification.createdAt)
            time.text = relativeTime

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun formatRelativeTime(createdAtIso: String): String {
        // Phân tích chuỗi ISO thành epoch millis
        val timeMillis = Instant.parse(createdAtIso).toEpochMilli()
        // Lấy thời gian hiện tại
        val now = System.currentTimeMillis()
        // Tính khoảng cách thời gian
        val diffMillis = now - timeMillis

        return when {
            diffMillis < DateUtils.MINUTE_IN_MILLIS -> "Vừa xong"
            diffMillis < DateUtils.HOUR_IN_MILLIS -> {
                val minutes = (diffMillis / DateUtils.MINUTE_IN_MILLIS).toInt()
                "$minutes phút trước"
            }

            diffMillis < DateUtils.DAY_IN_MILLIS -> {
                val hours = (diffMillis / DateUtils.HOUR_IN_MILLIS).toInt()
                "$hours giờ trước"
            }

            diffMillis < DateUtils.WEEK_IN_MILLIS -> {
                val days = (diffMillis / DateUtils.DAY_IN_MILLIS).toInt()
                "$days ngày trước"
            }

            diffMillis < DateUtils.YEAR_IN_MILLIS -> {
                val weeks = (diffMillis / DateUtils.WEEK_IN_MILLIS).toInt()
                "$weeks tuần trước"
            }

            else -> {
                val years = (diffMillis / DateUtils.YEAR_IN_MILLIS).toInt()
                "$years năm trước"
            }
        }
    }
}