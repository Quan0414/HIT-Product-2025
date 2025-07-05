package com.example.hitproduct.data.model.invite

import com.google.gson.annotations.SerializedName

data class InviteData(
    val userId: String,
    val acceptFriends: List<InviteUser>,
    val requestFriends: List<InviteUser>
)

data class InviteUser(
    @SerializedName("_id")
    val id: String,
    val username: String
)
