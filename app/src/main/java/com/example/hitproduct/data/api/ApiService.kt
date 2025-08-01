package com.example.hitproduct.data.api

import com.example.hitproduct.common.constants.ApiConstants
import com.example.hitproduct.data.model.auth.request.FindAccRequest
import com.example.hitproduct.data.model.auth.request.LoginRequest
import com.example.hitproduct.data.model.auth.request.RegisterRequest
import com.example.hitproduct.data.model.auth.request.SendOtpRequest
import com.example.hitproduct.data.model.auth.request.VerifyCodeRequest
import com.example.hitproduct.data.model.auth.response.FindAccResponse
import com.example.hitproduct.data.model.auth.response.RegisterResponse
import com.example.hitproduct.data.model.auth.response.SendOtpResponse
import com.example.hitproduct.data.model.auth.response.SetupProfileResponse
import com.example.hitproduct.data.model.auth.response.VerifyCodeResponse
import com.example.hitproduct.data.model.calendar.request.EditNoteRequest
import com.example.hitproduct.data.model.calendar.request.NewNoteRequest
import com.example.hitproduct.data.model.calendar.response.EditNoteResponse
import com.example.hitproduct.data.model.calendar.response.GetNoteResponse
import com.example.hitproduct.data.model.calendar.response.NewNoteResponse
import com.example.hitproduct.data.model.common.ApiResponse
import com.example.hitproduct.data.model.couple.ChooseStartDateRequest
import com.example.hitproduct.data.model.couple.CoupleData
import com.example.hitproduct.data.model.daily_question.get_question.DailyQuestionResponse
import com.example.hitproduct.data.model.daily_question.post_answer.SaveAnswerRequest
import com.example.hitproduct.data.model.daily_question.post_answer.SaveAnswerResponse
import com.example.hitproduct.data.model.daily_question.see_my_love_answer.GetYourLoveAnswerResponse
import com.example.hitproduct.data.model.food.FoodData
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.model.message.MessageResponse
import com.example.hitproduct.data.model.mission.MissionResponse
import com.example.hitproduct.data.model.notification.NotificationResponse
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
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
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

    // Forgot method
    @POST(ApiConstants.AUTH_FORGOT_PASSWORD)
    suspend fun findAcc(
        @Body request: FindAccRequest
    ): Response<ApiResponse<FindAccResponse>>

    @POST(ApiConstants.AUTH_VERIFY_CODE)
    suspend fun verifyCode2(
        @Body request: VerifyCodeRequest
    ): Response<ApiResponse<VerifyCodeResponse>>

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
    ): Response<ApiResponse<InviteData>>

    @GET(ApiConstants.USER_PROFILE)
    suspend fun getProfile(
//        @Header("Authorization") bearerToken: String
    ): Response<ApiResponse<User>>

    @DELETE(ApiConstants.DISCONNECT_COUPLE)
    suspend fun disconnectCouple(
    ): Response<ApiResponse<String>>

    @PATCH(ApiConstants.CHOOSE_START_DATE)
    suspend fun chooseStartDate(
        @Body request: ChooseStartDateRequest
    ): Response<ApiResponse<CoupleData>>

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

    @GET(ApiConstants.GET_NOTES)
    suspend fun getNotes(
    ): Response<ApiResponse<GetNoteResponse>>

    @POST(ApiConstants.CREATE_NOTE)
    suspend fun createNote(
        @Body request: NewNoteRequest
    ): Response<ApiResponse<NewNoteResponse>>

    @DELETE(ApiConstants.DELETE_NOTE)
    suspend fun deleteNote(
        @Path("id") noteId: String
    ): Response<ApiResponse<String>>

    @PUT(ApiConstants.EDIT_NOTE)
    suspend fun editNote(
        @Path("id") noteId: String,
        @Body request: EditNoteRequest
    ): Response<ApiResponse<EditNoteResponse>>

    @GET(ApiConstants.GET_NOTIFICATIONS)
    suspend fun getNotifications(
    ): Response<ApiResponse<NotificationResponse>>

    @GET(ApiConstants.GET_MISSIONS)
    suspend fun getMissions(
    ): Response<ApiResponse<MissionResponse>>

    @GET(ApiConstants.GET_MESSAGE)
    suspend fun getMessages(
        @Path("roomChatId") roomChatId: String,
        @Query("limit") limit: Int,
        @Query("before") before: String? = null
    ): Response<ApiResponse<MessageResponse>>
}