package com.example.hitproduct.data.model.auth.request

import com.google.gson.annotations.SerializedName

data class SendPublicKeyRequest(
    @SerializedName("public_key")
    val publicKey: String,
    @SerializedName("private_key")
    val privateKey: String
)