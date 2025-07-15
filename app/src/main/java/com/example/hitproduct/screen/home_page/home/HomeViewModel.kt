package com.example.hitproduct.screen.home_page.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.CoupleProfile
import com.example.hitproduct.data.model.pet.Pet
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _coupleProfile = MutableLiveData<UiState<CoupleProfile>>(UiState.Idle)
    val coupleProfile: LiveData<UiState<CoupleProfile>> = _coupleProfile

    private val _pet = MutableLiveData<UiState<Pet>>(UiState.Idle)
    val pet: LiveData<UiState<Pet>> = _pet

    fun getCoupleProfile() {
        _coupleProfile.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getCouple()) {
                is DataResult.Success -> {
                    _coupleProfile.value = UiState.Success(result.data)
                }
                is DataResult.Error -> {
                    _coupleProfile.value = UiState.Error(result.error)
                }
            }
        }
    }

    fun getPet() {
        _pet.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getPet()) {
                is DataResult.Success -> {
                    _pet.value = UiState.Success(result.data)
                }
                is DataResult.Error -> {
                    _pet.value = UiState.Error(result.error)
                }
            }
        }
    }
}