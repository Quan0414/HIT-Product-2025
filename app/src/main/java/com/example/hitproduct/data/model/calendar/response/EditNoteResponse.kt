package com.example.hitproduct.data.model.calendar.response

data class EditNoteResponse(
    val data: Update
)

data class Update(
    val _id: String,
    val content: String,
)