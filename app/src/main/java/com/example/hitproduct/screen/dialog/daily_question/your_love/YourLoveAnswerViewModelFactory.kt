package com.example.hitproduct.screen.dialog.daily_question.your_love

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class YourLoveAnswerViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YourLoveAnswerViewModel::class.java)) {
            return YourLoveAnswerViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}