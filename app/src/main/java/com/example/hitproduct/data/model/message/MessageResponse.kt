package com.example.hitproduct.data.model.message

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    val messages: List<Message>
)

data class Message(
    @SerializedName("_id") val id: String,
    val roomChatId: String,
    val senderId: Sender,
    val content: String,
    val images: List<String>,
    val sentAt: String
)

data class Sender(
    @SerializedName("_id") val id: String,
    val username: String,
    val avatar: String
)
