package com.example.hitproduct.data.model.user_profile

import com.example.hitproduct.data.model.User

data class UserProfileResponse(
    val statusCode: Int,
    val message: String,
    val data: User
)