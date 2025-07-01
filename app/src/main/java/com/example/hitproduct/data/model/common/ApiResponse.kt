package com.example.hitproduct.data.model.common

data class ApiResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T
) {
    // computed property
    val success: Boolean
        get() = statusCode in 200..299
}