package com.example.hitproduct.common.constants

object ApiConstants {

    // Base
    const val BASE_URL = "https://love-story-app-1.onrender.com/"

    // Auth
    const val AUTH_REGISTER = "api/v1/auth/register"
    const val AUTH_LOGIN = "api/v1/auth/login"
    const val AUTH_SEND_OTP = "api/v1/auth/send-otp"
    const val AUTH_VERIFY_CODE = "api/v1/auth/register/confirm-otp"

    // User
    const val AUTH_EDIT_PROFILE = "api/v1/auth/profile"
    const val USER_PROFILE = "api/user/profile"
    const val USER_UPDATE = "api/user/update"
    const val CHECK_INVITE = "api/v1/couple/connect"

    // Events / Countdown
    const val EVENTS_LIST = "api/events"
    const val EVENT_CREATE = "api/events/create"
    const val EVENT_UPDATE = "api/events/update"
    const val EVENT_DELETE = "api/events/delete"

    // Share
    const val SHARE_GENERATE = "api/share/generate"

    // Headers
    const val HEADER_AUTH = "Authorization"
    const val HEADER_CONTENT_JSON = "Content-Type: application/json"

}