package com.example.hitproduct.screen.authentication.verify_code

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    // Lấy email và flow từ arguments
    private val email by lazy {
        arguments?.getString("email")
            ?: throw IllegalArgumentException("Email argument is required")
    }
    private val flow by lazy {
        arguments?.getString("flow") ?: throw IllegalArgumentException("Flow argument is required")
    }

    // Biến để quản lý bộ đếm ngược
    private var timer: CountDownTimer? = null
    private val totalTime = 120_000L  // 120s = 120.000 ms

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // hiển thị text với email thay cho %1$s
        binding.tvVerifyCode.text = getString(R.string.verify_code_to, email)

        // ban đầu disable và đổi màu nút gửi lại
        binding.tvSendCode.isEnabled = false
        binding.tvSendCode.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.grayText)
        )
        startCountdown()

        // Quan sát trạng thái gửi OTP
        viewModel.sendOtpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // show loading nếu cần
                }

                is UiState.Success -> {
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    // sau khi gửi lại thành công, restart đếm
                    binding.tvSendCode.isEnabled = false
                    binding.tvSendCode.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.grayText)
                    )
                    startCountdown()
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_SHORT).show()
                    // nếu muốn cho retry ngay khi error, có thể enable:
                    binding.tvSendCode.isEnabled = true
                    binding.tvSendCode.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.orange)
                    )
                }

                UiState.Idle -> {
                    // trạng thái idle, có thể không cần làm gì
                }
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
                    codes.forEach { it.setBackgroundResource(R.drawable.bg_confirm_code) }
                    if (s?.length == 1 && index < codes.lastIndex) codes[index + 1].requestFocus()
                    updateContinueState()
                }

                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            })
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL
                    && event.action == KeyEvent.ACTION_DOWN
                    && editText.text.isEmpty()
                    && index > 0
                ) {
                    val prev = codes[index - 1]
                    if (prev.text.isNotEmpty())
                        prev.text.delete(prev.text.length - 1, prev.text.length)
                    prev.apply {
                        requestFocus()
                        setSelection(text.length)
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
                is UiState.Idle -> {
                }

                is UiState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Xác thực OTP thành công!", Toast.LENGTH_SHORT)
                        .show()
                    navigateNext()
                }

                is UiState.Error -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    // Hiển thị thông báo lỗi
                    val err = state.error
                    if (err.otp) {
                        // Nếu lỗi là do OTP không chính xác, highlight các ô nhập
                        codes.forEach { it.setBackgroundResource(R.drawable.bg_edit_text_error) }
                    } else {
                        // Nếu lỗi khác, reset các ô nhập
                        codes.forEach { it.setBackgroundResource(R.drawable.bg_confirm_code) }
                    }

                    // Hiển thị Toast với thông báo lỗi
                    Toast.makeText(
                        requireContext(),
                        state.error.message, Toast.LENGTH_SHORT
                    ).show()
                }

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
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentStart, SetUpInformationFragment())
                .addToBackStack(null)
                .commit()
        } else {
            parentFragmentManager.popBackStack(
                "EnterEmail", FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentStart, CreateNewPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }


    private fun startCountdown() {
        timer?.cancel()
        timer = object : CountDownTimer(totalTime, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                if (_binding == null) return
                val seconds = (millisUntilFinished / 1_000).toInt()
                binding.tvTime.text = getString(R.string.code_expired, seconds)
            }

            override fun onFinish() {
                if (_binding == null) return
                // Bật lại nút gửi mã và đổi màu
                binding.tvSendCode.isEnabled = true
                binding.tvSendCode.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.orange)
                )
            }
        }.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        timer = null
        _binding = null
    }
}
