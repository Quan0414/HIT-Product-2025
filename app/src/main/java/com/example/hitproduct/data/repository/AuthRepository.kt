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


    suspend fun login(email: String, password: String) =
        getResult {
            val response = api.login(LoginRequest(email, password))
            if (response.success) {
                prefs.edit().putString(AuthPrefersConstants.ACCESS_TOKEN, response.data.token)
                    .apply()
            }
            response
        }


}