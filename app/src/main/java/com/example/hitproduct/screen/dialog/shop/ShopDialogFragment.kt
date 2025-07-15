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
import com.example.hitproduct.common.util.toThousandComma
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogShopBinding


class ShopDialogFragment : DialogFragment() {
    private var _binding: DialogShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FoodAdapter

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

        adapter = FoodAdapter()
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

        viewModel.fetchFoodList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}