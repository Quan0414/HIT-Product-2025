package com.example.hitproduct.screen.dialog.note.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.calendar.response.NewNoteResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class DialogCreateNoteViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _createNote = MutableLiveData<UiState<NewNoteResponse>>()
    val createNote: MutableLiveData<UiState<NewNoteResponse>> = _createNote

    fun createNote(content: String, date: String) {
        _createNote.value = UiState.Loading
        viewModelScope.launch {
            when (val response = authRepository.createNote(content, date)) {
                is DataResult.Error -> {
                    _createNote.value = UiState.Error(response.error)
                }

                is DataResult.Success -> {
                    _createNote.value = UiState.Success(response.data.data)
                }
            }
        }
    }

    fun resetCreateNoteState() {
        _createNote.value = UiState.Idle
    }
}