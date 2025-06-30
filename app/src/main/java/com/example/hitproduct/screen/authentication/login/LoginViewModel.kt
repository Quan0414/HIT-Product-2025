package com.example.hitproduct.screen.authentication.login

import android.util.Patterns
import androidx.lifecycle.*
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.ErrorMessageMapper
import com.example.hitproduct.data.model.response.LoginResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<UiState<LoginResponse>>(UiState.Idle)
    val loginState: LiveData<UiState<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        // 1. Local validation ngay khi bấm login
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length < 6) {
            _loginState.value = UiState.Error(
                ErrorMessageMapper.fromBackend(
                    "\"email\" must be a valid email,\"password\" length must be at least 6 characters long"
                )
            )
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = UiState.Error(
                ErrorMessageMapper.fromBackend("\"email\" must be a valid email")
            )
            return
        }
        if (password.length < 6) {
            _loginState.value = UiState.Error(
                ErrorMessageMapper.fromBackend("\"password\" length must be at least 6 characters long")
            )
            return
        }

        // 2. Gọi API
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is DataResult.Success -> {
                    val apiResp = result.data
                    if (apiResp.success) {
                        _loginState.value = UiState.Success(apiResp.data)
                    } else {
                        // Trả trực tiếp MappedError
                        val mapped = ErrorMessageMapper.fromBackend(apiResp.message)
                        _loginState.value = UiState.Error(mapped)
                    }
                }

                is DataResult.Error -> {
                    val mapped = ErrorMessageMapper.fromBackend(
                        result.exception.message.orEmpty()
                    )
                    _loginState.value = UiState.Error(mapped)
                }
            }
        }
    }
}
