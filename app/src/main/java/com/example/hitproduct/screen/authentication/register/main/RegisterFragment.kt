package com.example.hitproduct.screen.authentication.register.main

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentRegisterBinding
import com.example.hitproduct.screen.authentication.verify_code.VerifyCodeFragment

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var isPasswordVisible1 = false
    private var isPasswordVisible2 = false
    private var isCheckBoxChecked = false

    // Dependencies
    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(RetrofitClient.getInstance().create(ApiService::class.java), prefs)
    }

    private val viewModel by viewModels<RegisterViewModel> {
        RegisterViewModelFactory(authRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup checkbox image
        binding.imgCheckBox.setImageResource(
            if (isCheckBoxChecked) R.drawable.ic_checked else R.drawable.ic_unchecked
        )

        // Back button
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Setup terms text with colored spans
        val text = "Tôi đã đọc và đồng ý với Điều khoản và Điều kiện cũng như Chính sách bảo mật"
        val spannableString = SpannableStringBuilder(text)
        val orange = ContextCompat.getColor(requireContext(), R.color.orange)

        val start1 = text.indexOf("Điều khoản và Điều kiện")
        val end1 = start1 + "Điều khoản và Điều kiện".length
        val start2 = text.indexOf("Chính sách bảo mật")
        val end2 = start2 + "Chính sách bảo mật".length

        spannableString.setSpan(
            ForegroundColorSpan(orange),
            start1,
            end1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(orange),
            start2,
            end2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvCheckBox.text = spannableString

        // Observe register state
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    val err = state.error
                    binding.edtTenNguoiDung.setBackgroundResource(
                        if (err.usernameError) R.drawable.bg_edit_text_error else R.drawable.bg_edit_text
                    )
                    binding.edtEmail.setBackgroundResource(
                        if (err.emailError) R.drawable.bg_edit_text_error else R.drawable.bg_edit_text
                    )
                    binding.edtPassword1.setBackgroundResource(
                        if (err.passwordError) R.drawable.bg_edit_text_error else R.drawable.bg_edit_text
                    )
                    binding.edtPassword2.setBackgroundResource(
                        if (err.confirmPasswordError) R.drawable.bg_edit_text_error else R.drawable.bg_edit_text
                    )
                    Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
                    binding.tvRegister.isEnabled = true
                }

                UiState.Idle -> {
                    binding.tvRegister.isEnabled = true
                    // Reset field backgrounds
                    binding.edtTenNguoiDung.setBackgroundResource(R.drawable.bg_edit_text)
                    binding.edtEmail.setBackgroundResource(R.drawable.bg_edit_text)
                    binding.edtPassword1.setBackgroundResource(R.drawable.bg_edit_text)
                    binding.edtPassword2.setBackgroundResource(R.drawable.bg_edit_text)
                }

                UiState.Loading -> {
                    binding.tvRegister.isEnabled = false
                }

                is UiState.Success -> {
                    binding.tvRegister.isEnabled = true
                    Toast.makeText(requireContext(), "Đăng ký thành công", Toast.LENGTH_SHORT)
                        .show()

                    // Navigate to VerifyCodeFragment
                    val verifyCodeFragment = VerifyCodeFragment().apply {
                        arguments = Bundle().apply { putString("flow", "register") }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentStart, verifyCodeFragment)
                        .addToBackStack("Register")
                        .commit()

                    viewModel.clearRegisterState()

                }
            }
        }

        updateRegisterButtonState()

        // Text watchers for form validation
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateRegisterButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.edtTenNguoiDung.addTextChangedListener(textWatcher)
        binding.edtEmail.addTextChangedListener(textWatcher)
        binding.edtPassword1.addTextChangedListener(textWatcher)
        binding.edtPassword2.addTextChangedListener(textWatcher)

        // Checkbox listener
        binding.imgCheckBox.setOnClickListener {
            isCheckBoxChecked = !isCheckBoxChecked
            binding.imgCheckBox.setImageResource(
                if (isCheckBoxChecked) R.drawable.ic_checked else R.drawable.ic_unchecked
            )
            updateRegisterButtonState()
        }

        // Password visibility toggles
        binding.eyeIcon1.setOnClickListener {
            isPasswordVisible1 = !isPasswordVisible1
            if (isPasswordVisible1) {
                binding.edtPassword1.transformationMethod = null
                binding.eyeIcon1.setImageResource(R.drawable.ic_eye_invisible)
            } else {
                binding.edtPassword1.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.eyeIcon1.setImageResource(R.drawable.ic_eye_visible)
            }
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

        // Register button
        binding.tvRegister.setOnClickListener {
            if (isFormValid()) {
                viewModel.register(
                    binding.edtTenNguoiDung.text.toString().trim(),
                    binding.edtEmail.text.toString().trim(),
                    binding.edtPassword1.text.toString(),
                    binding.edtPassword2.text.toString()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isFormValid(): Boolean {
        val username = binding.edtTenNguoiDung.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val password1 = binding.edtPassword1.text.toString()
        val password2 = binding.edtPassword2.text.toString()

        return username.isNotEmpty() && email.isNotEmpty() &&
                password1.isNotEmpty() && password2.isNotEmpty() &&
                isCheckBoxChecked
    }

    private fun updateRegisterButtonState() {
        val enabled = isFormValid()
        binding.tvRegister.isEnabled = enabled
        binding.tvRegister.setBackgroundResource(
            if (enabled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }
}