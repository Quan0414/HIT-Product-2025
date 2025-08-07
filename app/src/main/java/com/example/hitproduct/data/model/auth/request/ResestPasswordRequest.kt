package com.example.hitproduct.data.model.auth.request

data class ResestPasswordRequest (
    val email: String,
    val newPassword: String,
    val repeatNewPassword: String,
    val token: String
)