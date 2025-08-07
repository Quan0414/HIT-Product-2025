package com.example.hitproduct.screen.dialog.shop

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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.hitproduct.MainActivity
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.FcmClient
import com.example.hitproduct.common.util.NotificationConfig
import com.example.hitproduct.common.util.toThousandComma
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogShopBinding
import com.example.hitproduct.screen.adapter.ShopAdapter
import com.example.hitproduct.screen.dialog.food_detail.FoodDetailDialogFragment


class ShopDialogFragment : DialogFragment() {
    private var _binding: DialogShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ShopAdapter

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

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // full-width, wrap-content height, đẩy xuống đáy
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // lay coin tu home fragment
        binding.tvMoney.text = (activity as MainActivity).coin.toThousandComma()

        adapter = ShopAdapter(
            mutableListOf(),
            onImgClick = { food ->
                FoodDetailDialogFragment.newInstance(food)
                    .show(parentFragmentManager, "food_detail_bs")
            },
            onMoneyClick = { food ->
                // khi bấm vào giá thì feed pet
                viewModel.feedPet(food.id)
            }
        )
        binding.rvFood.adapter = adapter

        binding.rvFood.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            this.adapter = this@ShopDialogFragment.adapter
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        viewModel.foodListState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UiState.Error -> {
                    Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_SHORT)
                        .show()
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    adapter.submitList(result.data)
                }
            }
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
                    viewModel.clearFeedPetState()
                    dismiss()

                    val myLoveId = authRepo.getMyLoveId()
                    val payload = mapOf(
                        "type" to "pet_fed",
                    )
                    val tpl = NotificationConfig.getTemplate("pet_fed", payload)
                    FcmClient.sendToTopic(
                        receiverUserId = myLoveId,
                        title = tpl.title,
                        body = tpl.body,
                        data = payload
                    )
                }
            }
        }

        viewModel.fetchFoodList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}