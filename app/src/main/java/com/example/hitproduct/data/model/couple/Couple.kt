package com.example.hitproduct.data.model.couple

import com.google.gson.annotations.SerializedName

data class CoupleData(
    @SerializedName("couple") val couple: CoupleProfile
)

data class CoupleProfile(
    val loveStartedAtEdited: Boolean,
    @SerializedName("_id") val id: String,
    @SerializedName("userIdA") val userA: User,
    @SerializedName("userIdB") val userB: User,
    @SerializedName("coin") val coin: Int,
    @SerializedName("loveStartedAt") val loveStartedAt: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("public_key_my_love") val myLovePubKey: String?,
    @SerializedName("private_key_user") val myPrivateKey: String?
)

data class User(
    @SerializedName("_id")
    val id: String,
    val username: String,
    val gender: String?,
    val avatar: String?,
    val dateOfBirth: String?,
    val firstName: String?,
    val lastName: String?,
    val nickname: String?,
)
