package com.example.hitproduct.screen.home_page.setting

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.RequestProfile
import com.example.hitproduct.data.model.UserData
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AccountSettingViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _getProfileState = MutableLiveData<UiState<UserData>>(UiState.Idle)
    val getProfile: LiveData<UiState<UserData>> = _getProfileState

    private val _updateState = MutableLiveData<UiState<String>>(UiState.Idle)
    val updateState: LiveData<UiState<String>> = _updateState

    fun getProfile(token: String) = viewModelScope.launch {
        _getProfileState.value = UiState.Loading
        when (val result = authRepository.checkCouple("Bearer $token")) {
            is DataResult.Success -> {
                // Lấy dữ liệu người dùng từ result.data
                val userData = result.data
                _getProfileState.value = UiState.Success(userData)
            }

            is DataResult.Error -> {
                // Lỗi: hiển thị message
                _getProfileState.value = UiState.Error(result.error)
            }
        }
    }


    fun updateProfile(
        token: String,
        firstName: String?,
        lastName: String?,
        nickname: String?,
        gender: String?,
        dateOfBirth: String?,
        avatarUri: Uri?,
        context: Context
    ) = viewModelScope.launch {
        _updateState.value = UiState.Loading

        val fields: Map<String, RequestBody> = RequestProfile.prepareFields(
            firstName, lastName, nickname, gender, dateOfBirth
        )
        // 2. Chuẩn bị avatar part (nếu có)
        val avatarPart: MultipartBody.Part? = avatarUri
            ?.let { RequestProfile.prepareAvatarPart(it, context) }

        // 3. Gọi repository
        when (val result = authRepository.editProfile("Bearer $token", fields, avatarPart)) {
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