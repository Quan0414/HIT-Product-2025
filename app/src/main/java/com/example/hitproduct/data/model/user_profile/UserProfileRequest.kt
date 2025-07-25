package com.example.hitproduct.data.model.user_profile

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object UserProfileRequest {

    /**
     * Tạo các field text, null sẽ thành "" để gửi lên server.
     */
    fun prepareFields(
        firstName: String?,
        lastName: String?,
        nickName: String?,
        gender: String?,
        dateOfBirth: String?
    ): Map<String, RequestBody> = mutableMapOf<String, RequestBody>().apply {
        // MediaType text/plain
        val mt = "text/plain".toMediaType()

        // Luôn map key → RequestBody, dùng orEmpty() với null
        put("firstName",  (firstName.orEmpty()).toRequestBody(mt))
        put("lastName",   (lastName.orEmpty()).toRequestBody(mt))
        put("nickname",   (nickName.orEmpty()).toRequestBody(mt))
        put("gender",     (gender.orEmpty()).toRequestBody(mt))
        put("dateOfBirth",(dateOfBirth.orEmpty()).toRequestBody(mt))
    }

    /**
     * Tạo Multipart for avatar từ Uri, trả về null nếu Uri không hợp lệ.
     */
    fun prepareAvatarPart(uri: Uri, context: Context): MultipartBody.Part? {
        val contentResolver = context.contentResolver
        val mime = contentResolver.getType(uri) ?: return null
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        val bytes = inputStream.readBytes()
        val requestBody = bytes.toRequestBody(mime.toMediaType())
        return MultipartBody.Part.createFormData(
            name = "avatar",
            filename = "avatar.jpg",
            body = requestBody
        )
    }
}
