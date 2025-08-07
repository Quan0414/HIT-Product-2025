package com.example.hitproduct.screen.dialog.start_date

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.FcmClient
import com.example.hitproduct.common.util.NotificationConfig
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.DialogStartDateBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class DialogStartDate : DialogFragment() {
    private var _binding: DialogStartDateBinding? = null
    private val binding get() = _binding!!

    private lateinit var token: String

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

        binding.btnArrowDown.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Ngày Bắt Đầu Tình Yêu")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(childFragmentManager, "love_date_picker")

            datePicker.addOnPositiveButtonClickListener { selection: Long ->
                val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
                    timeInMillis = selection
                }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = cal.get(Calendar.MONTH) + 1
                val year = cal.get(Calendar.YEAR)
                val formatted = String.format("%02d/%02d/%04d", day, month, year)
                binding.etStartDate.text = formatted
            }

        }

        binding.btnContinue.setOnClickListener {
            if (!binding.btnContinue.isEnabled) return@setOnClickListener
            val inputDate = editText.text.toString()

            // Validate ngày trước khi gửi
            val validationResult = validateDate(inputDate)
            if (!validationResult.isValid) {
                Toast.makeText(requireContext(), validationResult.errorMessage, Toast.LENGTH_SHORT)
                    .show()
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

                    val myLoveId = authRepo.getMyLoveId()
                    val chosenDate = binding.etStartDate.text.toString()
                    val payload = mapOf(
                        "type" to "start_date_selected",
                        "startDate" to chosenDate
                    )
                    val tpl = NotificationConfig.getTemplate("start_date_selected", payload)
                    FcmClient.sendToTopic(
                        receiverUserId = myLoveId,
                        title = tpl.title,
                        body = tpl.body,
                        data = payload
                    )
                }
            }
        }

        viewModel.dismissDialog.observe(viewLifecycleOwner) {
            dismiss()
            parentFragmentManager.setFragmentResult("update_start_date", Bundle())
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    )

    private fun validateDate(dateString: String): ValidationResult {
        if (dateString.isBlank()) {
            return ValidationResult(
                false,
                "Ê ê! Nhập ngày đi bạn ơi,  bộ bạn không nhớ ngày bắt đầu yêu nhau hả :)))"
            )
        }

//        if (dateString.length != 10) {
//            return ValidationResult(false, "Vui lòng nhập đầy đủ ngày theo định dạng dd/MM/yyyy")
//        }

        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
                isLenient = false
            }

            val inputDate =
                inputFormat.parse(dateString) ?: return ValidationResult(false, "Ngày không hợp lệ")

            // Tính toán ngày giới hạn (200 năm trước)
            val calendar = Calendar.getInstance()
            val today = calendar.time

            calendar.add(Calendar.YEAR, -200)
            val minDate = calendar.time

            when {
                inputDate.before(minDate) -> {
                    ValidationResult(
                        false,
                        "Ôi dồi ôi! Bạn sinh ra từ thời khủng long à? Chọn ngày gần đây hơn đi!"
                    )
                }

                inputDate.after(today) -> {
                    ValidationResult(false, "Chưa đến ngày đó mà! Chọn lại đi bạn ơi~")
                }

                else -> ValidationResult(true)
            }
        } catch (e: Exception) {
            ValidationResult(
                false,
                "Định dạng ngày không đúng. Vui lòng nhập theo định dạng dd/MM/yyyy"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun String?.toSendDate(): String {
        if (isNullOrBlank()) return ""
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(this) ?: return ""
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return outputFormat.format(date)
    }
}