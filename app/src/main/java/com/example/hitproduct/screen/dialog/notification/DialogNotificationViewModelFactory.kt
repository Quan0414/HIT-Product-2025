package com.example.hitproduct.screen.dialog.notification

import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class DialogNotificationViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialogNotificationViewModel::class.java)) {
            return DialogNotificationViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}