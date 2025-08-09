package com.example.hitproduct.screen.home_page.setting.policy.change_password

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentChangePasswordBinding
import kotlinx.coroutines.launch

class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {

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

    private var isOldPassVisible = false
    private var isNewPassVisible = false
    private var isConfirmPassVisible = false

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChangePasswordBinding {
        return FragmentChangePasswordBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // Khởi tạo các EditText ở dạng password
        binding.edtOldPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.edtPassword1.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.edtPassword2.transformationMethod = PasswordTransformationMethod.getInstance()
        updateChangeButtonState()
    }

    override fun initListener() {
        setupPasswordToggles()
        setupTextWatchers()

        // Back lại màn trước
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Xử lý đổi mật khẩu
        binding.tvLogin.setOnClickListener {
            val oldPw = binding.edtOldPassword.text.toString().trim()
            val newPw = binding.edtPassword1.text.toString().trim()
            val confirmPw = binding.edtPassword2.text.toString().trim()

            if (oldPw.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Mật khẩu cũ không hợp lệ",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                Toast.makeText(requireContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPw.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Mật khẩu phải ít nhất 6 ký tự",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            binding.loadingProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                when (val result = authRepo.changePassword(oldPw, newPw, confirmPw)) {
                    is DataResult.Success -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Đổi mật khẩu thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    }

                    is DataResult.Error -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        }
    }

    override fun initData() {

    }

    override fun handleEvent() {

    }

    override fun bindData() {

    }

//    override fun onResume() {
//        super.onResume()
//        (requireActivity() as? MainActivity)?.hideBottomNav()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        (requireActivity() as? MainActivity)?.showBottomNav()
//    }

    private fun setupPasswordToggles() {
        // Old password
        binding.eyeIcon1.setOnClickListener {
            isOldPassVisible = !isOldPassVisible
            binding.edtOldPassword.transformationMethod =
                if (isOldPassVisible) null else PasswordTransformationMethod.getInstance()
            binding.eyeIcon1.setImageResource(
                if (isOldPassVisible) R.drawable.ic_eye_visible else R.drawable.ic_eye_invisible
            )
            binding.edtOldPassword.setSelection(binding.edtOldPassword.text?.length ?: 0)
        }
        // New password
        binding.eyeIcon2.setOnClickListener {
            isNewPassVisible = !isNewPassVisible
            binding.edtPassword1.transformationMethod =
                if (isNewPassVisible) null else PasswordTransformationMethod.getInstance()
            binding.eyeIcon2.setImageResource(
                if (isNewPassVisible) R.drawable.ic_eye_visible else R.drawable.ic_eye_invisible
            )
            binding.edtPassword1.setSelection(binding.edtPassword1.text?.length ?: 0)
        }
        // Confirm password
        binding.eyeIcon3.setOnClickListener {
            isConfirmPassVisible = !isConfirmPassVisible
            binding.edtPassword2.transformationMethod =
                if (isConfirmPassVisible) null else PasswordTransformationMethod.getInstance()
            binding.eyeIcon3.setImageResource(
                if (isConfirmPassVisible) R.drawable.ic_eye_visible else R.drawable.ic_eye_invisible
            )
            binding.edtPassword2.setSelection(binding.edtPassword2.text?.length ?: 0)
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateChangeButtonState()
            }
        }
        binding.edtOldPassword.addTextChangedListener(watcher)
        binding.edtPassword1.addTextChangedListener(watcher)
        binding.edtPassword2.addTextChangedListener(watcher)
    }

    private fun updateChangeButtonState() {
        val oldPw = binding.edtOldPassword.text.toString().trim()
        val newPw = binding.edtPassword1.text.toString().trim()
        val confirmPw = binding.edtPassword2.text.toString().trim()
        val enabled = oldPw.isNotEmpty() && newPw.isNotEmpty() && confirmPw.isNotEmpty()
        binding.tvLogin.isEnabled = enabled
        binding.tvLogin.setBackgroundResource(
            if (enabled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }
}
