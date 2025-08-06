package com.example.hitproduct.screen.authentication.create_pin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.user_profile.User
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.socket.SocketManager
import kotlinx.coroutines.launch

class CreatePinViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _sendKeyState = MutableLiveData<UiState<String>>(UiState.Idle)
    val sendKeyState: LiveData<UiState<String>> = _sendKeyState

    private val _profileState = MutableLiveData<UiState<User>>(UiState.Idle)
    val profileState: LiveData<UiState<User>> = _profileState

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

    fun checkProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            when (val res = authRepository.fetchProfile()) {
                is DataResult.Success ->
                    _profileState.value = UiState.Success(res.data)

                is DataResult.Error ->
                    _profileState.value = UiState.Error(res.error)
            }
        }
    }

    fun sendNewKey(publicKey: String, myLoveId: String) {
        try {
            SocketManager.sendNewPubKey(publicKey, myLoveId)
        } catch (e: Exception) {
            Log.e("CreatePinViewModel", "Socket error: ${e.message}")
            // Không cần báo lỗi cho user vì đây chỉ là notification
        }
    }
}