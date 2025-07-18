package com.example.hitproduct.data.model.daily_question.get_question

data class DailyQuestionResponse(
    val question: Question
)

data class Question(
    val _id: String,
    val question: String,
)