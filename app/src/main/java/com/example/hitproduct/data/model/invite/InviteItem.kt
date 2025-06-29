package com.example.hitproduct.data.model.invite

sealed class InviteItem {
    data class Sent(
        val code: String,
        val toUser: String,
        val sentDate: String
    ) : InviteItem()

    data class Received(
        val code: String,
        val fromUser: String,
        val receivedDate: String
    ) : InviteItem()
}
