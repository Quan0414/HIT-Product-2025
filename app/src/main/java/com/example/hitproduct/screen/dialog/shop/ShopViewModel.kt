package com.example.hitproduct.screen.dialog.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.food.Food
import com.example.hitproduct.data.model.pet.FeedPetData
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ShopViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _foodListState = MutableLiveData<UiState<List<Food>>>(UiState.Idle)
    val foodListState: LiveData<UiState<List<Food>>> = _foodListState

    private val _feedPet = MutableLiveData<UiState<ApiResponse<FeedPetData>>>(UiState.Idle)
    val feedPet: LiveData<UiState<ApiResponse<FeedPetData>>> = _feedPet

    fun fetchFoodList() = viewModelScope.launch {
        _foodListState.value = UiState.Loading
        authRepository.getAllFoods().let { result ->
            when (result) {
                is DataResult.Success -> {
                    _foodListState.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _foodListState.value = UiState.Error(result.error)
                }
            }
        }
    }

    fun feedPet(foodId: String) {
        _feedPet.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.feedPet(foodId)) {
                is DataResult.Success -> {
                    _feedPet.value = UiState.Success(result.data)
                }

                is DataResult.Error -> {
                    _feedPet.value = UiState.Error(result.error)
                }
            }
        }
    }

    fun clearFeedPetState() {
        _feedPet.value = UiState.Idle
    }
}