package com.example.hitproduct.data.model.check_couple

data class CheckCoupleResponse(
    val statusCode: Int,
    val message: String,
    val data: CheckCoupleData
)

data class CheckCoupleData(
    val coupleId: String?    // null nếu chưa có đôi, else chứa id đôi
)