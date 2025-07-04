package com.example.hitproduct.screen.authentication.send_invite_code

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.data.model.invite.InviteItem
import com.example.hitproduct.databinding.FragmentSendInviteCodeBinding
import com.example.hitproduct.screen.adapter.InviteAdapter

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

    @SuppressLint("ClickableViewAccessibility")
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

        //copy
        binding.tvInviteCode.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Lấy drawableEnd (index 2)
                val drawableEnd = binding.tvInviteCode.compoundDrawablesRelative[2]
                    ?: return@setOnTouchListener false

                // Kiểm tra xem tap có nằm trong bounds của icon hay không
                if (event.x >= binding.tvInviteCode.width
                    - binding.tvInviteCode.paddingEnd
                    - drawableEnd.bounds.width()
                ) {
                    v.performClick()
                    // Copy text
                    val code = binding.tvInviteCode.text.toString()
                    val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                            as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("invite_code", code))
                    Toast.makeText(requireContext(), "Copied: $code", Toast.LENGTH_SHORT).show()
                    return@setOnTouchListener true
                }
            }

            false
        }

        //thong bao
        binding.btnNotification.setOnClickListener {
            showInviteDialog()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showInviteDialog() {
        // 1. Inflate layout XML của dialog
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_invite, null)

        // 2. Thiết lập RecyclerView
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 3. Tạo adapter
        val inviteAdapter = InviteAdapter(
            onAccept = { item ->
                // gọi API accept
                Log.d("InviteDebug", "Accept clicked for: $item")
            },
            onReject = { item ->
                // gọi API reject
                Log.d("InviteDebug", "Reject clicked for: $item")
            }
        )

        // 4. SET ADAPTER TRƯỚC KHI SUBMIT LIST
        recyclerView.adapter = inviteAdapter

        // 5. Fake data list
        val fakeList = listOf(
            InviteItem.Received(fromUser = "Alice"),
            InviteItem.Received(fromUser = "Bob"),
            InviteItem.Sent(toUser = "Charlie")
        )

        // 6. SAU ĐÓ MỚI SUBMIT LIST
        inviteAdapter.submitList(fakeList)

        // 7. Show dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }



}