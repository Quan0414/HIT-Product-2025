package com.example.hitproduct.data.model.notification

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Notification(
    val coupleId: String,
    val fromUserId: String,
    val toUserId: String,
    val type: String,
    val content: String,
    val isRead: Boolean,
    @SerializedName("_id")
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v")
    val version: Int
) : Serializable