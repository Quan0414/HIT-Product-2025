package com.example.hitproduct.data.model.user_profile

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String?,
    val gender: String?,
    val avatar: String?,
    val requestFriends: List<String>,
    val acceptFriends: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val coupleCode: String,
    val dateOfBirth: String?,
    val firstName: String?,
    val lastName: String?,
    val nickname: String?,
    val tokenOtp: String?,
    @SerializedName("coupleId")
    val couple: Couple?,
    val roomChatId: String?
) : Serializable

data class Couple(
    @SerializedName("_id") val id: String,
    @SerializedName("userIdA") val userA: UserSummary,
    @SerializedName("userIdB") val userB: UserSummary
)

data class UserSummary(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String,
    val gender: String,
    val avatar: String,
    val isVerified: Boolean,
    val role: String,
    val requestFriends: List<String>,
    val acceptFriends: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val coupleCode: String,
    @SerializedName("__v") val v: Int,
    val tokenOtp: String?,
    val coupleId: String?
)
