package com.example.hitproduct.data.model.auth

import com.example.hitproduct.data.model.User

data class LoginResponse(
    val token: String,
    val user: User
)