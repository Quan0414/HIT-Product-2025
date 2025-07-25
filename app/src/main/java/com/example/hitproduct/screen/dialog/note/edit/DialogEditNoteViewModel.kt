package com.example.hitproduct.screen.dialog.note.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.calendar.response.EditNoteResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class DialogEditNoteViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {
    private val _editNote = MutableLiveData<UiState<EditNoteResponse>>(UiState.Idle)
    val editNote: MutableLiveData<UiState<EditNoteResponse>> = _editNote

    fun editNote(noteId: String, content: String) {
        _editNote.value = UiState.Loading
        viewModelScope.launch {
            when (val response = authRepo.editNote(noteId, content)) {
                is DataResult.Error -> {
                    _editNote.value = UiState.Error(response.error)
                }

                is DataResult.Success -> {
                    _editNote.value = UiState.Success(response.data.data)
                }
            }
        }
    }

    fun resetEditNoteState() {
        _editNote.value = UiState.Idle
    }
}