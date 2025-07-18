package com.example.hitproduct.data.api

import com.example.hitproduct.common.constants.ApiConstants
import com.example.hitproduct.data.model.auth.request.LoginRequest
import com.example.hitproduct.data.model.auth.request.RegisterRequest
import com.example.hitproduct.data.model.auth.request.SendOtpRequest
import com.example.hitproduct.data.model.auth.request.VerifyCodeRequest
import com.example.hitproduct.data.model.auth.response.RegisterResponse
import com.example.hitproduct.data.model.auth.response.SendOtpResponse
import com.example.hitproduct.data.model.auth.response.SetupProfileResponse
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.couple.CoupleData
import com.example.hitproduct.data.model.daily_question.get_question.DailyQuestionResponse
import com.example.hitproduct.data.model.daily_question.post_answer.SaveAnswerRequest
import com.example.hitproduct.data.model.daily_question.post_answer.SaveAnswerResponse
import com.example.hitproduct.data.model.daily_question.see_my_love_answer.GetYourLoveAnswerResponse
import com.example.hitproduct.data.model.food.FoodData
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.model.pet.FeedPetData
import com.example.hitproduct.data.model.pet.FeedPetRequest
import com.example.hitproduct.data.model.pet.PetData
import com.example.hitproduct.data.model.user_profile.User
import com.example.hitproduct.data.model.user_profile.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

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
    ): Response<ApiResponse<String>>

    @Multipart
    @POST(ApiConstants.SETUP_PROFILE)
    suspend fun setupProfile(
        @PartMap
        fields: @JvmSuppressWildcards Map<String, RequestBody>,
    ): Response<ApiResponse<SetupProfileResponse>>

    @Multipart
    @POST(ApiConstants.EDIT_PROFILE)
    suspend fun editProfile(
        @PartMap
        fields: @JvmSuppressWildcards Map<String, RequestBody>,
        @Part avatar: MultipartBody.Part?
    ): Response<ApiResponse<UserProfileResponse>>


    @GET(ApiConstants.CHECK_INVITE)
    suspend fun checkInvite(
        @Header("Authorization") token: String
    ): Response<ApiResponse<InviteData>>

    @GET(ApiConstants.USER_PROFILE)
    suspend fun getProfile(
        @Header("Authorization") bearerToken: String
    ): Response<ApiResponse<User>>

    @DELETE(ApiConstants.DISCONNECT_COUPLE)
    suspend fun disconnectCouple(
    ): Response<ApiResponse<String>>

    @GET(ApiConstants.GET_COUPLE)
    suspend fun getCouple(
    ): Response<ApiResponse<CoupleData>>

    @GET(ApiConstants.GET_PET)
    suspend fun getPet(
    ): Response<ApiResponse<PetData>>

    @GET(ApiConstants.GET_FOOD)
    suspend fun getFood(
        @Query("page") page: Int
    ): Response<ApiResponse<FoodData>>

    @POST(ApiConstants.FEED_PET)
    suspend fun feedPet(
        @Body request: FeedPetRequest
    ): Response<ApiResponse<FeedPetData>>

    @GET(ApiConstants.GET_DAILY_QUESTION)
    suspend fun getDailyQuestion(
    ): Response<ApiResponse<DailyQuestionResponse>>

    @POST(ApiConstants.SAVE_ANSWER_DAILY_QUESTION)
    suspend fun saveAnswerDailyQuestion(
        @Body request: SaveAnswerRequest
    ): Response<ApiResponse<SaveAnswerResponse>>

    @GET(ApiConstants.GET_YOUR_LOVE_DAILY_QUESTION)
    suspend fun getYourLoveDailyQuestion(
    ): Response<ApiResponse<GetYourLoveAnswerResponse>>

}