package com.example.hitproduct.screen.home_page.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.couple.CoupleProfile
import com.example.hitproduct.data.model.daily_question.get_question.DailyQuestionResponse
import com.example.hitproduct.data.model.pet.Pet
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.socket.SocketManager
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _coupleProfile = MutableLiveData<UiState<CoupleProfile>>(UiState.Idle)
    val coupleProfile: LiveData<UiState<CoupleProfile>> = _coupleProfile

    private val _pet = MutableLiveData<UiState<Pet>>(UiState.Idle)
    val pet: LiveData<UiState<Pet>> = _pet

    private val _hunger = MutableLiveData<Int>()
    val hunger: LiveData<Int> get() = _hunger

    private val _happiness = MutableLiveData<Int>()
    val happiness: LiveData<Int> get() = _happiness

    private val _coin = MutableLiveData<Int>()
    val coin: LiveData<Int> get() = _coin

    private val _activeKey = MutableLiveData<String>()
    val activeKey: LiveData<String> get() = _activeKey

    private val _eatEvent = MutableLiveData<Unit>()
    val eatEvent: LiveData<Unit> = _eatEvent

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

    init {
        listenToSocket()
    }

    private fun listenToSocket() {
        // Khi pet active
        SocketManager.onListenForPetActive { data ->
            val key = data.optString("active")
            _activeKey.postValue(key)
        }
        // Khi feed pet thành công
        SocketManager.onFeedPetSuccess { data ->
            _hunger.postValue(data.optInt("hunger"))
            _happiness.postValue(data.optInt("happiness"))
            _coin.postValue(data.optInt("coin"))

            _eatEvent.postValue(Unit)
        }
        // Khi decrease hunger
        SocketManager.onDecreaseHunger { data ->
            _hunger.postValue(data.optInt("hunger"))
        }

        SocketManager.onMissionCompleted { data ->
            _coin.postValue(data.optInt("coin"))
        }
    }


}