package com.example.hitproduct.data.model.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    val statusCode: Int,
    val message: String,

    @SerializedName("data")
    val token: String,
)