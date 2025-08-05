package com.example.hitproduct.screen.authentication.verify_pin

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.util.CryptoHelper
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentVerifyPinBinding
import com.example.hitproduct.screen.authentication.create_pin.CreatePinFragment
import com.example.hitproduct.screen.authentication.login.LoginViewModel
import com.example.hitproduct.screen.authentication.login.LoginViewModelFactory


class VerifyPinFragment : BaseFragment<FragmentVerifyPinBinding>() {

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
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

    private lateinit var codes: List<EditText>

    companion object {
        private const val MAX_ATTEMPTS = 5
    }
    private var remainingAttempts = MAX_ATTEMPTS

    override fun initView() {
        codes = listOf(
            binding.edtCode1, binding.edtCode2, binding.edtCode3,
            binding.edtCode4, binding.edtCode5, binding.edtCode6
        )
        // Ban đầu check button continue
        updateContinueState()
    }

    override fun initListener() {
        // 2. Gắn TextWatcher & OnKeyListener cho từng ô
        codes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // Reset background nếu bạn có style riêng
                    codes.forEach { it.setBackgroundResource(R.drawable.bg_pin) }

                    // Nếu đã nhập 1 ký tự thì tự focus sang ô tiếp
                    if (s?.length == 1 && index < codes.lastIndex) {
                        codes[index + 1].requestFocus()
                    }
                    updateContinueState()
                }

                override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                // Bắt phím xoá để quay lại ô trước
                if (keyCode == KeyEvent.KEYCODE_DEL
                    && event.action == KeyEvent.ACTION_DOWN
                    && editText.text.isEmpty()
                    && index > 0
                ) {
                    val prev = codes[index - 1]
                    prev.apply {
                        // Xoá ký tự cuối cùng (nếu có)
                        if (text.isNotEmpty()) {
                            text.delete(text.length - 1, text.length)
                        }
                        requestFocus()
                        setSelection(text.length)
                    }
                    updateContinueState()
                    true
                } else false
            }
        }

        binding.tvContinue.setOnClickListener {
            val pin = codes.joinToString("") { it.text.toString() }

            try {
                // 1) Thử giải mã private key với PIN
                val rawPriv = CryptoHelper.decryptPrivateKeyWithPin(requireContext(), pin)

                // 2) Nếu thành công, restore raw key và lưu LAST_USER_ID
                CryptoHelper.restorePrivateKey(requireContext(), rawPriv)
                val currentId = prefs.getString(AuthPrefersConstants.MY_USER_ID, "").orEmpty()
                prefs.edit()
                    .putString(AuthPrefersConstants.LAST_USER_ID, currentId)
                    .apply()

                // 3) Tiếp tục flow: gọi lại profile để vào SendInvite hoặc MainActivity
                viewModel.checkProfile()
                parentFragmentManager.popBackStack()
            }
            catch (e: javax.crypto.AEADBadTagException) {
                // PIN sai
                remainingAttempts--
                if (remainingAttempts > 0) {
                    Toast.makeText(
                        requireContext(),
                        "PIN không đúng, bạn còn $remainingAttempts lần thử",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Bạn đã nhập sai quá $MAX_ATTEMPTS lần. Vui lòng đặt lại mã pin.",
                        Toast.LENGTH_LONG
                    ).show()
                    // Xóa sạch mọi key
                    CryptoHelper.deleteAllKeys(requireContext())

                    // Sau khi deleteAllKeys()
                    val resetFragment = CreatePinFragment().apply {
                        arguments = Bundle().apply {
                            putString("flow", "reset-pin")
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                        .replace(R.id.fragmentStart, resetFragment)
                        .commit()
                }

                updateContinueState()
            }
            catch (e: Exception) {
                // Lỗi khác
                Toast.makeText(
                    requireContext(),
                    "Có lỗi xảy ra, vui lòng thử lại",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
    ): FragmentVerifyPinBinding {
        return FragmentVerifyPinBinding.inflate(inflater, container, false)
    }

    // 4. Hàm tiện ích để enable/disable nút Continue
    private fun updateContinueState() {
        val allFilled = codes.all { it.text?.length == 1 }
        binding.tvContinue.isEnabled = allFilled
        binding.tvContinue.setBackgroundResource(
            if (allFilled) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }
}