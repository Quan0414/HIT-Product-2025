package com.example.hitproduct.screen.authentication.login

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hitproduct.R
import com.example.hitproduct.databinding.FragmentLoginBinding
import com.example.hitproduct.screen.authentication.forgot_method.FindAccFragment
import com.example.hitproduct.screen.authentication.register.RegisterFragment

class LoginFragment : Fragment() {

    // 1. Khai báo nullable _binding
    private var _binding: FragmentLoginBinding? = null

    // 2. Exposed non-nullable binding
    private val binding get() = _binding!!

    private var isPasswordVisible = false

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
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) = updateLoginButtonState()
        }
        binding.edtEmail.addTextChangedListener(watcher)
        binding.edtPassword.addTextChangedListener(watcher)

        //...
    }
}
