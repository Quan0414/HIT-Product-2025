package com.example.hitproduct.screen.home_page.setting.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.CryptoHelper
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSettingBinding
import com.example.hitproduct.screen.authentication.login.LoginActivity
import com.example.hitproduct.screen.dialog.disconnect.DialogDisconnectFragment
import com.example.hitproduct.screen.dialog.logout.DialogLogout
import com.example.hitproduct.screen.home_page.setting.account_setting.AccountSettingFragment
import com.example.hitproduct.screen.splash.SplashActivity
import io.getstream.avatarview.glide.loadImage


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
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
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

        binding.btnLogout.setOnClickListener {
            DialogLogout {
                val prefs = requireContext().getSharedPreferences(
                    AuthPrefersConstants.PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                val onboardingDone = prefs.getBoolean(AuthPrefersConstants.ON_BOARDING_DONE, false)
                authRepo.clearAccessToken()
                prefs.edit().putBoolean(AuthPrefersConstants.ON_BOARDING_DONE, onboardingDone)
                    .apply()

                // Xoa key pair ECDH
//                CryptoHelper.deleteAllKeys(requireContext())

                val intent = Intent(requireContext(), SplashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

                requireActivity().finish()

            }.show(parentFragmentManager, "logout_dialog")
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
                    val user = state.data
                    binding.tvName.text = user.username

                    val nickname = user.nickname
                        .takeIf { !it.isNullOrBlank() }
                        ?: ""
                    binding.tvNickname.text = nickname

                    val avatarUrl = user.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: R.drawable.avatar_default
                    if (avatarUrl != "/example.png") {
                        binding.imgAvatar.loadImage(avatarUrl)
                    } else {
                        binding.imgAvatar.loadImage(R.drawable.avatar_default)
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
                    Toast.makeText(
                        requireContext(),
                        "Bạn đã chia tay với cậu ấy.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val onboardingDone =
                        prefs.getBoolean(AuthPrefersConstants.ON_BOARDING_DONE, false)
                    prefs.edit().clear().apply()
                    prefs.edit().putBoolean(AuthPrefersConstants.ON_BOARDING_DONE, onboardingDone)
                        .apply()

//                    CryptoHelper.deleteAllKeys(requireContext())

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