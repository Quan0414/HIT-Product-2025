package com.example.hitproduct.screen.authentication.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.ErrorMessageMapper
import com.example.hitproduct.data.model.UserData
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<UiState<ApiResponse<String>>>(UiState.Idle)
    val loginState: LiveData<UiState<ApiResponse<String>>> = _loginState

    private val _coupleState = MutableLiveData<UiState<UserData>>(UiState.Idle)
    val coupleState: LiveData<UiState<UserData>> = _coupleState

    fun clearLoginState() {
        _loginState.value = UiState.Idle
    }

    fun login(email: String, password: String) {

//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            _loginState.value = UiState.Error(
//                ErrorMessageMapper.fromBackend("must be a valid email")
//            )
//            return
//        }
        if (password.length < 6) {
            _loginState.value = UiState.Error(
                ErrorMessageMapper.fromBackend("length must be at least 6 characters long")
            )
            return
        }

        // 2. Gọi API
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is DataResult.Success -> {
                    _loginState.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    // XONG: chỉ show result.error, không cần map lại
                    _loginState.value = UiState.Error(result.error)
                }
            }
        }
    }

    fun checkCouple(token: String) {
        viewModelScope.launch {
            _coupleState.value = UiState.Loading
            when (val res = authRepository.checkCouple(token)) {
                is DataResult.Success ->
                    _coupleState.value = UiState.Success(res.data)

                is DataResult.Error ->
                    _coupleState.value = UiState.Error(res.error)
            }
        }
    }
}
