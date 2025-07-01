package com.example.hitproduct.data.model.auth.request

data class RegisterRequest (
    val username: String,
    val email: String,
    val password: String,
    val repeatPassword: String
)