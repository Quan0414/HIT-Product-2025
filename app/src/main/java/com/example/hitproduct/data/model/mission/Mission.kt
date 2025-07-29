package com.example.hitproduct.data.model.mission

data class Mission(
    val _id: String,
    val coupleId: String,
    val missionId: MissionDetail,
    val date: String,
    val isCompleted: Boolean
)

data class MissionDetail(
    val _id: String,
    val key: String,
    val description: String,
    val coin: Int,
    val isActive: Boolean
)