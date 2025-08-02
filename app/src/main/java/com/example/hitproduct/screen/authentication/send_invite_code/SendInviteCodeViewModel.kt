package com.example.hitproduct.screen.authentication.send_invite_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SendInviteCodeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _inviteResult = MutableLiveData<DataResult<InviteData>>()
    val inviteResult: LiveData<DataResult<InviteData>> = _inviteResult

    private val _publicKeyState = MutableLiveData<UiState<String>>(UiState.Idle)
    val publicKeyState: LiveData<UiState<String>> = _publicKeyState

    fun checkInvite() {
        viewModelScope.launch {
            val result = authRepository.checkInvite()
            _inviteResult.value = result
        }
    }

    fun sendPublicKey(publicKey: String) {
        _publicKeyState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.sendPublicKey(publicKey)) {
                is DataResult.Success -> {
                    _publicKeyState.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _publicKeyState.value = UiState.Error(result.error)
                }
            }
        }
    }

}