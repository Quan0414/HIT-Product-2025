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
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.constants.AuthPrefersConstants.USER_ID
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.model.invite.InviteData
import com.example.hitproduct.data.model.invite.InviteItem
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSendInviteCodeBinding
import com.example.hitproduct.screen.adapter.InviteAdapter
import com.example.hitproduct.socket.SocketManager

class SendInviteCodeFragment : Fragment() {

    private var _binding: FragmentSendInviteCodeBinding? = null

    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(RetrofitClient.getInstance().create(ApiService::class.java), prefs)
    }

    private val viewModel by lazy {
        SendInviteCodeViewModel(authRepo)
    }

    lateinit var inviteAdapter: InviteAdapter

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
            val token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "") ?: ""
            viewModel.checkInvite(token)
        }


        viewModel.inviteResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is DataResult.Success -> {
                    val inviteData = result.data

                    // 1. Lấy userId từ response và lưu vào SharedPreferences
                    val currentUserId = inviteData.userId
                    prefs.edit()
                        .putString(USER_ID, currentUserId)
                        .apply()

                    // 2. Hiển thị dialog
                    showInviteDialog(inviteData)
                }

                is DataResult.Error ->
                    Toast.makeText(requireContext(), "Error fetch data", Toast.LENGTH_SHORT).show()
            }
        }


        //nut connect
        binding.tvConnect.setOnClickListener {
            val code = binding.edtInviteCode.text.toString().trim()
            val myId = prefs.getString(USER_ID, "") ?: ""

            Log.d("SendInviteCode", "Click - Code: $code, UserID: $myId")

            SocketManager.sendFriendRequest(myId, code) { success, _ ->
                Log.d("SendInviteCode", "Socket ACK - Success: $success")
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Gửi thành công!", Toast.LENGTH_SHORT
                    ).show()
                    binding.edtInviteCode.text?.clear()
                }
                // Không toast ở đây khi success = false
                // vì lỗi đã được bắt và toast trong onError()
            }
        }


        SocketManager.onError { errMsg ->
            Toast.makeText(requireContext(), "Lỗi: $errMsg", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showInviteDialog(inviteData: InviteData) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_invite, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 1. Dùng MutableList để có thể xóa item
        val items = mutableListOf<InviteItem>().apply {
            addAll(inviteData.acceptFriends.map {
                InviteItem.Received(fromUser = it.username, userId = it.id)
            })
            addAll(inviteData.requestFriends.map {
                InviteItem.Sent(toUser = it.username, userId = it.id)
            })
        }

        // 2. Lấy myUserId
        val myId = prefs.getString(USER_ID, "") ?: ""

        // 3. Tạo adapter, xử lý onCancel ngay trong callback
        inviteAdapter = InviteAdapter(
            onAccept = { received ->
                SocketManager.acceptFriendRequest(myId, received.userId)
                // (nếu muốn) xóa luôn item này:
                items.remove(received)
                inviteAdapter.submitList(items.toList())
            },
            onReject = { received ->
                SocketManager.refuseFriendRequest(myId, received.userId)
                items.remove(received)
                inviteAdapter.submitList(items.toList())
            },
            onCancel = { sent ->
                SocketManager.cancelFriendRequest(myId, sent.userId)
                // Xóa item khỏi list và cập nhật adapter ngay
                items.remove(sent)
                inviteAdapter.submitList(items.toList())
            }
        )

        // 4. Gán adapter và ban đầu load data
        recyclerView.adapter = inviteAdapter
        inviteAdapter.submitList(items.toList())

        // 5. Tạo và show dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.83).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}