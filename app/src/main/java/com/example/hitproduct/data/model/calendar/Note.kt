package com.example.hitproduct.data.model.calendar

import java.io.Serializable

data class Note(
    val _id: String,
    val date: String,
    val content: String,
    val createBy: String,
    val createdAt: String,
    val updatedAt: String,
) : Serializable