package com.example.hitproduct.screen.authentication.login

import android.content.Context
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
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hitproduct.HomeFragment
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.ErrorMessageMapper
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentLoginBinding
import com.example.hitproduct.screen.authentication.forgot_method.find_acc.FindAccFragment
import com.example.hitproduct.screen.authentication.register.main.RegisterFragment

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // 1. SharedPreferences
    private val prefs by lazy {
        requireContext()
            .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 2. AuthRepository
    private val authRepo by lazy {
        AuthRepository(
            RetrofitClient.getInstance().create(ApiService::class.java),
            prefs
        )
    }

    // 3. ViewModel
    private val viewModel by viewModels<LoginViewModel> {
        LoginViewModelFactory(authRepo)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe loginState để show Loading, Error, Success
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Idle -> {
                    // Trạng thái Idle, không làm gì
                }

                is UiState.Loading -> {
                    binding.tvLogin.isEnabled = false
                }

                is UiState.Error -> {
                    // 1. Reset background
                    val err = state.error
                    binding.edtEmail.setBackgroundResource(
                        if (err.emailError) R.drawable.bg_edit_text_error
                        else R.drawable.bg_edit_text
                    )
//                    binding.errEmail.isVisible = err.emailError

                    binding.edtPassword.setBackgroundResource(
                        if (err.passwordError) R.drawable.bg_edit_text_error
                        else R.drawable.bg_edit_text
                    )
//                    binding.errPassword.isVisible = err.passwordError
//                    binding.eyeIcon.isVisible = !err.passwordError

                    Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
                    binding.tvLogin.isEnabled = true
                }

                is UiState.Success -> {
                    binding.tvLogin.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Đăng nhập thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    //chuyen man vao main activity va ket thuc loginactivity


                }
            }
        }

        // Nút Đăng nhập: gọi ViewModel.login()
        binding.tvLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val pass = binding.edtPassword.text.toString()
            viewModel.login(email, pass)
        }

        //Form validation
        setUpListeners()

        //quen mat khau
        binding.tvForgotPassword.setOnClickListener {
            val findAccFragment = FindAccFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentStart, findAccFragment)
                .addToBackStack(null)
                .commit()
        }


        // Lấy TextView từ binding
        val text = binding.tvRegister
        val spannableString = SpannableString(text.text)

        //  ClickableSpan chuyen sang dang ky
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {

                // Thay thế Fragment hiện tại bằng RegisterFragment mới
                val registerFragment = RegisterFragment().apply {
                    arguments = Bundle().apply { putString("flow", "register") }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentStart, registerFragment)
                    .addToBackStack(null)
                    .commit()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.orange)
                ds.isUnderlineText = false   // tắt gạch chân nếu không muốn
            }
        }

        // Áp dụng ClickableSpan cho phần văn bản
        spannableString.setSpan(
            clickableSpan,
            19, // Start index của clickable span
            31, // End index của clickable span
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Đặt text đã được format và clickable
        text.text = spannableString
        text.movementMethod = LinkMovementMethod.getInstance()  // Cho phép nhấn vào văn bản


        binding.eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Hiện password
                binding.edtPassword.transformationMethod = null
                // Đổi icon sang mắt mở
                binding.eyeIcon.setImageResource(R.drawable.ic_eye_invisible)
            } else {
                // Ẩn password
                binding.edtPassword.transformationMethod = PasswordTransformationMethod()
                // Đổi icon sang mắt gạch
                binding.eyeIcon.setImageResource(R.drawable.ic_eye_visible)
            }

            // Đặt con trỏ về cuối text
            binding.edtPassword.setSelection(binding.edtPassword.text?.length ?: 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear để tránh giữ reference đến View sau khi destroy
        _binding = null
    }


    private var isPasswordVisible = false


    private fun isFormValid(): Boolean {
        val email = binding.edtEmail.text.toString().trim()
        val pass = binding.edtPassword.text.toString()
        return email.isNotEmpty() && pass.isNotEmpty()
    }

    private fun updateLoginButtonState() {
        val enabled = isFormValid()
        binding.tvLogin.isEnabled = enabled
        binding.tvLogin.setBackgroundResource(
            if (enabled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }

    private fun setUpListeners() {
        // Watcher cho email: ẩn X và reset background
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 1. Ẩn dấu X
//                binding.errEmail.isVisible = false
                // 2. Reset nền về bình thường
                binding.edtEmail.setBackgroundResource(R.drawable.bg_edit_text)
                // 3. Cập nhật trạng thái nút Đăng Nhập
                updateLoginButtonState()
            }
        })

        // Watcher cho password: ẩn X, hiện lại mắt và reset background
        binding.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 1. Ẩn dấu X
//                binding.errPassword.isVisible = false
                // 2. Reset nền về bình thường
                binding.edtPassword.setBackgroundResource(R.drawable.bg_edit_text)
                // 3. Hiện lại icon mắt
//                binding.eyeIcon.isVisible = true
                // 4. Cập nhật trạng thái nút Đăng Nhập
                updateLoginButtonState()
            }
        })
    }
}

