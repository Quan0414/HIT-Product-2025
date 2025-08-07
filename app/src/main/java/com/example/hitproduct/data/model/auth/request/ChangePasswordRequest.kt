package com.example.hitproduct.data.model.auth.request

data class ChangePasswordRequest (
    val password: String,
    val newPassword: String,
    val repeatNewPassword: String
)