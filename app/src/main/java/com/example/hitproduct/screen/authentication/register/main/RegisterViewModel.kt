package com.example.hitproduct.screen.authentication.register.main

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.ErrorMessageMapper
import com.example.hitproduct.data.model.response.RegisterResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel (
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableLiveData<UiState<RegisterResponse>>(UiState.Idle)
    val registerState: LiveData<UiState<RegisterResponse>> = _registerState

    fun clearRegisterState() {
        _registerState.value = UiState.Idle
    }


    fun register(username: String, email: String, password: String, repeatPassword: String) {
        // Local validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = UiState.Error(
                ErrorMessageMapper.fromBackend("\"email\" must be a valid email")
            )
            return
        }
        if (password.length < 6) {
            _registerState.value = UiState.Error(
                ErrorMessageMapper.fromBackend("\"password\" length must be at least 6 characters long")
            )
            return
        }
        if (password != repeatPassword) {
            _registerState.value = UiState.Error(
                ErrorMessageMapper.fromBackend("Mật khẩu nhập lại không khớp với mật khẩu.")
            )
            return
        }


        // Call API
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.register(username, email, password, repeatPassword)) {
                is DataResult.Success -> {
                    _registerState.value = UiState.Success(result.data)
                }
                is DataResult.Error -> {
                    _registerState.value = UiState.Error(result.error)
                }
            }
        }
    }
}
