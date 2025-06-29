package com.example.hitproduct.screen.authentication.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.auth.LoginResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch


class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<UiState<LoginResponse>>(UiState.Loading)
    val loginState: LiveData<UiState<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is DataResult.Success -> {
                    val apiResp = result.data    // đây là ApiResponse<LoginResponse>
                    if (apiResp.success) {
                        _loginState.value = UiState.Success(apiResp.data)
                    } else {
                        _loginState.value = UiState.Error(
                            Throwable(apiResp.message)
                        )
                    }
                }

                is DataResult.Error -> {
                    _loginState.value = UiState.Error(result.exception)
                }
            }
        }
    }

}