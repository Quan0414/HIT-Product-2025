package com.example.hitproduct.screen.authentication.send_invite_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.user_profile.User
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SendInviteCodeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _inviteResult = MutableLiveData<DataResult<InviteData>>()
    val inviteResult: LiveData<DataResult<InviteData>> = _inviteResult

    private val _inviteCode = MutableLiveData<UiState<User>>()
    val inviteCode: LiveData<UiState<User>> = _inviteCode

//    private val _inviteMessage = MutableLiveData<String>()
//    val inviteMessage: LiveData<String> = _inviteMessage

    fun checkInvite(token: String) {
        viewModelScope.launch {
            val result = authRepository.checkInvite(token)
            _inviteResult.value = result
        }
    }

    fun fetchUserProfile() = viewModelScope.launch {
        when (val result =
            authRepository.fetchProfile(token = authRepository.getAccessToken() ?: "")) {
            is DataResult.Success ->
                _inviteCode.value = UiState.Success(result.data)

            is DataResult.Error ->
                _inviteCode.value = UiState.Error(result.error)
        }
    }

//    fun setInviteMessage(message: String) {
//        if (inviteMessage.value == message) return
//        else {
//            _inviteMessage.value = message
//        }
//    }
//
//    init {
//        // --- 7. Bắt lỗi socket ---
//        SocketManager.onError { errMsg ->
//            setInviteMessage(errMsg)
//        }
//
//        SocketManager.onSuccess { errMsg ->
//            setInviteMessage(errMsg)
//        }
//    }
}