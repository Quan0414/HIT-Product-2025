package com.example.hitproduct.base

import com.example.hitproduct.data.model.response.ApiResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

open class BaseRepository {
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> getResult(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        request: suspend CoroutineScope.() -> Response<ApiResponse<T>>
    ): DataResult<ApiResponse<T>> {
        return withContext(dispatcher) {
            try {
                val resp = request()
                val body: ApiResponse<T> = if (resp.isSuccessful) {
                    resp.body()!!
                } else {
                    // parse error‐body về đúng ApiResponse<T>
                    val errJson = resp.errorBody()?.string().orEmpty()
                    val type = object : TypeToken<ApiResponse<T>>() {}.type
                    Gson().fromJson<ApiResponse<T>>(errJson, type)
                }
                DataResult.Success(body)

            } catch (io: IOException) {
                DataResult.Error(io)
            } catch (http: HttpException) {
                // nếu Retrofit ném HttpException (4xx/5xx), parse tương tự
                val errJson = http.response()?.errorBody()?.string().orEmpty()
                val type = object : TypeToken<ApiResponse<T>>() {}.type
                val apiErr = Gson().fromJson<ApiResponse<T>>(errJson, type)
                DataResult.Success(apiErr)
            } catch (e: Exception) {
                DataResult.Error(e)
            }
        }
    }
}
