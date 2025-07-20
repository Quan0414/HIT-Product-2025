package com.example.hitproduct.data.model.note

import java.io.Serializable

data class NoteResponse(
    val notes: List<Note>,
)

data class Note(
    val _id: String,
    val date: String,
    val content: String,
    val creaBy: String,
    val createdAt: String,
    val updatedAt: String,
) : Serializable