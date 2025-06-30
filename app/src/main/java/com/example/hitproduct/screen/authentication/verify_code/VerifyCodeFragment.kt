package com.example.hitproduct.screen.authentication.verify_code

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.hitproduct.R
import com.example.hitproduct.databinding.FragmentVerifyCodeBinding
import com.example.hitproduct.screen.authentication.forgot_method.create_new_pass.CreateNewPasswordFragment
import com.example.hitproduct.screen.authentication.register.set_up_infor.SetUpInformationFragment


class VerifyCodeFragment : Fragment() {

    private var _binding: FragmentVerifyCodeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerifyCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val codes = listOf(
            binding.edtCode1,
            binding.edtCode2,
            binding.edtCode3,
            binding.edtCode4
        )

        val btnContinue = binding.tvContinue

        fun updateButtonState() {
            // Nếu tất cả ô đều đã nhập 1 ký tự thì enable và đổi màu
            val allFilled = codes.all { it.text?.length == 1 }
            if (allFilled) {
                btnContinue.isEnabled = true
                btnContinue.setBackgroundResource(R.drawable.bg_enable_btn)
            } else {
                btnContinue.isEnabled = false
                btnContinue.setBackgroundResource(R.drawable.bg_disable_btn)
            }
        }

        // 1. Thêm TextWatcher cho từng EditText
        codes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        // Nếu chưa phải ô cuối cùng, chuyển focus
                        if (index < codes.size - 1) {
                            codes[index + 1].requestFocus()
                        }
                    }
                    updateButtonState()
                }
            })

            // 2. Bắt phím DEL để quay lại ô trước
            editText.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL
                    && editText.text.isEmpty()
                    && index > 0
                ) {
                    codes[index - 1].requestFocus()
                    codes[index - 1].text = null
                    updateButtonState()
                    true
                } else {
                    false
                }
            }
        }

        updateButtonState()


        //nut back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        //chuyen man


        binding.tvContinue.setOnClickListener {
            val flow = arguments?.getString("flow")
            if (flow == "register") {
                // Chuyển sang fragment thiết lập tài khoản
                val setUpFragment = SetUpInformationFragment()
                parentFragmentManager.popBackStack("Register", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentStart, setUpFragment)
                    .addToBackStack(null)
                    .commit()
            } else if (flow == "forgot") {
                // Chuyển sang fragment tạo mật khẩu mới
                val createNewPasswordFragment = CreateNewPasswordFragment()
                parentFragmentManager.popBackStack("EnterEmail", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentStart, createNewPasswordFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}