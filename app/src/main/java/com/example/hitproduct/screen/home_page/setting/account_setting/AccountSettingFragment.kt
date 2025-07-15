package com.example.hitproduct.screen.home_page.setting.account_setting

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentAccountSettingBinding
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
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
            binding.imgAvatar.setImageURI(it)    // preview
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

        binding.actvGender.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            binding.actvGender.setText(selected, false)
        }
    }

    override fun initListener() {
        binding.btnEditProfile.setOnClickListener {
            isEditMode = !isEditMode
            toggleFields(enabled = isEditMode)

            // Đổi text nút
            binding.btnEditProfile.text = if (isEditMode) "Lưu" else "Sửa thông tin"

            if (isEditMode) {
                //api
                toggleFields(enabled = true)
                binding.btnEditProfile.text = "Lưu"
            } else {
                binding.btnEditProfile.text = "Sửa thông tin"
                toggleFields(enabled = false)

                val firstName = binding.edtHo.text.toString().takeIf { it.isNotBlank() }
                val lastName = binding.edtTen.text.toString().takeIf { it.isNotBlank() }
                val nickname = binding.edtNickname.text.toString().takeIf { it.isNotBlank() }
                val gender = binding.actvGender.text.toString().takeIf { it.isNotBlank() }
                val birthday = binding.edtBirthday.text.toString().takeIf { it.isNotBlank() }
                val avatarUri = selectedAvatarUri

                viewModel.updateProfile(
                    firstName, lastName, nickname,
                    gender, birthday, avatarUri, requireContext()
                )
            }
        }

        binding.btnEditAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
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
                    binding.edtNickname.setText(user.nickname)
                    binding.actvGender.setText(user.gender)
                    binding.edtBirthday.setText(user.dateOfBirth.toDisplayDate())

                    user.avatar?.takeIf { it.isNotBlank() }?.let { url ->
                        val secureUrl = url.replaceFirst("http://", "https://")
                        // dùng Glide / Coil / Picasso tuỳ thích
                        Glide.with(this)
                            .load(secureUrl)
                            .placeholder(R.drawable.avatar_default)
                            .error(R.drawable.avatar_default)
                            .into(binding.imgAvatar)
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
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedAvatarUri = null

                    binding.loadingProgressBar.visibility = View.GONE

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
        listOf(binding.edtHo, binding.edtTen, binding.edtNickname, binding.edtEmail, binding.edtBirthday).forEach { et ->
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

    fun String?.toDisplayDate(): String {
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

}