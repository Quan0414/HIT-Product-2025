package com.example.hitproduct.screen.home_page.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.calendar.Note
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class NoteViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _notes = MutableLiveData<UiState<List<Note>>>(UiState.Idle)
    val notes: LiveData<UiState<List<Note>>> get() = _notes

    fun fetchNotes() {
        viewModelScope.launch {
            _notes.value = UiState.Loading
            when (val result = authRepository.fetchNote()) {
                is DataResult.Error -> {
                    _notes.value = UiState.Error(result.error)
                }

                is DataResult.Success -> {
                    Log.d("NoteVM", "Fetched notes: ${result.data.data.notes.map { it.date }}")
                    _notes.value = UiState.Success(result.data.data.notes)
                }
            }
        }

    }
}