package com.example.hitproduct.screen.dialog.start_date

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class DialogStartDateViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _chooseStartDateState = MutableLiveData<UiState<String>>()
    val chooseStartDateState: LiveData<UiState<String>> = _chooseStartDateState

    fun chooseStartDate(startDate: String) {
        _chooseStartDateState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.chooseStartDate(startDate)) {
                is DataResult.Error -> {
                    _chooseStartDateState.value = UiState.Error(result.error)
                }

                is DataResult.Success -> {
                    _chooseStartDateState.value = UiState.Success(result.data.message)
                }

            }
        }
    }

    fun resetChooseStartDateState() {
        _chooseStartDateState.value = UiState.Idle
    }
}