package com.example.hitproduct.common.state

import com.example.hitproduct.common.util.MappedError

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: MappedError) : UiState<Nothing>()
}
