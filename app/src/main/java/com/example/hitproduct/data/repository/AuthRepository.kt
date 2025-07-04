package com.example.hitproduct.data.repository

import android.content.SharedPreferences
import com.example.hitproduct.base.BaseRepository
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.model.auth.request.LoginRequest
import com.example.hitproduct.data.model.auth.request.RegisterRequest
import com.example.hitproduct.data.model.auth.request.SendOtpRequest
import com.example.hitproduct.data.model.auth.request.VerifyCodeRequest
import com.example.hitproduct.data.model.auth.response.EditProfileResponse
import com.example.hitproduct.data.model.auth.response.RegisterResponse
import com.example.hitproduct.data.model.common.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

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
            is DataResult.Success -> DataResult.Success(result.data.data)
            is DataResult.Error -> result
        }
    }

    suspend fun sendOtp(email: String): DataResult<String> {
        val result = getResult { api.sendOtp(SendOtpRequest(email)) }
        return when (result) {
            is DataResult.Success -> DataResult.Success(result.data.message)
            is DataResult.Error -> result
        }
    }


    suspend fun verifyCode(
        otp: String,
        email: String,
        type: String
    ): DataResult<String> {
        // gọi api, result.data.data là String token
        return when (val result =
            getResult { api.verifyCode(VerifyCodeRequest(otp, email, type)) }) {
            is DataResult.Success -> {
                val token = result.data.data
                if (type == "register") {
                    prefs.edit()
                        .putString(AuthPrefersConstants.ACCESS_TOKEN, token)
                        .apply()
                }
                DataResult.Success(token)    // <-- phải return token
            }

            is DataResult.Error -> result
        }
    }

    /**
     * Gọi API chỉnh sửa thông tin cá nhân, nếu thành công sẽ trả về EditProfileResponse
     * @param fields là Map<String, RequestBody> chứa các trường cần chỉnh sửa
     * @param avatar là MultipartBody.Part? chứa ảnh đại diện (có thể null)
     */
    suspend fun editProfile(
        fields: Map<String, RequestBody>,
        avatar: MultipartBody.Part?
    ): DataResult<EditProfileResponse> {
        // getResult ở đây trả về DataResult<ApiResponse<EditProfileResponse>>
        val result = getResult { api.editProfile(fields, avatar) }

        return when (result) {
            is DataResult.Success -> {
                // result.data là ApiResponse<EditProfileResponse>
                // result.data.data là payload EditProfileResponse
                DataResult.Success(result.data.data)
            }
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
