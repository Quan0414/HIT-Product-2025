package com.example.hitproduct.data.api

import com.example.hitproduct.common.constants.ApiConstants
import com.example.hitproduct.data.model.auth.request.LoginRequest
import com.example.hitproduct.data.model.auth.request.RegisterRequest
import com.example.hitproduct.data.model.auth.request.SendOtpRequest
import com.example.hitproduct.data.model.auth.request.VerifyCodeRequest
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.auth.response.RegisterResponse
import com.example.hitproduct.data.model.auth.response.SendOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(ApiConstants.AUTH_LOGIN)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<String>>

    @POST(ApiConstants.AUTH_REGISTER)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse>>

    @POST(ApiConstants.AUTH_SEND_OTP)
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<ApiResponse<SendOtpResponse>>

    @POST(ApiConstants.AUTH_VERIFY_CODE)
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Response<ApiResponse<Unit>>


}