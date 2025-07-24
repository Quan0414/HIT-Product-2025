package com.example.hitproduct.data.model.calendar.response

import com.example.hitproduct.data.model.calendar.Note

data class GetNoteResponse(
    val notes: List<Note>,
)
