package com.example.hitproduct.screen.authentication.register.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class RegisterViewModelFactory (
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}