package com.example.hitproduct.screen.home_page.setting.account_setting

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.UserProfileRequest
import com.example.hitproduct.data.model.user_profile.User
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AccountSettingViewmodel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _updateState = MutableLiveData<UiState<String>>(UiState.Idle)
    val updateState: LiveData<UiState<String>> = _updateState

    private val _userProfileState = MutableLiveData<UiState<User>>(UiState.Idle)
    val userProfileState: LiveData<UiState<User>> = _userProfileState

    fun updateProfile(
        firstName: String?,
        lastName: String?,
        nickname: String?,
        gender: String?,
        dateOfBirth: String?,
        avatarUri: Uri?,
        context: Context
    ) = viewModelScope.launch {
        _updateState.value = UiState.Loading

        val fields: Map<String, RequestBody> = UserProfileRequest.prepareFields(
            firstName, lastName, nickname, gender, dateOfBirth
        )
        // 2. Chuẩn bị avatar part (nếu có)
        val avatarPart: MultipartBody.Part? = avatarUri
            ?.let { UserProfileRequest.prepareAvatarPart(it, context) }

        // 3. Gọi repository
        when (val result = authRepository.editProfile(fields, avatarPart)) {
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

    fun fetchUserProfile() = viewModelScope.launch {
        _userProfileState.value = UiState.Loading
        when (val result = authRepository.fetchProfile(token = authRepository.getAccessToken() ?: "")) {
            is DataResult.Success ->
                _userProfileState.value = UiState.Success(result.data)
            is DataResult.Error   ->
                _userProfileState.value = UiState.Error(result.error)
        }
    }

}