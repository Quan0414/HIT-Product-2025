package com.example.hitproduct.data.repository

import android.content.SharedPreferences
import com.example.hitproduct.base.BaseRepository
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.model.auth.request.LoginRequest
import com.example.hitproduct.data.model.auth.request.RegisterRequest
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.auth.response.RegisterResponse

class AuthRepository(
    private val api: ApiService,
    private val prefs: SharedPreferences
) : BaseRepository() {

    /**
     * Gọi API login, nếu statusCode == 200 thì tự động lưu token vào SharedPreferences
     */
    suspend fun login(email: String, password: String): DataResult<ApiResponse<String>> =
        getResult { api.login(LoginRequest(email, password)) }
            .also { result ->
                if (result is DataResult.Success) {
                    // result.data.data chính là token String
                    val token = result.data.data
                    prefs.edit()
                        .putString(AuthPrefersConstants.ACCESS_TOKEN, token)
                        .apply()
                }
            }


    suspend fun register(
        username: String, email: String,
        password: String, repeatPassword: String
    ): DataResult<RegisterResponse> {
        val result =
            getResult { api.register(RegisterRequest(username, email, password, repeatPassword)) }
        return when (result) {
            is DataResult.Success -> DataResult.Success(result.data.data)   // unwrap .data thành RegisterResponse
            is DataResult.Error -> result
        }
    }


    /**
     * Lấy token đã lưu (hoặc null nếu chưa lưu)
     */
    fun getAccessToken(): String? =
        prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, null)

    /**
     * Xoá token khi logout hoặc khi cần refresh
     */
    fun clearAccessToken() {
        prefs.edit()
            .remove(AuthPrefersConstants.ACCESS_TOKEN)
            .apply()
    }
}
