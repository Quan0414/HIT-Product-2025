package com.example.hitproduct.screen.authentication.forgot_method.create_new_pass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentCreateNewPasswordBinding
import com.example.hitproduct.screen.authentication.login.LoginActivity
import com.example.hitproduct.screen.authentication.register.success.SuccessCreateAccFragment
import kotlinx.coroutines.launch


class CreateNewPasswordFragment : BaseFragment<FragmentCreateNewPasswordBinding>() {

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

    private val email by lazy {
        arguments?.getString("email")
            ?: throw IllegalArgumentException("Email argument is required")
    }
    private val token by lazy {
        arguments?.getString("token")
            ?: throw IllegalArgumentException("Token argument is required")
    }

    override fun initView() {

    }

    override fun initListener() {


        setUpListeners()


        binding.backIcon.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }



    }

    override fun initData() {

    }

    override fun handleEvent() {

    }

    override fun bindData() {

    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateNewPasswordBinding {
        return FragmentCreateNewPasswordBinding.inflate(inflater, container, false)
    }


    //password
    private var isPasswordVisible1 = false
    private var isPasswordVisible2 = false


    private fun isFormValid(): Boolean {
        val password1 = binding.edtPassword1.text.toString().trim()
        val password2 = binding.edtPassword2.text.toString().trim()
        return password1.isNotEmpty() && password2.isNotEmpty()
    }


    private fun updateCreateNewPasswordButtonState() {
        val enabled = isFormValid()
        binding.tvReset.isEnabled = enabled
        binding.tvReset.setBackgroundResource(
            if (enabled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }

    private fun setUpListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                updateCreateNewPasswordButtonState()

                val pwd1 = binding.edtPassword1.text.toString().trim()
                val pwd2 = binding.edtPassword2.text.toString().trim()

                // Hiển thị error real-time
//                if (pwd1.isNotEmpty() && pwd2.isNotEmpty() && pwd1 != pwd2) {
//                    binding.edtPassword2.error = "Mật khẩu không khớp"
//                } else {
//                    binding.edtPassword2.error = null
//                }
            }

        }

        binding.edtPassword1.addTextChangedListener(watcher)
        binding.edtPassword2.addTextChangedListener(watcher)

        binding.tvReset.setOnClickListener {

            val pwd1 = binding.edtPassword1.text.toString().trim()
            val pwd2 = binding.edtPassword2.text.toString().trim()

            // Đây là chỗ xử lý logic chính: nếu không khớp thì báo và dừng
            if (pwd1 != pwd2) {
                Toast.makeText(requireContext(), "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pwd1.length < 6) {
                Toast.makeText(requireContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                when (val res = authRepo.resetPassword(email, pwd1, pwd2, token)) {
                    is DataResult.Error -> {
//                        binding.loadingProgressBar.visibility = View.GONE

                        Toast.makeText(
                            requireContext(),
                            res.error.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is DataResult.Success -> {
//                        binding.loadingProgressBar.visibility = View.GONE

                        Toast.makeText(
                            requireContext(),
                            "Đặt lại mật khẩu thành công",
                            Toast.LENGTH_SHORT
                        ).show()

                        val successCreateAccFragment = SuccessCreateAccFragment().apply {
                            arguments = Bundle().apply {
                                putString("flow", "forgot-password")
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                            )
                            .replace(R.id.fragmentStart, successCreateAccFragment)
                            .commit()
                    }
                }
            }
        }


        //An hien password
        binding.eyeIcon1.setOnClickListener {
            isPasswordVisible1 = !isPasswordVisible1

            binding.edtPassword1.transformationMethod =
                if (isPasswordVisible1) null else PasswordTransformationMethod.getInstance()

            binding.eyeIcon1.setImageResource(
                if (isPasswordVisible1) R.drawable.ic_eye_invisible else R.drawable.ic_eye_visible
            )


            binding.edtPassword1.setSelection(binding.edtPassword1.text?.length ?: 0)
        }

        binding.eyeIcon2.setOnClickListener {
            isPasswordVisible2 = !isPasswordVisible2

            if (isPasswordVisible2) {
                binding.edtPassword2.transformationMethod = null
                binding.eyeIcon2.setImageResource(R.drawable.ic_eye_invisible)
            } else {
                binding.edtPassword2.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.eyeIcon2.setImageResource(R.drawable.ic_eye_visible)
            }

            binding.edtPassword2.setSelection(binding.edtPassword2.text?.length ?: 0)
        }
    }

}