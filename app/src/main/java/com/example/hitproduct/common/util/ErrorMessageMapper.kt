package com.example.hitproduct.common.util

data class MappedError(
    val message: String,
    val accountExits: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val confirmPasswordError: Boolean = false,
    val otp: Boolean = false
)

object ErrorMessageMapper {
    fun fromBackend(raw: String): MappedError {


        val emailError = raw.contains("must be a valid email", ignoreCase = true)
        val passwordError = raw.contains(
            "length must be at least 6 characters long",
            ignoreCase = true
        )
        val confirmPasswordError =
            raw.contains("Mật khẩu nhập lại không khớp với mật khẩu.", ignoreCase = true)

        val accountExits = raw.contains("tài khoản đã tồn tại.", ignoreCase = true)

        //401
        val wrongAccount = raw.contains("Email hoặc Password không chính xác.", ignoreCase = true)
        val unauthorized = raw.contains("Tài khoản chưa được xác nhận.", ignoreCase = true)

        val otpError = raw.contains("Nhập mã OTP sai.", ignoreCase = true)
        val otpExprired =
            raw.contains("OTP hết hạn.", ignoreCase = true)

        val petFullHUngry =
            raw.contains("Pet của bạn đã no.", ignoreCase = true)

        val message = when {

            unauthorized ->
                "Tài khoản chưa được xác nhận!"

            accountExits ->
                "Tên người dùng hoặc email đã tồn tại!"

            emailError ->
                "Email không hợp lệ!"

            passwordError ->
                "Mật khẩu phải tối thiểu 6 ký tự!"

            confirmPasswordError ->
                "Mật khẩu không khớp!"

            wrongAccount ->
                "Email hoặc mật khẩu không chính xác!"

            otpError ->
                "Mã OTP không chính xác!"

            otpExprired ->
                "Mã OTP hết hạn!"

            petFullHUngry ->
                "Pet của bạn đã no, không thể ăn nữa!"

            else ->
                "Lỗi chưa xác định, bật logcat lên!"
        }

        val emailErrorFlag = emailError || wrongAccount
        val passwordErrorFlag = passwordError || wrongAccount
        val otpFlag = otpError || otpExprired

        return MappedError(
            accountExits = accountExits,
            message = message,
            emailError = emailErrorFlag,
            passwordError = passwordErrorFlag,
            confirmPasswordError = confirmPasswordError,
            otp = otpFlag
        )
    }
}