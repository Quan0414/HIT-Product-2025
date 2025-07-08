package com.example.hitproduct.screen.authentication.register.set_up_infor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.auth.request.SetupProfileRequest
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class SetUpInformationViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _updateState = MutableLiveData<UiState<String>>(UiState.Idle)
    val updateState: LiveData<UiState<String>> = _updateState

    fun updateProfile(
        firstName: String?,
        lastName: String?,
        nickname: String?,
        gender: String?,
        dateOfBirth: String?,
    ) = viewModelScope.launch {
        _updateState.value = UiState.Loading

        val fields: Map<String, RequestBody> = SetupProfileRequest.prepareFields(
            firstName, lastName, nickname, gender, dateOfBirth
        )

        // 3. Gọi repository
        when (val result = authRepository.setupProfile(fields)) {
            is DataResult.Success -> {
                val msg = result.data.message
                _updateState.value = UiState.Success(msg)
            }

            is DataResult.Error -> {
                // Lỗi: hiển thị message
                _updateState.value = UiState.Error(result.error)
            }
        }

    }
}