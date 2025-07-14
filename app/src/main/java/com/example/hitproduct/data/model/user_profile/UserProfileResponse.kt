package com.example.hitproduct.data.model.user_profile

data class UserProfileResponse(
    val statusCode: Int,
    val message: String,
    val data: User
)