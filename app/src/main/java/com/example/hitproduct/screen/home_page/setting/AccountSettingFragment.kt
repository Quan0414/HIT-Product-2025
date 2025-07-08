package com.example.hitproduct.screen.home_page.setting

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentAccountSettingBinding
import com.example.hitproduct.screen.authentication.login.LoginViewModel
import com.example.hitproduct.screen.authentication.login.LoginViewModelFactory


class AccountSettingFragment : BaseFragment<FragmentAccountSettingBinding>() {

    private val prefs by lazy {
        requireContext()
            .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            RetrofitClient.getInstance().create(ApiService::class.java),
            prefs
        )
    }

    private val viewModel by viewModels<AccountSettingViewmodel> {
        AccountSettingViewModelFactory(authRepo)
    }

    private var isEditMode = false

    override fun initView() {
// Khởi đầu: disable tất cả, ẩn icon
        toggleFields(enabled = false)

    }

    override fun initListener() {
        binding.btnEditProfile.setOnClickListener {
            isEditMode = !isEditMode
            toggleFields(enabled = isEditMode)

            // Đổi text nút
            binding.btnEditProfile.text = if (isEditMode) "Lưu" else "Sửa thông tin"

            if (isEditMode) {
                //api
                toggleFields(enabled = true)
                binding.btnEditProfile.text = "Lưu"
            } else {
                binding.btnEditProfile.text = "Sửa thông tin"
                toggleFields(enabled = false)

                val firstName  = binding.edtHo.text.toString().takeIf { it.isNotBlank() }
                val lastName   = binding.edtTen.text.toString().takeIf { it.isNotBlank() }
                val nickname   = binding.edtNickname.text.toString().takeIf { it.isNotBlank() }
                val gender     = binding.edtGender.text.toString().takeIf { it.isNotBlank() }
                val birthday   = binding.edtBirthday.text.toString().takeIf { it.isNotBlank() }
                val avatarUri  = null  // hoặc lấy Uri nếu bạn cho chọn ảnh trước đó

                viewModel.updateProfile(
                    firstName, lastName, nickname,
                    gender, birthday, avatarUri, requireContext()
                )
            }
        }
    }

    override fun initData() {

    }

    override fun handleEvent() {

    }

    override fun bindData() {
        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {

                }
                is UiState.Success -> {
                    Toast.makeText(requireContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
                }
                is UiState.Error -> {
                    // Hiển thị thông báo lỗi
                    Toast.makeText(requireContext(), "Lỗi: ${state.error}", Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun toggleFields(enabled: Boolean) {
        val fields = listOf(
            binding.edtHo,
            binding.edtTen,
            binding.edtEmail,
            binding.edtGender,
            binding.edtBirthday
        )

        fields.forEach { field ->
            field.isEnabled = enabled
        }

        // Map từng EditText với icon tương ứng khi ở chế độ edit
        val iconMap = mapOf(
            binding.edtHo       to R.drawable.ic_edit_text,
            binding.edtTen      to R.drawable.ic_edit_text,
            binding.edtNickname to R.drawable.ic_edit_text,
            binding.edtGender   to R.drawable.ic_arrow_down,
            binding.edtBirthday to R.drawable.ic_edit_text
        )

        iconMap.forEach { (field, iconRes) ->
            field.isEnabled = enabled
            val endDrawable = if (enabled) iconRes else 0
            field.setCompoundDrawablesRelativeWithIntrinsicBounds(
                /* start  */ 0,
                /* top    */ 0,
                /* end    */ endDrawable,
                /* bottom */ 0
            )
        }
        binding.btnEditAvatar.visibility = if (enabled) View.VISIBLE else View.INVISIBLE
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAccountSettingBinding {
        return FragmentAccountSettingBinding.inflate(inflater, container, false)
    }


}