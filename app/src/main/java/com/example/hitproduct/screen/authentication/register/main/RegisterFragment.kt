package com.example.hitproduct.screen.authentication.register.main

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hitproduct.R
import com.example.hitproduct.databinding.FragmentRegisterBinding
import com.example.hitproduct.screen.authentication.verify_code.VerifyCodeFragment


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gán ảnh dựa vào biến ngay khi view được tạo
        binding.imgCheckBox.setImageResource(
            if (isCheckBoxChecked) R.drawable.ic_checked else R.drawable.ic_unchecked
        )

        //nut back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        //Xu ly string
        val text =
            "Tôi đã đọc và đồng ý với Điều khoản và Điều kiện cũng như Chính sách bảo mật"
        val spannableString = SpannableStringBuilder(text)
        val orange = ContextCompat.getColor(requireContext(), R.color.orange)

        val start1 = text.indexOf("Điều khoản và Điều kiện")
        val end1 = start1 + "Điều khoản và Điều kiện".length

        val start2 = text.indexOf("Chính sách bảo mật")
        val end2 = start2 + "Chính sách bảo mật".length

        spannableString.setSpan(
            ForegroundColorSpan(orange),
            start1, end1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(orange),
            start2, end2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvCheckBox.text = spannableString



        setUpListeners()
        updateRegisterButtonState()


        //An hien password
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


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private var isPasswordVisible1 = false
    private var isPasswordVisible2 = false

    private var isCheckBoxChecked = false

    private fun isFormValid(): Boolean {
        val ten = binding.edtTenNguoiDung.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val pass1 = binding.edtPassword1.text.toString()
        val pass2 = binding.edtPassword2.text.toString()

        // 1. Clear error cũ
        binding.edtPassword2.error = null

        // 2. Nếu cả 2 ô có text nhưng không khớp, show error
        if (pass1.isNotEmpty() && pass2.isNotEmpty() && pass1 != pass2) {
            binding.edtPassword2.setError("Mật khẩu không khớp", null)
        }

        return ten.isNotEmpty() && email.isNotEmpty()
                && pass1.isNotEmpty() && pass2.isNotEmpty()
                && pass1 == pass2 && isCheckBoxChecked
    }

    private fun updateRegisterButtonState() {
        val enabled = isFormValid()
        binding.tvRegister.isEnabled = enabled
        binding.tvRegister.setBackgroundResource(
            if (enabled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }

    private fun setUpListeners() {
        // TextWatcher
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateRegisterButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.edtTenNguoiDung.addTextChangedListener(watcher)
        binding.edtEmail.addTextChangedListener(watcher)
        binding.edtPassword1.addTextChangedListener(watcher)
        binding.edtPassword2.addTextChangedListener(watcher)

        // Checkbox bằng ImageView
        binding.imgCheckBox.setOnClickListener {
            isCheckBoxChecked = !isCheckBoxChecked
            binding.imgCheckBox.setImageResource(
                if (isCheckBoxChecked) R.drawable.ic_checked else R.drawable.ic_unchecked
            )
            updateRegisterButtonState()
        }


        // Khi bấm nút Đăng ký
        binding.tvRegister.setOnClickListener {
            if (binding.tvRegister.isEnabled) {
                val verifyCodeFragment = VerifyCodeFragment().apply {
                    arguments = Bundle().apply { putString("flow", "register") }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentStart, verifyCodeFragment)
                    .addToBackStack("Register")
                    .commit()
            }
        }
    }

}