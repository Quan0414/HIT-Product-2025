package com.example.hitproduct.screen.authentication.create_pin

import com.example.hitproduct.data.repository.AuthRepository

class CreatePinViewModelFactory(
    private val authRepository: AuthRepository
) : androidx.lifecycle.ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePinViewModel::class.java)) {
            return CreatePinViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}