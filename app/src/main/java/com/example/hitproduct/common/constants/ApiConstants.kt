package com.example.hitproduct.common.constants

object ApiConstants {

    // Base
    const val BASE_URL = "https://love-story-app.onrender.com/"

    // Auth
    const val AUTH_REGISTER = "api/v1/auth/register"
    const val AUTH_LOGIN = "api/v1/auth/login"
    const val AUTH_SEND_OTP = "api/v1/auth/send-otp"
    const val AUTH_VERIFY_CODE = "api/v1/auth/register/confirm-otp"
    const val SETUP_PROFILE = "api/v1/auth/profile"
    const val AUTH_FORGOT_PASSWORD = "api/v1/auth/forgot-password"
    const val AUTH_RESET_PASSWORD = "api/v1/auth/reset-password"
    const val AUTH_CHANGE_PASSWORD = "api/v1/auth/change-password"

    // User
    const val EDIT_PROFILE = "api/v1/auth/profile"
    const val USER_PROFILE = "api/v1/auth/profile"
    const val CHECK_INVITE = "api/v1/couple/connect"
    const val DISCONNECT_COUPLE = "api/v1/couple/disconnect"


    // HOME
    const val CHOOSE_START_DATE = "api/v1/couple"
    const val GET_COUPLE = "api/v1/couple"
    const val GET_PET = "api/v1/pet"
    const val GET_FOOD = "api/v1/food"
    const val FEED_PET = "api/v1/pet"
    const val GET_DAILY_QUESTION = "api/v1/question/daily"
    const val SAVE_ANSWER_DAILY_QUESTION = "api/v1/question/daily"
    const val GET_YOUR_LOVE_DAILY_QUESTION = "api/v1/question/daily/feedback"

    //NOTE
    const val GET_NOTES = "api/v1/notes"
    const val CREATE_NOTE = "api/v1/notes"
    const val DELETE_NOTE = "api/v1/notes/{id}"
    const val EDIT_NOTE = "api/v1/notes/{id}"

    // Notification
    const val GET_NOTIFICATIONS = "api/v1/notification"

    // Mission
    const val GET_MISSIONS = "api/v1/mission"

    // Mess
    const val GET_MESSAGE = "api/v1/chat/{roomChatId}"

}