package com.example.hitproduct.screen.dialog.note.delete

import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

class DialogDeleteNoteViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialogDeleteNoteViewModel::class.java)) {
            return DialogDeleteNoteViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}