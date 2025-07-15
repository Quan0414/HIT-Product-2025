package com.example.hitproduct.screen.dialog.shop

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.food.Food
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ShopViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _foodListState = MutableLiveData<UiState<List<Food>>>(UiState.Idle)
    val foodListState: MutableLiveData<UiState<List<Food>>> = _foodListState

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
}