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
import com.example.hitproduct.screen.authentication.send_invite_code.adapter.InviteAdapter
import com.example.hitproduct.socket.SocketManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private lateinit var inviteAdapter: InviteAdapter

    private var showOnRefresh = false // flag để biết có cần show dialog hay không

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendInviteCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 1. Khi vào màn: chỉ checkInvite để lấy userId ---
        val token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "") ?: ""
        viewModel.checkInvite(token)
        viewModel.inviteResult.observe(viewLifecycleOwner) { result ->
            if (result is DataResult.Success) {
                // lưu userId thôi, không show dialog
                prefs.edit()
                    .putString(USER_ID, result.data.userId)
                    .apply()

                if (showOnRefresh) showInviteDialog(result.data)
            }
            // lỗi thì ignore (hoặc show toast tuỳ em)
        }

        // --- 2. Setup input + nút Connect ---
        val edtInviteCode = binding.edtInviteCode
        val btnConnect = binding.tvConnect
        fun updateButtonState() {
            val filled = edtInviteCode.text.toString().isNotBlank()
            btnConnect.isEnabled = filled
            btnConnect.setBackgroundResource(
                if (filled) R.drawable.bg_enable_btn
                else R.drawable.bg_disable_btn
            )
        }
        edtInviteCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        updateButtonState()

        // --- 3. Nút back ---
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // --- 4. Copy invite code ---
        binding.tvInviteCode.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.tvInviteCode.compoundDrawablesRelative[2] ?: return@setOnTouchListener false
                if (event.x >= binding.tvInviteCode.width
                    - binding.tvInviteCode.paddingEnd
                    - drawableEnd.bounds.width()
                ) {
                    v.performClick()
                    val code = binding.tvInviteCode.text.toString()
                    val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("invite_code", code))
                    Toast.makeText(requireContext(), "Copied: $code", Toast.LENGTH_SHORT).show()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // --- 5. Nút Xem thông báo: gọi API mới và show dialog ---
        binding.btnNotification.setOnClickListener {
            viewModel.checkInvite(token)   // chỉ cần gọi lại cái này, ViewModel sẽ chạy suspend trong coroutine
            showOnRefresh = true           // một flag nhỏ cho biết là lần này show dialog
        }

        // --- 6. Gửi lời mời qua socket ---
        binding.tvConnect.setOnClickListener {
            val code = edtInviteCode.text.toString().trim()
            val myId = prefs.getString(USER_ID, "") ?: ""

            Log.d("SendInviteCodeFragment", "myId: $myId, code: $code")
            SocketManager.sendFriendRequest(myId, code) { success, _ ->
                Log.d("SendInviteCodeFragment", "Socket response: success=$success")
                if (success) {
                    Toast.makeText(requireContext(), "Gửi thành công!", Toast.LENGTH_SHORT).show()
                    edtInviteCode.text?.clear()
                }
            }

        }



        // --- 7. Bắt lỗi socket ---
        SocketManager.onError { errMsg ->
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Lỗi: $errMsg", Toast.LENGTH_SHORT).show()
            }
        }

        SocketManager.onSuccess { errMsg ->
            Log.e("SendInviteCodeFragment", "Socket error: $errMsg")
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), " $errMsg", Toast.LENGTH_SHORT).show()
            }
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

        val items = mutableListOf<InviteItem>().apply {
            addAll(inviteData.acceptFriends.map {
                InviteItem.Received(fromUser = it.username, userId = it.id)
            })
            addAll(inviteData.requestFriends.map {
                InviteItem.Sent(toUser = it.username, userId = it.id)
            })
        }

        val myId = prefs.getString(USER_ID, "") ?: ""
        inviteAdapter = InviteAdapter(
            onAccept = { received ->
                SocketManager.acceptFriendRequest(myId, received.userId)
                items.remove(received); inviteAdapter.submitList(items.toList())
            },
            onReject = { received ->
                SocketManager.refuseFriendRequest(myId, received.userId)
                items.remove(received); inviteAdapter.submitList(items.toList())
            },
            onCancel = { sent ->
                SocketManager.cancelFriendRequest(myId, sent.userId)
                items.remove(sent); inviteAdapter.submitList(items.toList())
            }
        )
        recyclerView.adapter = inviteAdapter
        inviteAdapter.submitList(items.toList())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.83).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
