package com.example.hitproduct.data.model.food

import com.google.gson.annotations.SerializedName

data class FoodData(
    val foods: List<Food>,
    val totalFoods: Int,
    val totalPages: Int,
    val currentPage: Int
)

data class Food(
    @SerializedName("_id") val id: String,
    val name: String,
    val image: String,
    val price: Int,
    val nutritionValue: Int,
    val happinessValue: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = null
)
