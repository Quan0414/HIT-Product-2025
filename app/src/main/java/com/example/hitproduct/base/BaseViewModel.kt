package com.example.hitproduct.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.common.util.MappedError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val loading = MutableLiveData(false)
    protected val isLoading: LiveData<Boolean> get() = loading

    private val error = MutableLiveData<MappedError>()
    protected val hasError: LiveData<MappedError> get() = error

    protected fun <T> executeTask(
        request: suspend CoroutineScope.() -> DataResult<T>,
        onSuccess: (T) -> Unit,
        onError: (MappedError) -> Unit = {},
        showLoading: Boolean = true
    ) {
        if (showLoading) loading.value = true
        viewModelScope.launch {
            when (val response = request(this)) {
                is DataResult.Success -> {
                    onSuccess(response.data)
                }
                is DataResult.Error -> {
                    // dùng response.error
                    onError(response.error)
                    error.value = response.error
                }
            }
            loading.value = false
        }
    }
}
