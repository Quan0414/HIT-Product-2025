package com.example.hitproduct.screen.home_page.setting.account_setting

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentAccountSettingBinding
import com.example.hitproduct.screen.dialog.start_date.DialogStartDate.ValidationResult
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import io.getstream.avatarview.glide.loadImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class AccountSettingFragment : BaseFragment<FragmentAccountSettingBinding>() {

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

    private val viewModel by viewModels<AccountSettingViewmodel> {
        AccountSettingViewModelFactory(authRepo)
    }

    private var isEditMode = false

    private var selectedAvatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedAvatarUri = it
            binding.imgAvatar.loadImage(it)    // preview
        }
    }

    override fun initView() {
// Khởi đầu: disable tất cả, ẩn icon
        toggleFields(enabled = false)


        val genders = listOf("Nam", "Nữ", "Khác")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_gender,
            genders
        )
        binding.actvGender.setAdapter(adapter)
        binding.actvGender.threshold = 0

        binding.tilGender.setEndIconOnClickListener {
            binding.actvGender.showDropDown()
        }

//        binding.actvGender.setOnItemClickListener { parent, _, position, _ ->
//            val selected = parent.getItemAtPosition(position) as String
//            binding.actvGender.setText(selected, false)
//        }
    }

    override fun initListener() {
        binding.btnEditProfile.setOnClickListener {
            if (!isEditMode) {
                // Chuyển từ view-only sang edit mode
                isEditMode = true
                toggleFields(enabled = true)
                binding.btnEditProfile.text = "Lưu"
                return@setOnClickListener
            }
            val birthday = binding.edtBirthday.text.toString().takeIf { it.isNotBlank() }
            val validationResult = birthday?.let { validateDate(it) }
            if (validationResult != null && !validationResult.isValid) {
                Toast.makeText(requireContext(), validationResult.errorMessage, Toast.LENGTH_SHORT).show()
                // Giữ nguyên edit mode
                return@setOnClickListener
            }
            val firstName  = binding.edtHo.text.toString().takeIf { it.isNotBlank() }
            val lastName   = binding.edtTen.text.toString().takeIf { it.isNotBlank() }
            val nickname   = binding.edtNickname.text.toString().takeIf { it.isNotBlank() }
            val gender     = binding.actvGender.text.toString().takeIf { it.isNotBlank() }
            val avatarUri  = selectedAvatarUri

            viewModel.updateProfile(
                firstName, lastName, nickname,
                gender, birthday, avatarUri, requireContext()
            )

        }

        binding.btnEditAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.edtBirthday.setOnClickListener {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(childFragmentManager, "love_date_picker")

            datePicker.addOnPositiveButtonClickListener { selection: Long ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selection
                }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = cal.get(Calendar.MONTH) + 1
                val year = cal.get(Calendar.YEAR)
                val formatted = String.format("%02d/%02d/%04d", day, month, year)
                binding.edtBirthday.text = formatted
            }
        }

    }

    override fun initData() {
        viewModel.fetchUserProfile()
    }

    override fun handleEvent() {

    }

    override fun bindData() {

        viewModel.userProfileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                }

                UiState.Idle -> {
                }

                UiState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    val user = state.data
                    // đổ data lên form
                    binding.tvName.text = user.username
                    binding.tvNickname1.text = user.nickname
                    binding.edtHo.setText(user.firstName)
                    binding.edtTen.setText(user.lastName)
                    binding.edtEmail.setText(user.email)
                    binding.edtNickname.setText(user.nickname)
                    binding.actvGender.setText(user.gender, false)
                    binding.edtBirthday.text = user.dateOfBirth.toDisplayDate()

                    val avatarUrl = user.avatar
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirst("http://", "https://")
                        ?: R.drawable.avatar_default
                    if (avatarUrl != "/example.png") {
                        binding.imgAvatar.loadImage(avatarUrl)
                    } else {
                        binding.imgAvatar.loadImage(R.drawable.avatar_default)
                    }
                    binding.loadingProgressBar.visibility = View.GONE
                }
            }
        }
        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    isEditMode = false
                    toggleFields(enabled = false)
                    binding.btnEditProfile.text = "Sửa thông tin"
                    selectedAvatarUri = null
                }

                is UiState.Error -> {
                    // Hiển thị thông báo lỗi
                    Toast.makeText(requireContext(), "Lỗi: ${state.error}", Toast.LENGTH_SHORT)
                        .show()
                }

                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.hideBottomNav()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as? MainActivity)?.showBottomNav()
    }

    private fun toggleFields(enabled: Boolean) {

        binding.btnEditAvatar.visibility = if (enabled) View.VISIBLE else View.INVISIBLE

        val fields = listOf(
            binding.edtHo,
            binding.edtTen,
            binding.edtNickname,
            binding.actvGender,
            binding.edtBirthday
        )

        fields.forEach { field ->
            field.isEnabled = enabled
        }

        // Đối với ô giới tính:
        if (isEditMode) {
            // Hiện dropdown icon
            binding.tilGender.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            // hoặc với M3+: binding.tilGender.isEndIconVisible = true
        } else {
            // Ẩn icon
            binding.tilGender.endIconMode = TextInputLayout.END_ICON_NONE
            // hoặc: binding.tilGender.isEndIconVisible = false
        }

        val editIconRes = if (enabled) R.drawable.ic_edit_text else 0
        listOf(
            binding.edtHo,
            binding.edtTen,
            binding.edtNickname,
            binding.edtBirthday
        ).forEach { et ->
            // clear và set lại drawable: left, top, right, bottom
            et.setCompoundDrawablesRelativeWithIntrinsicBounds(
                /* start */ 0,
                /* top   */ 0,
                /* end   */ editIconRes,
                /* bottom*/ 0
            )
        }

    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAccountSettingBinding {
        return FragmentAccountSettingBinding.inflate(inflater, container, false)
    }

    private fun String?.toDisplayDate(): String {
        if (this.isNullOrBlank()) return ""
        return try {
            // parser chuỗi UTC
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(this)!!

            // formatter local
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            this
        }
    }

    private fun validateDate(dateString: String): ValidationResult {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.isLenient = false // Không cho phép ngày không hợp lệ như 32/13/2023
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val inputDate =
                inputFormat.parse(dateString) ?: return ValidationResult(
                    false,
                    "Ngày sinh không hợp lệ"
                )

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
                        "Ngày sinh không hợp lệ"
                    )
                }

                inputDate.after(today) -> {
                    ValidationResult(false, "Ngày sinh không hợp lệ")
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