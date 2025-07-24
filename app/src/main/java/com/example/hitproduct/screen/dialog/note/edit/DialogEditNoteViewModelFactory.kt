package com.example.hitproduct.screen.dialog.note.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hitproduct.data.repository.AuthRepository

data class DialogEditNoteViewModelFactory(
    private val authRepo: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialogEditNoteViewModel::class.java)) {
            return DialogEditNoteViewModel(authRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}