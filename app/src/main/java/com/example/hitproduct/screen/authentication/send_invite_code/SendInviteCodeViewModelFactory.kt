package com.example.hitproduct.screen.authentication.send_invite_code

import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class SendInviteCodeViewModelFactory(
    private val authRepository: AuthRepository
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SendInviteCodeViewModel::class.java)) {
            return SendInviteCodeViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}