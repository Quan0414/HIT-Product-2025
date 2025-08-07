package com.example.hitproduct.screen.authentication.register.set_up_infor

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSetUpInformationBinding
import com.example.hitproduct.screen.authentication.create_pin.CreatePinFragment
import com.example.hitproduct.screen.dialog.start_date.DialogStartDate.ValidationResult
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class SetUpInformationFragment : Fragment() {

    private lateinit var binding: FragmentSetUpInformationBinding

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

    private val viewModel by viewModels<SetUpInformationViewModel> {
        SetUpInformationViewModelFactory(authRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetUpInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe updateState
        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvContinue.isEnabled = false
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT)
                        .show()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                        .replace(
                            R.id.fragmentStart,
                            CreatePinFragment().apply {
                                arguments = Bundle().apply {
                                    putString("flow", "create-pin")
                                }
                            }
                        )
                        .commit()
                }
                is UiState.Error -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.tvContinue.isEnabled = true
                    Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_SHORT).show()
                }
                UiState.Idle -> { /* no-op */ }
            }
        }

        // TextWatcher for first name, last name, nickname
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateContinueButtonState()
            }
        }
        binding.edtHo.addTextChangedListener(watcher)
        binding.edtTen.addTextChangedListener(watcher)
        binding.edtNickname.addTextChangedListener(watcher)

        // Gender dropdown
        val genders = listOf("Nam", "Nữ", "Khác")
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_gender, genders)
        binding.actvGender.setAdapter(genderAdapter)
        binding.actvGender.threshold = 0
        binding.tilGender.setEndIconOnClickListener {
            binding.actvGender.showDropDown()
        }
        binding.actvGender.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            binding.actvGender.setText(selected, false)
            updateContinueButtonState()
        }

        // Birthday picker
        binding.tvBirthday.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.show(childFragmentManager, "date_picker")
            picker.addOnPositiveButtonClickListener { ts ->
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                val formatted = String.format(
                    Locale.getDefault(),
                    "%02d/%02d/%04d",
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR)
                )
                binding.tvBirthday.text = formatted
                updateContinueButtonState()
            }
        }

        // Continue button action
        binding.tvContinue.setOnClickListener {
            val inputDate = binding.tvBirthday.text.toString().takeIf { it.isNotBlank() }
            inputDate?.let {
                val result = validateDate(it)
                if (!result.isValid) {
                    Toast.makeText(requireContext(), result.errorMessage, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            viewModel.updateProfile(
                binding.edtHo.text.toString().takeIf { it.isNotBlank() },
                binding.edtTen.text.toString().takeIf { it.isNotBlank() },
                binding.edtNickname.text.toString().takeIf { it.isNotBlank() },
                binding.actvGender.text.toString().takeIf { it.isNotBlank() },
                binding.tvBirthday.text.toString().toSendDate()
            )
        }

        // Skip
        binding.tvSkip.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentStart, CreatePinFragment())
                .commit()
        }

        // Back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Initialize button state
        updateContinueButtonState()
    }

    /** Enable Continue only if at least one field is filled */
    private fun updateContinueButtonState() {
        val enable = binding.edtHo.text.isNotBlank() ||
                binding.edtTen.text.isNotBlank() ||
                binding.edtNickname.text.isNotBlank() ||
                binding.actvGender.text.isNotBlank() ||
                binding.tvBirthday.text.isNotBlank()
        binding.tvContinue.isEnabled = enable
        binding.tvContinue.setBackgroundResource(
            if (enable) R.drawable.bg_enable_btn else R.drawable.bg_disable_btn
        )
    }

    // Không thay đổi hàm validateDate
    private fun validateDate(dateString: String): ValidationResult {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.isLenient = false
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val inputDate =
                inputFormat.parse(dateString) ?: return ValidationResult(false, "Ngày sinh không hợp lệ")

            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getTimeZone("UTC")
            val today = calendar.time

            calendar.add(Calendar.YEAR, -200)
            val minDate = calendar.time

            when {
                inputDate.before(minDate) ->
                    ValidationResult(false, "Ngày sinh không hợp lệ")
                inputDate.after(today) ->
                    ValidationResult(false, "Ngày sinh không hợp lệ")
                else ->
                    ValidationResult(true)
            }
        } catch (e: Exception) {
            ValidationResult(
                false,
                "Định dạng ngày không đúng. Vui lòng nhập theo định dạng dd/MM/yyyy"
            )
        }
    }

    // Không thay đổi hàm toSendDate
    private fun String?.toSendDate(): String {
        if (this.isNullOrBlank()) return ""
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(this) ?: return ""
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date)
        } catch (e: Exception) {
            this ?: ""
        }
    }
}
