package com.example.hitproduct.screen.authentication.send_invite_code

import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.socket.SocketManager
import kotlinx.coroutines.launch

class SendInviteCodeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _inviteResult = MutableLiveData<DataResult<InviteData>>()
    val inviteResult: LiveData<DataResult<InviteData>> = _inviteResult

    private val _inviteCode = MutableLiveData<String>()
    val inviteCode: LiveData<String> = _inviteCode

    fun checkInvite(token: String) {
        viewModelScope.launch {
            val result = authRepository.checkInvite(token)
            _inviteResult.value = result
        }
    }


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