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

    fun checkInvite() {
        viewModelScope.launch {
            val result = authRepository.checkInvite()
            _inviteResult.value = result
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