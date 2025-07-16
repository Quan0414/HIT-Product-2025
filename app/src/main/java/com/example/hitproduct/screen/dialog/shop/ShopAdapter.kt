package com.example.hitproduct.screen.dialog.shop

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hitproduct.R
import com.example.hitproduct.data.model.food.Food

class ShopAdapter(
    private val items: MutableList<Food> = mutableListOf(),
    private val onImgClick: (Food) -> Unit,
    private val onMoneyClick: (Food) -> Unit

) : RecyclerView.Adapter<ShopAdapter.FoodViewHolder>() {

    fun submitList(list: List<Food>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val foodImg: ImageView = view.findViewById(R.id.imgFood)
        private val foodPrice: TextView = view.findViewById(R.id.tvMoney)

        fun bind(food: Food) {
            val secureUrl = food.image.replace("http://", "https://")
            Glide.with(itemView.context).load(secureUrl).into(foodImg)
            foodPrice.text = food.price.toString()

            foodImg.setOnClickListener {
                onImgClick(food)
            }

            foodPrice.setOnClickListener {
                onMoneyClick(food)
            }
        }

    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_food, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}