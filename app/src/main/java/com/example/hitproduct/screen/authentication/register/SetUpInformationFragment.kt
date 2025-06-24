package com.example.hitproduct.screen.authentication.register

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.hitproduct.R
import com.example.hitproduct.databinding.FragmentSetUpInformationBinding


class SetUpInformationFragment : Fragment() {

    private lateinit var binding: FragmentSetUpInformationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetUpInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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


        //nut back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.tvContinue.setOnClickListener {
            val successCreateAccFragment = SuccessCreateAccFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentStart, successCreateAccFragment)

                .commit()
        }
    }
}