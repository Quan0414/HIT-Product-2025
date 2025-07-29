package com.example.hitproduct.screen.dialog.mission

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.mission.MissionResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class DialogMissionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _missions = MutableLiveData<UiState<ApiResponse<MissionResponse>>>(UiState.Idle)
    val missions: LiveData<UiState<ApiResponse<MissionResponse>>> = _missions

    fun fetchMissons() = viewModelScope.launch {
        _missions.value = UiState.Loading
        authRepository.getMissions().let { result ->
            when (result) {
                is DataResult.Success -> {
                    _missions.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _missions.value = UiState.Error(result.error)
                }
            }
        }
    }
}