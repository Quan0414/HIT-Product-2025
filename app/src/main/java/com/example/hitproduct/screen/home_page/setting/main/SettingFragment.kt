package com.example.hitproduct.screen.home_page.setting.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSettingBinding
import com.example.hitproduct.screen.authentication.login.LoginActivity
import com.example.hitproduct.screen.dialog.disconnect.DialogDisconnectFragment
import com.example.hitproduct.screen.home_page.setting.account_setting.AccountSettingFragment


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

    private val viewModel by activityViewModels<SettingViewModel> {
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

//        binding.btnBack.setOnClickListener {
//            (requireActivity() as? MainActivity)?.goToHomeTab()
//        }

        binding.btnDisconnect.setOnClickListener {
            DialogDisconnectFragment {
                // callback khi bấm "Đồng ý"
                viewModel.disconnectCouple()
            }.show(parentFragmentManager, "disconnect_dialog")
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

        viewModel.disconnectState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    val err = state.error
                    Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
                }
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Hủy kết nối thành công", Toast.LENGTH_SHORT)
                        .show()

                    prefs.edit().clear().apply()
                    // Chuyển về Splash và clear toàn bộ stack
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
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