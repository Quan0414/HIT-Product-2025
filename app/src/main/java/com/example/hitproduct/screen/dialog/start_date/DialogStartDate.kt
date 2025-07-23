package com.example.hitproduct.screen.dialog.start_date

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogStartDateBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class DialogStartDate : DialogFragment() {
    private var _binding: DialogStartDateBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by viewModels<DialogStartDateViewModel> {
        DialogStartDateViewModelFactory(authRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1) Vô hiệu hoá back-press
        isCancelable = false
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStartDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = binding.etStartDate
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                // Lọc chỉ giữ chữ số
                val digits = s.toString().filter { it.isDigit() }
                val sb = StringBuilder()

                for ((index, char) in digits.withIndex()) {
                    sb.append(char)
                    // chèn "/" sau 2 và 4 chữ số
                    if ((index == 1 || index == 3) && index != digits.lastIndex) {
                        sb.append('/')
                    }
                    // giới hạn max dd/MM/yyyy = 10 ký tự
                    if (sb.length >= 10) break
                }

                isUpdating = true
                editText.setText(sb.toString())
                editText.setSelection(sb.length)
                isUpdating = false
            }
        })

        binding.btnContinue.setOnClickListener {
            if (!binding.btnContinue.isEnabled) return@setOnClickListener
            val inputDate = editText.text.toString()

            // Validate ngày trước khi gửi
            val validationResult = validateDate(inputDate)
            if (!validationResult.isValid) {
                Toast.makeText(requireContext(), validationResult.errorMessage, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnContinue.isEnabled = false
            val startDate = inputDate.toSendDate()
            viewModel.chooseStartDate(startDate)
        }

        viewModel.chooseStartDateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_SHORT).show()
                    binding.btnContinue.isEnabled = true
                }

                UiState.Idle -> {}
                UiState.Loading -> {
                    binding.btnContinue.isEnabled = false
                }
                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thông tin thành công.",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                    parentFragmentManager.setFragmentResult("update_start_date", Bundle())
                }
            }
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    )

    private fun validateDate(dateString: String): ValidationResult {
        if (dateString.isBlank()) {
            return ValidationResult(false, "Ê ê! Nhập ngày đi bạn ơi,  bộ bạn không nhớ ngày bắt đầu yêu nhau hả :)))")
        }

        if (dateString.length != 10) {
            return ValidationResult(false, "Vui lòng nhập đầy đủ ngày theo định dạng dd/MM/yyyy")
        }

        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.isLenient = false // Không cho phép ngày không hợp lệ như 32/13/2023
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val inputDate = inputFormat.parse(dateString) ?: return ValidationResult(false, "Ngày không hợp lệ")

            // Tính toán ngày giới hạn (200 năm trước)
            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getTimeZone("UTC")
            val today = calendar.time

            calendar.add(Calendar.YEAR, -200)
            val minDate = calendar.time

            when {
                inputDate.before(minDate) -> {
                    ValidationResult(false, "Ôi dồi ôi! Bạn sinh ra từ thời khủng long à? Chọn ngày gần đây hơn đi!")
                }
                inputDate.after(today) -> {
                    ValidationResult(false, "Chưa đến ngày đó mà! Chọn lại đi bạn ơi~")
                }
                else -> ValidationResult(true)
            }
        } catch (e: Exception) {
            ValidationResult(false, "Định dạng ngày không đúng. Vui lòng nhập theo định dạng dd/MM/yyyy")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun String?.toSendDate(): String {
        if (this.isNullOrBlank()) return ""
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(this) ?: return ""
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date)
        } catch (e: Exception) {
            this
        }
    }
}