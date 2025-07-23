package com.example.hitproduct.screen.home_page.couple

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentCoupleBinding
import com.example.hitproduct.screen.home_page.couple.detail.DialogProfileDetail
import com.example.hitproduct.screen.home_page.home.HomeViewModel
import com.example.hitproduct.screen.home_page.home.HomeViewModelFactory
import io.getstream.avatarview.glide.loadImage


class CoupleFragment : BaseFragment<FragmentCoupleBinding>() {

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by activityViewModels<HomeViewModel> {
        HomeViewModelFactory(authRepo)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            "total_date_updated",
            viewLifecycleOwner
        ) { _, bundle ->
            val totalDate = bundle.getInt("totalLoveDate", 0)
            binding.tvDay.text = totalDate.toString()
        }
    }

    override fun initListener() {
        binding.imgAvatar1.setOnClickListener {
            val dialog = DialogProfileDetail()
            dialog.show(childFragmentManager, "DialogProfileDetail")
        }
        binding.imgAvatar2.setOnClickListener {
            val dialog = DialogProfileDetail()
            dialog.show(childFragmentManager, "DialogProfileDetail")
        }
    }

    override fun initData() {
        viewModel.getCoupleProfile()
    }

    override fun handleEvent() {

    }

    override fun bindData() {
        viewModel.coupleProfile.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                }

                UiState.Idle -> {
                }

                UiState.Loading -> {
                }

                is UiState.Success -> {
                    // User A
                    val userA = state.data.userA
                    val displayNameA = userA.nickname
                        ?.takeIf { it.isNotBlank() }
                        ?: userA.firstName
                            ?.takeIf { it.isNotBlank() }
                        ?: userA.username
                    binding.tvNickname1.text = displayNameA

                    val avatarDataA: Any = userA.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: R.drawable.avatar_default
                    if (avatarDataA != "/example.png") {
                        binding.imgAvatar1.loadImage(avatarDataA)
                    } else {
                        binding.imgAvatar1.loadImage(R.drawable.avatar_default)
                    }

                    // User B
                    val userB = state.data.userB
                    val displayNameB = userB.nickname
                        ?.takeIf { it.isNotBlank() }
                        ?: userB.firstName
                            ?.takeIf { it.isNotBlank() }
                        ?: userB.username
                    binding.tvNickname2.text = displayNameB

                    val avatarDataB: Any = userB.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: R.drawable.avatar_default
                    if (avatarDataB != "/example.png") {
                        binding.imgAvatar2.loadImage(avatarDataB)
                    } else {
                        binding.imgAvatar2.loadImage(R.drawable.avatar_default)
                    }
                }

            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCoupleBinding {
        return FragmentCoupleBinding.inflate(inflater, container, false)
    }


}