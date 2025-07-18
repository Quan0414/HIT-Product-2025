package com.example.hitproduct.screen.dialog.daily_question.your_love

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.daily_question.see_my_love_answer.GetYourLoveAnswerResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class YourLoveAnswerViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _yourLoveAnswer = MutableLiveData<UiState<GetYourLoveAnswerResponse>>(UiState.Idle)
    val yourLoveAnswer: LiveData<UiState<GetYourLoveAnswerResponse>> = _yourLoveAnswer

    fun fetchYourLoveAnswer() {
        _yourLoveAnswer.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getYourLoveAnswer()) {
                is DataResult.Error -> {
                    _yourLoveAnswer.value = UiState.Error(result.error)
                }

                is DataResult.Success -> {
                    _yourLoveAnswer.value = UiState.Success(result.data.data)
                }
            }
        }
    }
}
