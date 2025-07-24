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
    @SerializedName("loveStartedAt") val loveStartedAt: String,  // hoặc Instant nếu bạn config DateTypeAdapter
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int
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
