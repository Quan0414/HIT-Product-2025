package com.example.hitproduct.screen.dialog.note.delete

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class DialogDeleteNoteViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _deleteNote = MutableLiveData<UiState<String>>(UiState.Idle)
    val deleteNote: LiveData<UiState<String>> = _deleteNote

    fun deleteNote(noteId: String) {
        _deleteNote.value = UiState.Loading
        viewModelScope.launch {
            when (val response = authRepository.deleteNote(noteId)) {
                is DataResult.Error -> {
                    _deleteNote.value = UiState.Error(response.error)
                }
                is DataResult.Success -> {
                    _deleteNote.value = UiState.Success(response.data.message)
                }
            }
        }
    }

    fun resetDeleteNoteState() {
        _deleteNote.value = UiState.Idle
    }
}