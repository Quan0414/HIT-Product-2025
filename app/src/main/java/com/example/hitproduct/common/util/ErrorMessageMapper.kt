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

        val tokenExpired = raw.contains("token hết hạn!", ignoreCase = true)

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

        val duplicateOldPassword =
            raw.contains("Vui lòng không sử dụng mật khẩu trước đó.", ignoreCase = true)

        val otpError = raw.contains("Nhập mã OTP sai.", ignoreCase = true)
        val otpExprired =
            raw.contains("OTP hết hạn.", ignoreCase = true)

        val petFullHungry =
            raw.contains("Pet của bạn đã no.", ignoreCase = true)

        val notEnoughCoin =
            raw.contains("Bạn không có đủ tiền.", ignoreCase = true)

        val answer_question_error1 =
            raw.contains("Câu trả lời không được để trống.", ignoreCase = true)
        val answer_question_error2 =
            raw.contains("Bạn đã trả lời rồi.", ignoreCase = true)
        val answer_question_error3 =
            raw.contains(
                "Bạn cần trả lời câu hỏi trước khi xem câu trả lời của cậu ấy.",
                ignoreCase = true
            )

        val emtyNote = raw.contains("Nội dung ghi chú không được để trống.", ignoreCase = true)


        val message = when {
            tokenExpired ->
                "Phiên đăng nhập đã kết thúc, mời bạn đăng nhập lại."

            unauthorized ->
                "Bạn chưa được cấp quyền, hãy kiểm tra email xác thực."

            accountExits ->
                "Email hoặc tên người dùng đã tồn tại, chọn cái khác nha."

            emailError ->
                "Email không đúng định dạng, mời bạn kiểm tra lại."

            passwordError ->
                "Mật khẩu phải có ít nhất 6 ký tự."

            confirmPasswordError ->
                "Mật khẩu xác nhận chưa khớp, bạn kiểm tra lại giúp mình."

            wrongAccount ->
                "Email hoặc mật khẩu không khớp, xin bạn thử lại."

            otpError ->
                "Mã OTP chưa chính xác, mời bạn kiểm tra lại."

            otpExprired ->
                "Mã OTP đã hết hạn, bạn có muốn gửi lại mã mới không?"

            duplicateOldPassword ->
                "Bạn vừa dùng lại mật khẩu cũ rồi, đổi mật khẩu mới nhé."

            petFullHungry ->
                "Pet của bạn đã no căng rồi, hãy cho nó đi chơi chút."

            notEnoughCoin ->
                "Bạn đang thiếu coin, tích thêm rồi quay lại tiếp tục nhé."

            answer_question_error1 ->
                "Câu trả lời đang để trống, mời bạn nhập nội dung."

            answer_question_error2 ->
                "Bạn đã trả lời rồi."

            answer_question_error3 ->
                "Trả lời câu hỏi trước rồi mới xem đáp án của người ấy được."

            emtyNote ->
                "Ghi chú đang trống, bạn viết vài dòng vào nhé."

            else ->
                "Đã có lỗi xảy ra, vui lòng thử lại sau."
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