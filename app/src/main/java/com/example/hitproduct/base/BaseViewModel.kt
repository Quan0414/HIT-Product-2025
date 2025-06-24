package com.example.hitproduct.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val loading: MutableLiveData<Boolean> = MutableLiveData(false)
    protected val isLoading: LiveData<Boolean>
        get() = loading

    private val error: MutableLiveData<String> = MutableLiveData("")
    protected val hasError: LiveData<String>
        get() = error

    protected fun <T> executeTask(
        request: suspend CoroutineScope.() -> DataResult<T>,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit = {},
        showLoading: Boolean = true
    ) {
        if (showLoading) showLoading()
        viewModelScope.launch {
            when (val response = request(this)) {
                is DataResult.Success -> {
                    onSuccess(response.data)
                    hideLoading()
                }

                is DataResult.Error -> {
                    onError(response.exception)
                    hideLoading()
                }
            }
        }
    }


    private fun showLoading() {
        loading.value = true
    }

    private fun hideLoading() {
        loading.value = false
    }


}