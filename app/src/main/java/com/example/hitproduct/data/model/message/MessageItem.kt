package com.example.hitproduct.data.model.message

sealed class ChatItem {
    abstract val id: String
    abstract val senderId: String
    abstract val avatarUrl: String?
    abstract val sentAt: String
    abstract val fromMe: Boolean

    data class TextMessage(
        override val id: String,
        override val senderId: String,
        override val avatarUrl: String?,
        val text: String,
        override val sentAt: String,
        override val fromMe: Boolean
    ) : ChatItem()

    data class ImageMessage(
        override val id: String,
        override val senderId: String,
        override val avatarUrl: String?,
        val imageUrl: String,
        override val sentAt: String,
        override val fromMe: Boolean
    ) : ChatItem()

    data object TypingIndicator : ChatItem() {
        override val id: String = "typing_indicator"
        override val senderId: String = ""
        override val avatarUrl: String? = null
        override val sentAt: String = ""
        override val fromMe: Boolean = false
    }
}
