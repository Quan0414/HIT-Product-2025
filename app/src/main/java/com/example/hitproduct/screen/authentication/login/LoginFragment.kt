package com.example.hitproduct.screen.authentication.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentLoginBinding
import com.example.hitproduct.screen.authentication.forgot_method.find_acc.FindAccFragment
import com.example.hitproduct.screen.authentication.register.main.RegisterFragment
import com.example.hitproduct.screen.authentication.send_invite_code.SendInviteCodeFragment

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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

    private val viewModel by viewModels<LoginViewModel> {
        LoginViewModelFactory(authRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Quan sát kết quả checkCouple
        viewModel.coupleState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val myUserId = state.data.id
                    prefs.edit().putString(AuthPrefersConstants.MY_USER_ID, myUserId).apply()
                    val idRoomChat = state.data.roomChatId
                    prefs.edit().putString(AuthPrefersConstants.ID_ROOM_CHAT, idRoomChat).apply()

                    if (state.data.couple == null) {
                        // Chưa có đôi → chuyển sang SendInviteCodeFragment
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentStart, SendInviteCodeFragment())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        // Đã có đôi → vào MainActivity
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                }

                is UiState.Error -> {
                    // Nếu cần xử lý lỗi khi check couple
                }

                else -> {
                    // Idle/Loading bỏ qua
                }
            }
        }

        // 2. Quan sát loginState
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                    binding.tvLogin.isEnabled = false
                }

                is UiState.Error -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    // Reset background và show lỗi
                    val err = state.error
                    Toast.makeText(
                        requireContext(),
                        err.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.edtEmail.setBackgroundResource(
                        if (err.emailError) R.drawable.bg_edit_text_error
                        else R.drawable.bg_edit_text
                    )
                    binding.edtPassword.setBackgroundResource(
                        if (err.passwordError) R.drawable.bg_edit_text_error
                        else R.drawable.bg_edit_text
                    )

                    binding.tvLogin.isEnabled = true

                    if (err.message == "Tài khoản chưa được xác nhận!") {
                        Toast.makeText(
                            requireContext(),
                            "Tài khoản chưa được xác nhận! Vui lòng nhập email để xác nhận tài khoản. ",
                            Toast.LENGTH_SHORT
                        ).show()
                        //chuyen den verify code kem theo email da nhap trong text
                        val email = binding.edtEmail.text.toString().trim()
                        val bundle = Bundle().apply {
                            putString("email", email)
                            putString("flow", "register")
                        }
                        val fragment = FindAccFragment().apply {
                            arguments = bundle
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentStart, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }

                is UiState.Success -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.tvLogin.isEnabled = true
                    Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT)
                        .show()

                    // Sau khi login thành công, check tiếp couple
                    viewModel.checkCouple()

                    // Reset loginState để tránh lặp lại
                    viewModel.clearLoginState()
                }

                else -> {
                    // Idle bỏ qua
                }
            }
        }

        // 3. Nút Đăng nhập
        binding.tvLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtPassword.text.toString()
            viewModel.login(email, pass)
        }

        // 4. Validation form
        setUpListeners()
        updateLoginButtonState()


        // 5. Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentStart, FindAccFragment())
                .addToBackStack(null)
                .commit()
        }

        // 6. Link Đăng ký
        val spannable = SpannableString(binding.tvRegister.text)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(
                        R.id.fragmentStart,
                        RegisterFragment().apply {
                            arguments = Bundle().apply { putString("flow", "register") }
                        }
                    )
                    .addToBackStack(null)
                    .commit()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(requireContext(), R.color.orange)
                ds.isUnderlineText = false
            }
        }, 19, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvRegister.text = spannable
        binding.tvRegister.movementMethod = LinkMovementMethod.getInstance()

        // 7. Toggle hiển thị password
        var isPasswordVisible = false
        binding.eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.edtPassword.transformationMethod =
                if (isPasswordVisible) null
                else PasswordTransformationMethod()
            binding.eyeIcon.setImageResource(
                if (isPasswordVisible) R.drawable.ic_eye_invisible
                else R.drawable.ic_eye_visible
            )
            binding.edtPassword.setSelection(binding.edtPassword.text?.length ?: 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpListeners() {
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.edtEmail.setBackgroundResource(R.drawable.bg_edit_text)
                updateLoginButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.edtPassword.setBackgroundResource(R.drawable.bg_edit_text)
                updateLoginButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateLoginButtonState() {
        val enabled = binding.edtEmail.text.isNotBlank() &&
                binding.edtPassword.text.isNotBlank()
        binding.tvLogin.isEnabled = enabled
        binding.tvLogin.setBackgroundResource(
            if (enabled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }
}
