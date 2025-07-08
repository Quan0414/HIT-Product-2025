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
import com.example.hitproduct.data.model.auth.response.SetupProfileResponse
import com.example.hitproduct.data.model.auth.response.RegisterResponse
import com.example.hitproduct.data.model.check_couple.CheckCoupleData
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.model.user_profile.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

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
    suspend fun setupProfile(
        fields: Map<String, RequestBody>,
    ): DataResult<SetupProfileResponse> {
        // getResult ở đây trả về DataResult<ApiResponse<EditProfileResponse>>
        val result = getResult { api.setupProfile(fields) }

        return when (result) {
            is DataResult.Success -> {
                DataResult.Success(result.data.data)
            }
            is DataResult.Error -> result
        }
    }

    // AuthRepository.kt
    suspend fun checkInvite(token: String): DataResult<InviteData> {
        return when (val result = getResult {
            api.checkInvite("Bearer $token")
        }) {
            is DataResult.Success -> {
                // result.data: ApiResponse<InviteData>
                DataResult.Success(result.data.data)   // now InviteData
            }
            is DataResult.Error -> result
        }
    }


    suspend fun checkCouple(token: String): DataResult<CheckCoupleData> {
        return when (val result = getResult {
            api.checkCouple("Bearer $token")
        }) {
            is DataResult.Success ->
                // result.data: ApiResponse<CheckCoupleData>
                DataResult.Success(result.data.data)
            is DataResult.Error -> result
        }
    }

    suspend fun editProfile(
        fields: Map<String, RequestBody>,
        avatar: MultipartBody.Part?
    ): DataResult<UserProfileResponse> {
        val result = getResult { api.editProfile(fields, avatar) }
        return when (result) {
            is DataResult.Success -> DataResult.Success(result.data.data)
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
