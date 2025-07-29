package com.example.hitproduct.screen.authentication.forgot_method.find_acc

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentFindAccBinding
import com.example.hitproduct.screen.authentication.login.LoginViewModel
import com.example.hitproduct.screen.authentication.login.LoginViewModelFactory
import com.example.hitproduct.screen.authentication.register.main.RegisterFragment
import com.example.hitproduct.screen.authentication.register.main.RegisterViewModel
import com.example.hitproduct.screen.authentication.register.main.RegisterViewModelFactory
import com.example.hitproduct.screen.authentication.verify_code.VerifyCodeFragment
import kotlinx.coroutines.launch


class FindAccFragment : BaseFragment<FragmentFindAccBinding>() {

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

    private lateinit var spannableString: SpannableString
    private lateinit var clickableSpan: ClickableSpan

    override fun initView() {

        // Lấy TextView từ binding
        val text = binding.tvRegister
        spannableString = SpannableString(text.text)

        // Tạo một ClickableSpan cho phần văn bản nhấn vào
        clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragmentStart, RegisterFragment().apply {
                        arguments = Bundle().apply { putString("flow", "register") }
                    })
                    .addToBackStack(null)
                    .commit()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.orange)
                ds.isUnderlineText = false
            }
        }

        // Gán trước phần format
        spannableString.setSpan(
            clickableSpan,
            19, 31,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text.text = spannableString
        text.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun initListener() {
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setUpListeners()
        updateFindAccButtonState()
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
    ): FragmentFindAccBinding {
        return FragmentFindAccBinding.inflate(inflater, container, false)
    }


    private fun isFormValid(): Boolean {
        val email = binding.edtEmail.text.toString().trim()
        return email.isNotEmpty()
    }

    private fun updateFindAccButtonState() {
        val enabled = isFormValid()
        binding.tvFindAcc.isEnabled = enabled
        binding.tvFindAcc.setBackgroundResource(
            if (enabled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }

    private fun setUpListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) = updateFindAccButtonState()

        }

        binding.edtEmail.addTextChangedListener(watcher)


        //chuyen qua verify code
        binding.tvFindAcc.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            lifecycleScope.launch {
                when (val res = authRepo.findAccount(email)) {
                    is DataResult.Error -> {
                        Toast.makeText(
                            requireContext(),
                            res.error.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is DataResult.Success -> {
                        val verifyCodeFragment = VerifyCodeFragment().apply {
                            arguments = Bundle().apply {
                                putString("email", email)
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
                            .replace(R.id.fragmentStart, verifyCodeFragment)
                            .addToBackStack("FindAcc")
                            .commit()
                    }
                }
            }
        }

    }


}