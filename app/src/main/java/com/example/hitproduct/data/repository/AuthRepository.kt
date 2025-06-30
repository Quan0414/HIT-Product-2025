package com.example.hitproduct.data.repository

import android.content.SharedPreferences
import com.example.hitproduct.base.BaseRepository
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.model.auth.LoginRequest

class AuthRepository(
    private val api: ApiService,
    private val prefs: SharedPreferences
) : BaseRepository() {

    /**
     * Gọi API login, nếu statusCode == 200 thì tự động lưu token vào SharedPreferences
     */
    suspend fun login(email: String, password: String) =
        getResult {
            // gọi API, giờ trả về Response<ApiResponse<LoginResponse>>
            val response = api.login(LoginRequest(email, password))

            // nếu HTTP 2xx và statusCode == 200 thì lưu token
            if (response.isSuccessful) {
                response.body()?.let { apiRes ->
                    if (apiRes.statusCode == 200) {
                        val token = apiRes.data.token
                        prefs.edit()
                            .putString(AuthPrefersConstants.ACCESS_TOKEN, token)
                            .apply()

                    }
                }
            }

            // trả về nguyên response để BaseRepository xử lý tiếp
            response
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
