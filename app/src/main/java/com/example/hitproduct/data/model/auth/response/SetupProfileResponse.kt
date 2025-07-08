package com.example.hitproduct.data.model.auth.response

import com.example.hitproduct.data.model.auth.UserDataSetup

data class SetupProfileResponse (
    val statusCode: Int,
    val message: String,
    val data: UserDataSetup
)