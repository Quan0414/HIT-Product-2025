package com.example.hitproduct.screen.authentication.send_invite_code

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hitproduct.R
import com.example.hitproduct.databinding.FragmentSendInviteCodeBinding

class SendInviteCodeFragment : Fragment() {

    private var _binding: FragmentSendInviteCodeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSendInviteCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lấy đối tượng EditText và Button
        val edtInviteCode = binding.edtInviteCode
        val btnConnect = binding.tvConnect

        // Hàm cập nhật trạng thái nút
        fun updateButtonState() {
            val filled = edtInviteCode.text.toString().isNotBlank()  // Kiểm tra nếu có text
            btnConnect.isEnabled = filled

            if (filled) {
                btnConnect.setBackgroundResource(R.drawable.bg_enable_btn)
            } else {
                btnConnect.setBackgroundResource(R.drawable.bg_disable_btn)
            }

        }

        // Lắng nghe thay đổi text
        edtInviteCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateButtonState()
            }
        })

        updateButtonState()


        //nut back
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}