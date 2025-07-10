package com.example.hitproduct.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import com.example.hitproduct.R

data class Food(
    @IdRes val viewId: Int,
    val name: String,
    @DrawableRes val imgRes: Int,
    val hpGain: Int,            // +20
    val loveGain: Int,          // +10
    val price: Int              // 30 coin
)

val foodList = listOf(
    Food(1, "1", R.drawable.ic_food1, 20, 10, 30),
    Food(2, "2", R.drawable.ic_food2, 30, 20, 50),
    Food(3, "3", R.drawable.ic_food3, 40, 30, 70),
    Food(4, "4", R.drawable.ic_food4, 50, 40, 90),
    Food(5, "5", R.drawable.ic_food5, 60, 50, 110),
    Food(6, "6", R.drawable.ic_food6, 70, 60, 130),
    Food(7, "7", R.drawable.ic_food7, 80, 70, 150),
    Food(8, "8", R.drawable.ic_food8, 90, 80, 170),
    Food(9, "9", R.drawable.ic_food9, 100, 90, 190),
    Food(10, "10", R.drawable.ic_food10, 110, 100, 210),
    Food(11, "11", R.drawable.ic_food11, 120, 110, 230),
    Food(12, "12", R.drawable.ic_food12, 130, 120, 250),
    Food(13, "13", R.drawable.ic_food13, 140, 130, 270),
    Food(14, "14", R.drawable.ic_food14, 150, 140, 290),
    Food(15, "15", R.drawable.ic_food15, 160, 150, 310)
)