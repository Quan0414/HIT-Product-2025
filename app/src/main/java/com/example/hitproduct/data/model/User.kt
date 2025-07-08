package com.example.hitproduct.data.model

import com.example.hitproduct.data.model.invite.InviteItem
import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("_id") val id: String,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val email: String,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val avatar: String? = null,
    val coupleCode: String? = null,
    val tokenOtp: String? = null,
    val coupleId: String? = null,
    val acceptFriends: List<InviteItem> = emptyList(),
    val requestFriends: List<InviteItem> = emptyList()
)
