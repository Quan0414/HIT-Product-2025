package com.example.hitproduct.screen.authentication.register.set_up_infor

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import com.example.hitproduct.screen.authentication.register.success.SuccessCreateAccFragment
import com.example.hitproduct.screen.dialog.start_date.DialogStartDate.ValidationResult
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class SetUpInformationFragment : Fragment() {

    private lateinit var binding: FragmentSetUpInformationBinding

    // 1. SharedPreferences
    private val prefs by lazy {
        requireContext()
            .getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 2. AuthRepository
    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    // 3. ViewModel
    private val viewModel by viewModels<SetUpInformationViewModel> {
        SetUpInformationViewModelFactory(authRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetUpInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //observer viewModel
        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    binding.tvContinue.isEnabled = true
                    val err = state.error
                    Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
                }

                UiState.Idle -> {
                }

                UiState.Loading -> {
                    binding.tvContinue.isEnabled = false
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Chuyển đến SuccessCreateAccFragment
                    val successCreateAccFragment = SuccessCreateAccFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                        .replace(R.id.fragmentStart, successCreateAccFragment)
                        .commit()
                }
            }
        }

        binding.tvBirthday.setOnClickListener {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(childFragmentManager, "love_date_picker")

            datePicker.addOnPositiveButtonClickListener { selection: Long ->
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = selection
                }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = cal.get(Calendar.MONTH) + 1
                val year = cal.get(Calendar.YEAR)
                val formatted = String.format("%02d/%02d/%04d", day, month, year)
                binding.tvBirthday.text = formatted
            }

        }


        binding.tvContinue.setOnClickListener {
            val inputDate = binding.tvBirthday.text.toString().takeIf { it.isNotBlank() }
            val validationResult = inputDate?.let { it1 -> validateDate(it1) }
            if (validationResult != null) {
                if (!validationResult.isValid) {
                    Toast.makeText(
                        requireContext(),
                        validationResult.errorMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }
            }

            val firstName = binding.edtHo.text.toString().takeIf { it.isNotBlank() }
            val lastName = binding.edtTen.text.toString().takeIf { it.isNotBlank() }
            val nickName = binding.edtNickname.text.toString().takeIf { it.isNotBlank() }
            val gender = binding.actvGender.text.toString().takeIf { it.isNotBlank() }
            val dateOfBirth = inputDate.toSendDate()

            // Gọi updateProfile trong ViewModel
            viewModel.updateProfile(
                firstName, lastName, nickName,
                gender, dateOfBirth
            )
        }

        //chọn giới tính
        // 1. Data
        val genders = listOf("Nam", "Nữ", "Khác")

        // 2. Adapter
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_gender,
            genders
        )
        binding.actvGender.setAdapter(adapter)
        binding.actvGender.threshold = 0

        // 3. Show dropdown khi click icon
        binding.tilGender.setEndIconOnClickListener {
            binding.actvGender.showDropDown()
        }

        // 4. Đẩy text vào ô sau khi chọn
        binding.actvGender.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            binding.actvGender.setText(selected, false)
        }


        //ngày sinh
        val editText = binding.tvBirthday

//        editText.addTextChangedListener(object : TextWatcher {
//            private var isUpdating = false
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: Editable) {}
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                if (isUpdating) {
//                    isUpdating = false
//                    return
//                }
//
//                // Lọc chỉ giữ chữ số
//                val digits = s.toString().filter { it.isDigit() }
//                val sb = StringBuilder()
//
//                for ((index, char) in digits.withIndex()) {
//                    sb.append(char)
//                    // chèn "/" sau 2 và 4 chữ số
//                    if ((index == 1 || index == 3) && index != digits.lastIndex) {
//                        sb.append('/')
//                    }
//                    // giới hạn max dd/MM/yyyy = 10 ký tự
//                    if (sb.length >= 10) break
//                }
//
//                isUpdating = true
//                editText.setText(sb)
//            }
//        })


        //skip button
        binding.tvSkip.setOnClickListener {
            val successCreateAccFragment = SuccessCreateAccFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentStart, successCreateAccFragment)
                .commit()
        }

        //nut back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


    }

    private fun validateDate(dateString: String): ValidationResult {
//        if (dateString.isBlank()) {
//            return ValidationResult(
//                false,
//                "Ê ê! Nhập ngày đi bạn ơi,  bộ bạn không nhớ ngày bắt đầu yêu nhau hả :)))"
//            )
//        }

//        if (dateString.length != 10) {
//            return ValidationResult(false, "Vui lòng nhập đầy đủ ngày theo định dạng dd/MM/yyyy")
//        }

        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.isLenient = false // Không cho phép ngày không hợp lệ như 32/13/2023
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val inputDate =
                inputFormat.parse(dateString) ?: return ValidationResult(false, "Ngày không hợp lệ")

            // Tính toán ngày giới hạn (200 năm trước)
            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getTimeZone("UTC")
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