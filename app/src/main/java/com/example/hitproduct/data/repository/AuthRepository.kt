package com.example.hitproduct.data.repository

import android.content.SharedPreferences
import com.example.hitproduct.base.BaseRepository
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.model.auth.request.FindAccRequest
import com.example.hitproduct.data.model.auth.request.LoginRequest
import com.example.hitproduct.data.model.auth.request.RegisterRequest
import com.example.hitproduct.data.model.auth.request.SendOtpRequest
import com.example.hitproduct.data.model.auth.request.SendPublicKeyRequest
import com.example.hitproduct.data.model.auth.request.VerifyCodeRequest
import com.example.hitproduct.data.model.auth.response.FindAccResponse
import com.example.hitproduct.data.model.auth.response.RegisterResponse
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
import com.example.hitproduct.data.model.couple.CoupleProfile
import com.example.hitproduct.data.model.daily_question.get_question.DailyQuestionResponse
import com.example.hitproduct.data.model.daily_question.post_answer.SaveAnswerRequest
import com.example.hitproduct.data.model.daily_question.post_answer.SaveAnswerResponse
import com.example.hitproduct.data.model.daily_question.see_my_love_answer.GetYourLoveAnswerResponse
import com.example.hitproduct.data.model.food.Food
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.model.message.ChatItem
import com.example.hitproduct.data.model.mission.MissionResponse
import com.example.hitproduct.data.model.notification.NotificationResponse
import com.example.hitproduct.data.model.pet.FeedPetData
import com.example.hitproduct.data.model.pet.FeedPetRequest
import com.example.hitproduct.data.model.pet.Pet
import com.example.hitproduct.data.model.user_profile.User
import com.example.hitproduct.data.model.user_profile.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.security.PrivateKey

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

    suspend fun findAccount(
        email: String
    ): DataResult<ApiResponse<FindAccResponse>> {
        return when (val result = getResult { api.findAcc(FindAccRequest(email)) }) {
            is DataResult.Success -> DataResult.Success(result.data)
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

    suspend fun verifyCode2(
        otp: String,
        email: String,
        type: String
    ): DataResult<String> {
        return when (val result = getResult {
            api.verifyCode2(VerifyCodeRequest(otp, email, type))
        }) {
            is DataResult.Success -> {
                val token = result.data.data.token
//                if (type == "forgot-password") {
//                    prefs.edit()
//                        .putString(AuthPrefersConstants.ACCESS_TOKEN, token)
//                        .apply()
//                }
                DataResult.Success(token)
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
    suspend fun checkInvite(): DataResult<InviteData> {
        return when (val result = getResult {
            api.checkInvite()
        }) {
            is DataResult.Success -> {
                // result.data: ApiResponse<InviteData>
                DataResult.Success(result.data.data)   // now InviteData
            }

            is DataResult.Error -> result
        }
    }


    suspend fun fetchProfile(): DataResult<User> {
        return when (val result = getResult {
            api.getProfile()
        }) {
            is DataResult.Success ->
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

    suspend fun disconnect(): DataResult<ApiResponse<String>> {
        return when (val result = getResult { api.disconnectCouple() }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun chooseStartDate(loveStartedAt: String): DataResult<ApiResponse<CoupleData>> {
        return when (val result = getResult {
            api.chooseStartDate(ChooseStartDateRequest(loveStartedAt))
        }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun getCouple(): DataResult<CoupleProfile> {
        return when (val res = getResult { api.getCouple() }) {
            is DataResult.Success -> DataResult.Success(res.data.data.couple)
            is DataResult.Error -> res
        }
    }

    suspend fun getPet(): DataResult<Pet> {
        return when (val res = getResult { api.getPet() }) {
            is DataResult.Success -> DataResult.Success(res.data.data.pet)
            is DataResult.Error -> res
        }
    }

    suspend fun getAllFoods(): DataResult<List<Food>> {
        val all = mutableListOf<Food>()
        var page = 1
        var totalPages = 1  // khởi tạo tạm
        while (page <= totalPages) {
            when (val res = getResult { api.getFood(page) }) {
                is DataResult.Success -> {
                    val body = res.data.data
                    all += body.foods                  // gom dữ liệu
                    totalPages = body.totalPages       // cập nhật tổng số trang
                    page++                             // chuyển trang
                }

                is DataResult.Error -> {
                    return res
                }
            }
        }
        return DataResult.Success(all)
    }

    suspend fun feedPet(foodId: String): DataResult<ApiResponse<FeedPetData>> {
        return when (val result = getResult {
            api.feedPet(FeedPetRequest(foodId))
        }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun getDailyQuestion(): DataResult<ApiResponse<DailyQuestionResponse>> {
        return when (val result = getResult { api.getDailyQuestion() }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun saveDailyQuestion(
        answer: String
    ): DataResult<ApiResponse<SaveAnswerResponse>> {
        return when (val result = getResult {
            api.saveAnswerDailyQuestion(
                SaveAnswerRequest(answer)
            )
        }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun getYourLoveAnswer(): DataResult<ApiResponse<GetYourLoveAnswerResponse>> {
        return when (val result = getResult {
            api.getYourLoveDailyQuestion()
        }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun fetchNote(): DataResult<ApiResponse<GetNoteResponse>> {
        return when (val result = getResult { api.getNotes() }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun createNote(
        content: String,
        date: String
    ): DataResult<ApiResponse<NewNoteResponse>> {
        return when (val result = getResult { api.createNote(NewNoteRequest(content, date)) }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun deleteNote(noteId: String): DataResult<ApiResponse<String>> {
        return when (val result = getResult { api.deleteNote(noteId) }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun editNote(
        noteId: String,
        content: String
    ): DataResult<ApiResponse<EditNoteResponse>> {
        return when (val result = getResult { api.editNote(noteId, EditNoteRequest(content)) }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun getNotification(): DataResult<ApiResponse<NotificationResponse>> {
        return when (val result = getResult { api.getNotifications() }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }

    suspend fun getMissions(): DataResult<ApiResponse<MissionResponse>> {
        return when (val result = getResult { api.getMissions() }) {
            is DataResult.Success -> DataResult.Success(result.data)
            is DataResult.Error -> result
        }
    }


    companion object {
        private const val PAGE_SIZE = 20
    }

    /**
     * Trả về danh sách ChatItem đã biết fromMe
     */
    suspend fun getMessages(
        roomChatId: String,
        before: String? = null
    ): DataResult<List<ChatItem>> {
        return when (val result = getResult {
            api.getMessages(roomChatId, PAGE_SIZE, before)
        }) {
            is DataResult.Success -> {
                val resp = result.data.data
                val myUserId = prefs.getString(AuthPrefersConstants.MY_USER_ID, "") ?: ""

                val items = resp.messages.map { dto ->
                    // dto.senderId bây giờ là SenderDto, không phải String
                    val sender = dto.senderId
                    val fromMe = sender.id == myUserId
                    val avatarUrl = if (!fromMe) sender.avatar else null

                    if (dto.images.isNotEmpty()) {
                        ChatItem.ImageMessage(
                            id = dto.id,
                            senderId = sender.id,
                            imageUrl = dto.images.first(),
                            sentAt = dto.sentAt,
                            fromMe = fromMe,
                            avatarUrl = avatarUrl ?: "",
                        )
                    } else {
                        ChatItem.TextMessage(
                            id = dto.id,
                            senderId = sender.id,
                            text = dto.content,
                            sentAt = dto.sentAt,
                            fromMe = fromMe,
                            avatarUrl = avatarUrl ?: "",
                        )
                    }
                }

                DataResult.Success(items)
            }

            is DataResult.Error -> result
        }
    }


    suspend fun sendPublicKey(
        publicKey: String,
        privateKey: String
    ): DataResult<String> {
        return when (val result =
            getResult { api.sendPublicKey(SendPublicKeyRequest(publicKey, privateKey)) }) {
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

    fun getMyUserId(): String =
        prefs.getString(AuthPrefersConstants.MY_USER_ID, "") ?: ""

    fun getMyLoveId(): String =
        prefs.getString(AuthPrefersConstants.MY_LOVE_ID, "") ?: ""

}
