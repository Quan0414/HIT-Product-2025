package com.example.hitproduct.screen.authentication.create_pin

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.hitproduct.screen.dialog.confirm_pin.DialogConfirmPin
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.CryptoHelper
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentCreatePinBinding
import com.example.hitproduct.screen.authentication.login.LoginActivity
import com.example.hitproduct.screen.authentication.send_invite_code.SendInviteCodeFragment

class CreatePinFragment : BaseFragment<FragmentCreatePinBinding>() {

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }
    private val viewModel by viewModels<CreatePinViewModel> {
        CreatePinViewModelFactory(authRepo)
    }

    private val flow by lazy {
        arguments?.getString("flow") ?: throw IllegalArgumentException("Flow argument is required")
    }

    // 1. Tạo list chứa 6 EditText
    private lateinit var codes: List<EditText>

    override fun initView() {
        // Khởi tạo list
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

        // 3. Xử lý nút Continue
        binding.tvContinue.setOnClickListener {
            if (flow == "reset-pin") {
                DialogConfirmPin {
                    // Sinh key pair mới
                    CryptoHelper.ensureKeyPair(requireContext())
                    val myPub = CryptoHelper.getMyPublicKey(requireContext())
                    Log.d("CreatePinFragment", "Reset - My public key: $myPub")

                    val pin = codes.joinToString("") { it.text.toString() }
                    CryptoHelper.encryptPrivateKeyWithPin(requireContext(), pin)

                    val encryptedB64 = CryptoHelper.getEncryptedPrivateKeyB64(requireContext())
                    Log.d("CreatePinFragment", "Reset - Encrypted Private Key: $encryptedB64")
                    viewModel.sendKey(myPub, encryptedB64)
                }.show(parentFragmentManager, "confirm_reset_pin_dialog")
            } else {
                DialogConfirmPin {
                    // Callback khi bấm "Đồng ý"
                    val myPub = CryptoHelper.getMyPublicKey(requireContext())
                    Log.d("CreatePinFragment", "My Public Key: $myPub")

                    val pin = codes.joinToString("") { it.text.toString() }
                    CryptoHelper.encryptPrivateKeyWithPin(requireContext(), pin)

                    val encryptedB64 = CryptoHelper.getEncryptedPrivateKeyB64(requireContext())
                    Log.d("CreatePinFragment", "Encrypted Private Key: $encryptedB64")
                    viewModel.sendKey(myPub, encryptedB64)


                }.show(parentFragmentManager, "confirm_pin_dialog")
            }

        }
    }

    override fun initData() {
    }

    override fun handleEvent() {
    }

    override fun bindData() {
        viewModel.sendKeyState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.tvContinue.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        state.error.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                UiState.Idle -> {}
                UiState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                    binding.tvContinue.isEnabled = false
                }

                is UiState.Success -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.tvContinue.isEnabled = true

                    if (flow == "reset-pin") {
                        Toast.makeText(
                            requireContext(),
                            "Đặt lại mã pin thành công. Vui lòng đăng nhập lại.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    else{
                        Toast.makeText(
                            requireContext(),
                            "Tạo mã pin thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        val sendInviteCodeFragment = SendInviteCodeFragment()
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                            )
                            .replace(R.id.fragmentStart, sendInviteCodeFragment)
                            .commit()
                    }

                }
            }
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreatePinBinding {
        return FragmentCreatePinBinding.inflate(inflater, container, false)
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
