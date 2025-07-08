package com.example.hitproduct.data.model.invite

sealed class InviteItem {
    data class Sent(
        val toUser: String,
        val userId: String
    ) : InviteItem()

    data class Received(
        val fromUser: String,
        val userId: String
    ) : InviteItem()
}
