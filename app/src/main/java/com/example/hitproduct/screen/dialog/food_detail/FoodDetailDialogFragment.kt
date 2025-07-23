package com.example.hitproduct.screen.dialog.food_detail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.toThousandComma
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.model.food.Food
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogFoodDetailBinding
import com.example.hitproduct.screen.dialog.shop.ShopViewModel
import com.example.hitproduct.screen.dialog.shop.ShopViewModelFactory
import com.example.hitproduct.util.Constant

class FoodDetailDialogFragment : DialogFragment() {
    private var _binding: DialogFoodDetailBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext()
            .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by activityViewModels<ShopViewModel> {
        ShopViewModelFactory(authRepo)
    }

    companion object {
        fun newInstance(food: Food) = FoodDetailDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(Constant.ARG_FOOD, food)
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val food = requireArguments().getSerializable(Constant.ARG_FOOD) as? Food
        food?.let {
            binding.tvName.text = it.name
            binding.tvMoney.text = it.price.toThousandComma()
            binding.tvHungerIndex.text = it.nutritionValue.toString()
            binding.tvHappyIndex.text = it.happinessValue.toString()
            val secureUrl = it.image.replace("http://", "https://")
            Glide.with(this).load(secureUrl).into(binding.imageView5)
        }

        viewModel.feedPet.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UiState.Error -> {
                    Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_SHORT)
                        .show()
                    viewModel.clearFeedPetState()
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Đã cho pet ăn thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                    viewModel.clearFeedPetState()
                }
            }
        }

        binding.btnClose.setOnClickListener { dismiss() }

        binding.imgMoneyFoodDetail.setOnClickListener {
            viewModel.feedPet(foodId = food?.id ?: return@setOnClickListener)
            dismiss()
            (parentFragment as? DialogFragment)?.dismiss()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
