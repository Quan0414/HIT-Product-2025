package com.example.hitproduct.screen.authentication.register.set_up_infor

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSetUpInformationBinding
import com.example.hitproduct.screen.authentication.login.LoginViewModel
import com.example.hitproduct.screen.authentication.login.LoginViewModelFactory
import com.example.hitproduct.screen.authentication.register.success.SuccessCreateAccFragment
import java.text.SimpleDateFormat
import java.util.Locale


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //observer viewModel
        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    val err = state.error
                    Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
                }

                UiState.Idle -> {

                }

                UiState.Loading -> {

                }

                is UiState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Chuyển đến SuccessCreateAccFragment
                    val successCreateAccFragment = SuccessCreateAccFragment()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentStart, successCreateAccFragment)
                        .commit()
                }
            }
        }

        binding.tvContinue.setOnClickListener {
            val rawDob = binding.edtBirthday.text.toString().takeIf { it.isNotBlank() }
            val formattedDob = rawDob?.let {
                try {
                    val inputFmt  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    outputFmt.format(inputFmt.parse(it)!!)
                } catch (e: Exception) {
                    null
                }
            }

            val firstName   = binding.edtHo.text.toString().takeIf { it.isNotBlank() }
            val lastName    = binding.edtTen.text.toString().takeIf { it.isNotBlank() }
            val nickName    = binding.edtNickname.text.toString().takeIf { it.isNotBlank() }
            val gender      = binding.actvGender.text.toString().takeIf { it.isNotBlank() }
            // Chú ý: ở đây phải dùng formattedDob, không phải rawDob hay editText.text
            val dateOfBirth = formattedDob

            val avatarUri   = null

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
        val editText = binding.edtBirthday

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
                editText.setText(sb)
                editText.setSelection(sb.length)
            }
        })


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
}