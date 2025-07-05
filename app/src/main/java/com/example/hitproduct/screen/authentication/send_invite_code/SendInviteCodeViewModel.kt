package com.example.hitproduct.screen.authentication.send_invite_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SendInviteCodeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _inviteResult = MutableLiveData<DataResult<InviteData>>()
    val inviteResult: LiveData<DataResult<InviteData>> = _inviteResult


    fun checkInvite(token: String) {
        viewModelScope.launch {
            val result = authRepository.checkInvite(token)
            _inviteResult.value = result
        }
    }
}