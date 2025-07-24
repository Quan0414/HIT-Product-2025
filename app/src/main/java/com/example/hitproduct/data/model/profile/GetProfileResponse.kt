package com.example.hitproduct.data.model.auth.response

import com.example.hitproduct.data.model.UserData

data class ProfileResponse(
    val statusCode: Int,
    val message: String,
    val data: UserData       // ở đây data chính là object User
)
