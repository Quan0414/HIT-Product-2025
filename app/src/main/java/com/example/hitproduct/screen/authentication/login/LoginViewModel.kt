package com.example.hitproduct.screen.authentication.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.ErrorMessageMapper
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.couple.CoupleProfile
import com.example.hitproduct.data.model.user_profile.User
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<UiState<ApiResponse<String>>>(UiState.Idle)
    val loginState: LiveData<UiState<ApiResponse<String>>> = _loginState

    private val _profileState = MutableLiveData<UiState<User>>(UiState.Idle)
    val profileState: LiveData<UiState<User>> = _profileState

    private val _coupleProfile = MutableLiveData<UiState<CoupleProfile>>(UiState.Idle)
    val coupleProfile: LiveData<UiState<CoupleProfile>> = _coupleProfile

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

    fun checkProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            when (val res = authRepository.fetchProfile()) {
                is DataResult.Success ->
                    // res.data: UserProfile
                    _profileState.value = UiState.Success(res.data)

                is DataResult.Error ->
                    _profileState.value = UiState.Error(res.error)
            }
        }
    }

    fun clearProfileState() {
        _profileState.value = UiState.Idle
    }

    fun getCoupleProfile() {
        _coupleProfile.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getCouple()) {
                is DataResult.Success -> {
                    _coupleProfile.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _coupleProfile.value = UiState.Error(result.error)
                }
            }
        }
    }
}
