package com.example.hitproduct.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("avatar")
    val avatar: String,

    @SerializedName("isVerified")
    val isVerified: Boolean,

    @SerializedName("role")
    val role: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("coupleCode")
    val coupleCode: String,

    @SerializedName("__v")
    val version: Int,

    @SerializedName("tokenOtp")
    val tokenOtp: String?
)