package com.example.hitproduct.base

import com.example.hitproduct.common.util.MappedError

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val error: MappedError) : DataResult<Nothing>()
}