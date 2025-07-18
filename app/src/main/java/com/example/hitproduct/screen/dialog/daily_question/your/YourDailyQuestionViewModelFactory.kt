package com.example.hitproduct.screen.dialog.daily_question.your

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class YourDailyQuestionViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YourDailyQuestionViewModel::class.java)) {
            return YourDailyQuestionViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}