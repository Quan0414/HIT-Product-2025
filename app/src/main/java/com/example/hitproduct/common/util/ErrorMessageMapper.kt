package com.example.hitproduct.common.util

data class MappedError(
    val message: String,
    val emailError: Boolean = false,
    val passwordError: Boolean = false
)

object ErrorMessageMapper {
    fun fromBackend(raw: String): MappedError {
        val emailError = raw.contains("\"email\" must be a valid email", ignoreCase = true)
        val passwordError = raw.contains("\"password\" length must be at least 6 characters long", ignoreCase = true)

        val message = when {
            emailError && passwordError ->
                "Email không hợp lệ! Mật khẩu phải tối thiểu 6 ký tự!"

            emailError ->
                "Email không hợp lệ!"

            passwordError ->
                "Mật khẩu phải tối thiểu 6 ký tự!"

            else ->
                "App lỗi rồi, gọi Huy Hoàng đi!"
        }

        return MappedError(message, emailError, passwordError)
    }
}