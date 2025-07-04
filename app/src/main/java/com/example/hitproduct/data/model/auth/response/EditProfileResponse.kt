package com.example.hitproduct.data.model.auth.response

import com.example.hitproduct.data.model.UserData

data class EditProfileResponse (
    val statusCode: Int,
    val message: String,
    val data: UserData
)