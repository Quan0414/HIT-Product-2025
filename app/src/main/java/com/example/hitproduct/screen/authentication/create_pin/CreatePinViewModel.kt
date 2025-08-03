package com.example.hitproduct.screen.authentication.create_pin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class CreatePinViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _sendKeyState = MutableLiveData<UiState<String>>(UiState.Idle)
    val sendKeyState: LiveData<UiState<String>> = _sendKeyState

    fun sendKey(publicKey: String, privateKey: String) {
        _sendKeyState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.sendPublicKey(publicKey, privateKey)) {
                is DataResult.Success -> {
                    _sendKeyState.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _sendKeyState.value = UiState.Error(result.error)
                }
            }
        }
    }
}