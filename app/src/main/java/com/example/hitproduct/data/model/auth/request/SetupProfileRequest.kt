package com.example.hitproduct.data.model.auth.request

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object SetupProfileRequest {

    /**
     * Chỉ tạo các field text không-null, không-blank để gửi lên server.
     */
    fun prepareFields(
        firstName: String?,
        lastName: String?,
        nickName: String?,
        gender: String?,
        dateOfBirth: String?
    ): Map<String, RequestBody> {
        val map = mutableMapOf<String, RequestBody>()
        firstName?.takeIf { it.isNotBlank() }?.let {
            map["firstName"] = it.toRequestBody("text/plain".toMediaType())
        }
        lastName?.takeIf { it.isNotBlank() }?.let {
            map["lastName"] = it.toRequestBody("text/plain".toMediaType())
        }
        nickName?.takeIf { it.isNotBlank() }?.let {
            map["nickname"] = it.toRequestBody("text/plain".toMediaType())
        }
        gender?.takeIf { it.isNotBlank() }?.let {
            map["gender"] = it.toRequestBody("text/plain".toMediaType())
        }
        dateOfBirth?.takeIf { it.isNotBlank() }?.let {
            map["dateOfBirth"] = it.toRequestBody("text/plain".toMediaType())
        }
        return map
    }
}