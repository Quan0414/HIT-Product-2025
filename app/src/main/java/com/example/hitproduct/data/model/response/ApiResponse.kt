package com.example.hitproduct.data.model.response

data class ApiResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T
) {
    // computed property
    val success: Boolean
        get() = statusCode == 200
}