package com.example.hitproduct.screen.dialog.daily_question.your

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.daily_question.get_question.DailyQuestionResponse
import com.example.hitproduct.data.model.daily_question.post_answer.SaveAnswerResponse
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class YourDailyQuestionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _dailyQuestion = MutableLiveData<UiState<DailyQuestionResponse>>(UiState.Idle)
    val dailyQuestion: LiveData<UiState<DailyQuestionResponse>> = _dailyQuestion

    private val _saveDailyQuestion = MutableLiveData<UiState<ApiResponse<SaveAnswerResponse>>>(UiState.Idle)
    val saveDailyQuestion: LiveData<UiState<ApiResponse<SaveAnswerResponse>>> = _saveDailyQuestion



    fun fetchDailyQuestion() {
        viewModelScope.launch {
            _dailyQuestion.value = UiState.Loading
            when (val result = authRepository.getDailyQuestion()) {
                is DataResult.Success -> {
                    _dailyQuestion.value = UiState.Success(result.data.data)
                }

                is DataResult.Error -> {
                    _dailyQuestion.value = UiState.Error(result.error)
                }
            }

        }
    }

    fun saveDailyQuestion(answer: String) {
        viewModelScope.launch {
            _saveDailyQuestion.value = UiState.Loading
            when (val result = authRepository.saveDailyQuestion(answer)) {
                is DataResult.Success -> {
                    _saveDailyQuestion.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _saveDailyQuestion.value = UiState.Error(result.error)
                }
            }
        }
    }

    fun resetSaveQuestionState() {
        _saveDailyQuestion.value = UiState.Idle
    }
}