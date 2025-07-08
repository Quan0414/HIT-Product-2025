package com.example.hitproduct.common.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// Chuyển String thành RequestBody text/plain
fun String.toPlainText(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())

// Tạo MultipartBody.Part cho file ảnh
fun File.toImagePart(partName: String): MultipartBody.Part {
    val reqFile = this.asRequestBody("image/*".toMediaType())
    return MultipartBody.Part.createFormData(partName, this.name, reqFile)
}