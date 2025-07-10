package com.example.hitproduct.screen.home_page.setting.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSettingBinding
import com.example.hitproduct.screen.home_page.setting.account_setting.AccountSettingFragment
import com.example.hitproduct.screen.home_page.setting.account_setting.AccountSettingViewModelFactory
import com.example.hitproduct.screen.home_page.setting.account_setting.AccountSettingViewmodel


class SettingFragment : BaseFragment<FragmentSettingBinding>() {

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

    private val viewModel by viewModels<SettingViewModel> {
        SettingViewModelFactory(authRepo)
    }

    override fun initView() {

    }

    override fun initListener() {

        binding.tvAccountManagement.setOnClickListener {
            val accountSettingFragment = AccountSettingFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentHomeContainer, accountSettingFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    override fun initData() {
        viewModel.fetchUserProfile()
    }

    override fun handleEvent() {

    }

    override fun bindData() {
        viewModel.userProfileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                }

                UiState.Idle -> {
                }

                UiState.Loading -> {
                }

                is UiState.Success -> {
                    // Cập nhật giao diện với dữ liệu người dùng
                    val user = state.data
                    // đổ data lên form
                    binding.tvName.text = user.username
                    binding.tvNickname.text = user.nickname

                    user.avatar?.takeIf { it.isNotBlank() }?.let { url ->
                        val secureUrl = url.replaceFirst("http://", "https://")
                        // dùng Glide / Coil / Picasso tuỳ thích
                        Glide.with(this)
                            .load(secureUrl)
                            .placeholder(R.drawable.avatar_default)
                            .error(R.drawable.avatar_default)
                            .into(binding.imgAvatar)
                    }
                }
            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

}