package com.example.hitproduct.screen.authentication.verify_code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class VerifyCodeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _sendOtpState = MutableLiveData<UiState<String>>(UiState.Idle)
    val sendOtpState: MutableLiveData<UiState<String>> = _sendOtpState

    private val _verifyCodeState = MutableLiveData<UiState<String>>(UiState.Idle)
    val verifyCodeState: MutableLiveData<UiState<String>> = _verifyCodeState

    private val _verifyCodeState2 = MutableLiveData<UiState<String>>(UiState.Idle)
    val verifyCodeState2: MutableLiveData<UiState<String>> = _verifyCodeState2

    fun sendOtp(email: String) {

        viewModelScope.launch {
            _sendOtpState.value = UiState.Loading
            when (val result = authRepository.sendOtp(email)) {
                is DataResult.Success -> {
                    _sendOtpState.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _sendOtpState.value = UiState.Error(result.error)
                }
            }
        }
    }

    fun verifyCode(otp: String, email: String, type: String) =
        viewModelScope.launch {
            _verifyCodeState.value = UiState.Loading
            when (val result = authRepository.verifyCode(otp, email, type)) {
                is DataResult.Success -> {
                    _verifyCodeState.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _verifyCodeState.value = UiState.Error(result.error)
                }
            }
        }

    fun verifyCode2(otp: String, email: String, type: String) =
        viewModelScope.launch {
            _verifyCodeState2.value = UiState.Loading
            when (val result = authRepository.verifyCode2(otp, email, type)) {
                is DataResult.Success -> {
                    _verifyCodeState2.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _verifyCodeState2.value = UiState.Error(result.error)
                }
            }
        }

    fun clearVerifyCodeState2() {
        _verifyCodeState2.value = UiState.Idle
    }
}