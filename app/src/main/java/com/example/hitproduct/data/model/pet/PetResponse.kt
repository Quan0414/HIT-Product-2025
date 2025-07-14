package com.example.hitproduct.data.model.pet
import com.google.gson.annotations.SerializedName

data class PetResponse(
    @SerializedName("pet")
    val pet: Pet
)

data class Pet(
    @SerializedName("_id") val id: String,
    @SerializedName("coupleId") val coupleId: String,
    @SerializedName("name") val name: String,
    @SerializedName("hunger") val hunger: Int,
    @SerializedName("happiness") val happiness: Int,
    @SerializedName("lastFedAt") val lastFedAt: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int
)
