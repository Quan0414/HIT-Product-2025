package com.example.hitproduct.data.model.daily_status

data class DailyStatusResponse(
    val status: Status
)

data class Status(
    val _id: String,
    val coupleId: String,
    val statusId: StatusId,
    val date: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class StatusId(
    val _id: String,
    val content: String
)