package com.example.hitproduct.screen.dialog.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.notification.Notification
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class DialogNotificationViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableLiveData<UiState<List<Notification>>>(UiState.Idle)
    val notifications: LiveData<UiState<List<Notification>>> = _notifications

    fun fetchNotifications() = viewModelScope.launch {
        _notifications.value = UiState.Loading
        authRepository.getNotification().let { result ->
            when (result) {
                is DataResult.Success -> {
                    _notifications.value = UiState.Success(result.data.data.nots)
                }

                is DataResult.Error -> {
                    _notifications.value = UiState.Error(result.error)
                }
            }
        }
    }
}