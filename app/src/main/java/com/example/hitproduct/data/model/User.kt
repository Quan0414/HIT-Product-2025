package com.example.hitproduct.data.model

data class UserData(
    val _id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val gender: String,
    val avatar: String,
    val isVerified: Boolean,
    val role: String,
    val createdAt: String,
    val updatedAt: String,
    val coupleCode: String,
    val __v: Int,
    val tokenOtp: String?    // nếu backend trả null thì dùng nullable
)