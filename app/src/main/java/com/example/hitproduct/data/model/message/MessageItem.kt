package com.example.hitproduct.data.model.message

sealed class ChatItem {
    abstract val sentAt: String
    abstract val fromMe: Boolean

    data class TextMessage(
        val id: String,
        val senderId: String,
        val text: String,
        override val sentAt: String,
        override val fromMe: Boolean
    ) : ChatItem()

    data class ImageMessage(
        val id: String,
        val senderId: String,
        val imageUrl: String,
        override val sentAt: String,
        override val fromMe: Boolean
    ) : ChatItem()
}
