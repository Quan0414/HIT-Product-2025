package com.example.hitproduct.screen.home_page.setting

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentAccountSettingBinding
import com.example.hitproduct.screen.authentication.register.set_up_infor.SetUpInformationViewModel
import com.example.hitproduct.screen.authentication.register.set_up_infor.SetUpInformationViewModelFactory


class AccountSettingFragment : BaseFragment<FragmentAccountSettingBinding>() {

    // 1. SharedPreferences
    private val prefs by lazy {
        requireContext()
            .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 2. AuthRepository
    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    // 3. ViewModel
    private val viewModel by viewModels<AccountSettingViewModel> {
        SetUpInformationViewModelFactory(authRepo)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_setting, container, false)
    }

    override fun initView() {

    }

    override fun initListener() {

    }

    override fun initData() {
        viewModel.getProfile(prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "") ?: "")

    }

    override fun handleEvent() {

    }

    override fun bindData() {
        viewModel.getProfile.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Hiển thị loading
                }

                is UiState.Success -> {
                    // Cập nhật giao diện với dữ liệu người dùng
                    Glide.with(this)
                        .load(state.data.avatar)
                        .placeholder(R.drawable.bg_avatar)
                        .error(R.drawable.bg_avatar)
                        .into(binding.imgAvatar)

                    binding.tvUsername.text = state.data.username
                    binding.tvNickname1.text = state.data.username
                    binding.edtHo.setText(state.data.firstName)
                    binding.edtTen.setText(state.data.lastName)
                    binding.edtNickname.setText(state.data.nickname)
                    binding.edtEmail.setText(state.data.email)
                    binding.edtGender.setText(state.data.gender)
                    binding.edtBirthday.setText(state.data.dateOfBirth)
                }

                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Lấy profile thất bại: ${state.error.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                UiState.Idle -> {

                }
            }
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Hiển thị loading
                }

                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thành công: ${state.data}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thất bại: ${state.error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                UiState.Idle -> {

                }
            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAccountSettingBinding {
        return FragmentAccountSettingBinding.inflate(inflater, container, false)
    }


}