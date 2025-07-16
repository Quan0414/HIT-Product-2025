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

    // User
    const val EDIT_PROFILE = "api/v1/auth/profile"
    const val USER_PROFILE = "api/v1/auth/profile"
    const val CHECK_INVITE = "api/v1/couple/connect"
    const val DISCONNECT_COUPLE = "api/v1/couple/disconnect"


    // HOME
    const val GET_COUPLE = "api/v1/couple"
    const val GET_PET = "api/v1/pets"
    const val GET_FOOD = "api/v1/foods"
    const val FEED_PET = "api/v1/pets"


}