package com.example.hitproduct.common.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object RequestProfile {

    /**
     * Chỉ tạo các field text không-null, không-blank để gửi lên server.
     */
    fun prepareFields(
//        username: String?,
        firstName: String?,
        lastName: String?,
        nickName: String?,
        gender: String?,
        dateOfBirth: String?
    ): Map<String, RequestBody> {
        val map = mutableMapOf<String, RequestBody>()
//        username?.takeIf { it.isNotBlank() }?.let {
//            map["username"] = it.toRequestBody("text/plain".toMediaType())
//        }
        firstName?.takeIf { it.isNotBlank() }?.let {
            map["firstName"] = it.toRequestBody("text/plain".toMediaType())
        }
        lastName?.takeIf { it.isNotBlank() }?.let {
            map["lastName"] = it.toRequestBody("text/plain".toMediaType())
        }
        nickName?.takeIf { it.isNotBlank() }?.let {
            map["nickName"] = it.toRequestBody("text/plain".toMediaType())
        }
        gender?.takeIf { it.isNotBlank() }?.let {
            map["gender"] = it.toRequestBody("text/plain".toMediaType())
        }
        dateOfBirth?.takeIf { it.isNotBlank() }?.let {
            map["dateOfBirth"] = it.toRequestBody("text/plain".toMediaType())
        }
        return map
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
