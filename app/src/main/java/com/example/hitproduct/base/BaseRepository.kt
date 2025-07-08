package com.example.hitproduct.base

import com.example.hitproduct.common.util.ErrorMessageMapper
import com.example.hitproduct.common.util.MappedError
import com.example.hitproduct.data.model.common.ApiResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

open class BaseRepository {
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> getResult(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        request: suspend CoroutineScope.() -> Response<ApiResponse<T>>
    ): DataResult<ApiResponse<T>> = withContext(dispatcher) {
        try {
            val resp = request()
            if (resp.isSuccessful) {
                val body = resp.body()!!
                return@withContext if (body.success) {
                    // Trả về nguyên ApiResponse<T>
                    DataResult.Success(body)
                } else {
                    val mapped = ErrorMessageMapper.fromBackend(body.message)
                    DataResult.Error(mapped)
                }
            } else {
                val raw = resp.errorBody()?.string().orEmpty()
                val mapped = ErrorMessageMapper.fromBackend(raw)
                return@withContext DataResult.Error(mapped)
            }
        } catch (e: Exception) {
            e.printStackTrace()                       // in stacktrace
            val msg = e.message ?: "Unknown exception"
            val mapped = MappedError("Lỗi: $msg")
            return@withContext DataResult.Error(mapped)
        }

    }
}
