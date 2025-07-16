package com.example.hitproduct.screen.dialog.food_detail

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.hitproduct.common.util.toThousandComma
import com.example.hitproduct.data.model.food.Food
import com.example.hitproduct.databinding.DialogFoodDetailBinding

class FoodDetailDialogFragment : DialogFragment() {
    private var _binding: DialogFoodDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_FOOD = "arg_food"
        fun newInstance(food: Food) = FoodDetailDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_FOOD, food)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            attributes = attributes.apply { windowAnimations = 0 }
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFoodDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val food = requireArguments().getSerializable(ARG_FOOD) as? Food
        food?.let {
            binding.tvName.text = it.name
            binding.tvMoney.text = it.price.toThousandComma()
            binding.tvHungerIndex.text = it.nutritionValue.toString()
            binding.tvHappyIndex.text = it.happinessValue.toString()
            val secureUrl = it.image.replace("http://", "https://")
            Glide.with(this).load(secureUrl).into(binding.imageView5)
        }

        binding.btnClose.setOnClickListener { dismiss() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
