package com.example.hitproduct.screen.home_page.setting.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.user_profile.User
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SettingViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _userProfileState = MutableLiveData<UiState<User>>(UiState.Idle)
    val userProfileState: LiveData<UiState<User>> = _userProfileState

    private val _disconnectState = MutableLiveData<UiState<String>>(UiState.Idle)
    val disconnectState: LiveData<UiState<String>> = _disconnectState

    fun fetchUserProfile() = viewModelScope.launch {
        _userProfileState.value = UiState.Loading
        when (val result =
            authRepository.fetchProfile()) {
            is DataResult.Success ->
                _userProfileState.value = UiState.Success(result.data)

            is DataResult.Error ->
                _userProfileState.value = UiState.Error(result.error)
        }
    }

    fun disconnectCouple() = viewModelScope.launch {
        _disconnectState.value = UiState.Loading
        when (val result = authRepository.disconnect()) {
            is DataResult.Error -> {
                _disconnectState.value = UiState.Error(result.error)
            }
            is DataResult.Success -> {
                _disconnectState.value = UiState.Success(result.data.message)
            }
        }
    }
}