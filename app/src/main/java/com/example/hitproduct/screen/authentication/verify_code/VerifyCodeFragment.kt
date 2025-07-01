package com.example.hitproduct.screen.authentication.verify_code

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentVerifyCodeBinding
import com.example.hitproduct.screen.authentication.forgot_method.create_new_pass.CreateNewPasswordFragment
import com.example.hitproduct.screen.authentication.register.set_up_infor.SetUpInformationFragment

class VerifyCodeFragment : Fragment() {
    private var _binding: FragmentVerifyCodeBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences(
            AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE
        )
    }
    private val authRepo by lazy {
        AuthRepository(
            RetrofitClient.getInstance().create(ApiService::class.java),
            prefs
        )
    }
    private val viewModel by viewModels<VerifyCodeViewModel> {
        VerifyCodeViewModelFactory(authRepo)
    }

    private val email by lazy {
        arguments?.getString("email")
            ?: throw IllegalArgumentException("Email argument is required")
    }
    private val flow by lazy {
        arguments?.getString("flow") ?: throw IllegalArgumentException("Flow argument is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Quan sát trạng thái gửi OTP
        viewModel.sendOtpState.observe(viewLifecycleOwner) { state ->
            binding.tvSendCode.isEnabled = state !is UiState.Loading
            when (state) {
                is UiState.Success -> Toast.makeText(
                    requireContext(),
                    state.data,
                    Toast.LENGTH_SHORT
                ).show()

                is UiState.Error -> Toast.makeText(
                    requireContext(),
                    state.error.message,
                    Toast.LENGTH_SHORT
                ).show()

                else -> {}
            }
        }

        // Nút gửi lại OTP
        binding.tvSendCode.setOnClickListener {
            viewModel.sendOtp(email)
        }

        // Chuẩn bị các ô nhập OTP
        val codes = listOf(
            binding.edtCode1, binding.edtCode2,
            binding.edtCode3, binding.edtCode4
        )

        fun updateContinueState() {
            val allFilled = codes.all { it.text?.length == 1 }
            binding.tvContinue.isEnabled = allFilled
            binding.tvContinue.setBackgroundResource(
                if (allFilled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
            )
        }
        codes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < codes.lastIndex) codes[index + 1].requestFocus()
                    updateContinueState()
                }

                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            })
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && editText.text.isEmpty() && index > 0) {
                    codes[index - 1].apply {
                        text?.clear()
                        requestFocus()
                    }
                    updateContinueState()
                    true
                } else false
            }
        }
        updateContinueState()

        // Quan sát xác thực OTP
        viewModel.verifyCodeState.observe(viewLifecycleOwner) { state ->
            binding.tvContinue.isEnabled = state !is UiState.Loading
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    navigateNext()
                }

                is UiState.Error -> Toast.makeText(
                    requireContext(),
                    state.error.message,
                    Toast.LENGTH_SHORT
                ).show()

                else -> {}
            }
        }

        // Nút xác thực
        binding.tvContinue.setOnClickListener {
            val otp = codes.joinToString(separator = "") { it.text.toString() }
            viewModel.verifyCode(otp, email, flow)
        }

        // Nút back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun navigateNext() {
        if (flow == "register") {
            parentFragmentManager.popBackStack(
                "Register", FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentStart, SetUpInformationFragment())
                .addToBackStack(null)
                .commit()
        } else {
            parentFragmentManager.popBackStack(
                "EnterEmail", FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentStart, CreateNewPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
