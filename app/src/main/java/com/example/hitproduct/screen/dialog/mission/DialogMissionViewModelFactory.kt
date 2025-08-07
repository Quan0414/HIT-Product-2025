package com.example.hitproduct.screen.dialog.mission

import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class DialogMissionViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialogMissionViewModel::class.java)) {
            return DialogMissionViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}