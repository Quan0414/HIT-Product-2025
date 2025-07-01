package com.example.hitproduct.data.model.auth.request

data class VerifyCodeRequest (
    val otp : String,
    val email: String,
    val type: String
)