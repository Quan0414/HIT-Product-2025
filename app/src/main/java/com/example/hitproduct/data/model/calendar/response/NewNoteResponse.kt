package com.example.hitproduct.data.model.calendar.response

import java.io.Serializable

data class NewNoteResponse(
    val newNote: NewNote
) : Serializable

data class NewNote(
    val date: String,
    val content: String,
) : Serializable