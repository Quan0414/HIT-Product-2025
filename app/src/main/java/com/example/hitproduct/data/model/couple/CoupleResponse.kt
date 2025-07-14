package com.example.hitproduct.data.model

import com.google.gson.annotations.SerializedName

data class CoupleResponse(
    @SerializedName("couple") val couple: CoupleProfile
)

data class CoupleProfile(
    @SerializedName("_id")           val id: String,
    @SerializedName("userIdA")       val userA: User,
    @SerializedName("userIdB")       val userB: User,
    @SerializedName("coin")          val coin: Int,
    @SerializedName("loveStartedAt") val loveStartedAt: String,  // hoặc Instant nếu bạn config DateTypeAdapter
    @SerializedName("createdAt")     val createdAt: String,
    @SerializedName("updatedAt")     val updatedAt: String,
    @SerializedName("__v")           val version: Int
)

data class User(
    @SerializedName("_id")       val id: String,
    @SerializedName("username")  val username: String,
    @SerializedName("avatar")    val avatar: String
)
