package com.example.hitproduct.screen.authentication.send_invite_code

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.ApiService
import com.example.hitproduct.data.api.RetrofitClient
import com.example.hitproduct.data.model.invite.InviteItem
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSendInviteCodeBinding
import com.example.hitproduct.screen.authentication.send_invite_code.adapter.InviteAdapter
import com.example.hitproduct.socket.SocketManager

class SendInviteCodeFragment : Fragment() {

    private var _binding: FragmentSendInviteCodeBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val authRepo by lazy {
        AuthRepository(
            RetrofitClient.getInstance().create(ApiService::class.java),
            prefs
        )
    }
    private val viewModel by viewModels<SendInviteCodeViewModel> {
        SendInviteCodeViewModelFactory(authRepo)
    }

    private fun setupInviteDialog() {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_invite, null)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(requireContext())

        inviteAdapter = InviteAdapter(
            onAccept = { itUser -> SocketManager.acceptFriendRequest(itUser.userId) },
            onReject = { itUser -> SocketManager.refuseFriendRequest(itUser.userId) },
            onCancel = { itUser -> SocketManager.cancelFriendRequest(itUser.userId) },
        )
        rv.adapter = inviteAdapter

        inviteDialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }


    private lateinit var inviteAdapter: InviteAdapter
    private val currentItems = mutableListOf<InviteItem>()
    private var inviteDialog: AlertDialog? = null

    private lateinit var token: String

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

        token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "").orEmpty()
        SocketManager.connect(token)

        // 1. Tạo dialog + adapter sẵn, nhưng chưa show
        setupInviteDialog()

        // 2. Fetch initial list từ API
        viewModel.checkInvite(token)
        viewModel.inviteResult.observe(viewLifecycleOwner) { result ->
            if (result is DataResult.Success) {
                currentItems.clear()
                // Received
                result.data.acceptFriends.forEach {
                    currentItems.add(InviteItem.Received(fromUser = it.username, userId = it.id))
                }
                // Sent
                result.data.requestFriends.forEach {
                    currentItems.add(InviteItem.Sent(toUser = it.username, userId = it.id))
                }
                // Cập nhật adapter
                inviteAdapter.submitList(currentItems.toList())
            }
        }

        viewModel.fetchUserProfile()
        viewModel.inviteCode.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Log.d("SendInvite", "Error: ${state.error}")
                }
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    // Hiển thị mã mời
                    val user = state.data
                    binding.tvInviteCode.text = user.coupleCode
                    Log.d("SendInvite", "User invite code: ${user.coupleCode}")
                }
            }
        }

        registerSocketListeners()

        setupInviteInput()
        setupBackButton()
        setupCopyInviteCode()
        setupNotificationButton()
        setupSendInviteButton()
    }

    private fun setupInviteInput() {
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
    }

    private fun setupBackButton() {
        binding.backIcon.setOnClickListener {
            Log.d("SendInvite", "Back button clicked")
            Log.d("SendInvite", "BackStack count: ${parentFragmentManager.backStackEntryCount}")

            if (parentFragmentManager.backStackEntryCount > 0) {
                Log.d("SendInvite", "Popping backstack")
                parentFragmentManager.popBackStack()
            } else {
                Log.d("SendInvite", "No backstack, finishing activity")
                requireActivity().finish()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCopyInviteCode() {
        binding.tvInviteCode.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.tvInviteCode.compoundDrawablesRelative[2]
                    ?: return@setOnTouchListener false
                if (event.x >= binding.tvInviteCode.width
                    - binding.tvInviteCode.paddingEnd
                    - drawableEnd.bounds.width()
                ) {
                    val code = binding.tvInviteCode.text.toString()
                    val cm =
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("invite_code", code))
                    Toast.makeText(requireContext(), "Copied: $code", Toast.LENGTH_SHORT).show()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun setupNotificationButton() {
        binding.btnNotification.setOnClickListener {
            // Mở dialog chỉ dựa trên dữ liệu socket
            showInviteDialog()
        }
    }

    private fun setupSendInviteButton() {
        binding.tvConnect.setOnClickListener {
            val code = binding.edtInviteCode.text.toString().trim()
            if (code.isEmpty()) return@setOnClickListener

            SocketManager.sendFriendRequest(code)
            Log.d("SendInvite", "Sent friend request with code=$code")
            // Mở dialog nếu chưa mở
//            if (inviteDialog == null || inviteDialog?.isShowing == false) {
//                showInviteDialog()
//            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        inviteDialog?.dismiss()
        SocketManager.disconnect()
    }

    private fun showInviteDialog() {
        if (inviteDialog == null) {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_invite, null)
            val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
            rv.layoutManager = LinearLayoutManager(requireContext())
            inviteAdapter = InviteAdapter(
                onAccept = { itUser -> SocketManager.acceptFriendRequest(itUser.userId) },
                onReject = { itUser -> SocketManager.refuseFriendRequest(itUser.userId) },
                onCancel = { itUser -> SocketManager.cancelFriendRequest(itUser.userId) },
            )
            rv.adapter = inviteAdapter
            inviteDialog = AlertDialog.Builder(requireContext())
                .setView(view)
                .create().apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
        }
        inviteAdapter.submitList(currentItems.toList())
        inviteDialog?.show()
        inviteDialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.83).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun registerSocketListeners() {
        SocketManager.onSuccess { message ->
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                binding.edtInviteCode.text?.clear()
            }
        }
        SocketManager.onError { message ->
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
        SocketManager.onRequestSent { data ->
            // Trên background thread của socket, nên post về main thread để cập nhật UI
            Handler(Looper.getMainLooper()).post {
                // Lấy đúng userId và username của người nhận
                val receiverId = data.optString("yourUserId")
                val receiverName = data.optString("yourUserName")

                // Thêm vào danh sách Sent
                currentItems.add(
                    InviteItem.Sent(
                        toUser = receiverName,
                        userId = receiverId
                    )
                )

                // Cập nhật dialog
                refreshDialog()
                if (inviteDialog?.isShowing != true) {
                    showInviteDialog()
                }
            }
        }

        // khi có invite đến → thêm vào Received
        SocketManager.onIncomingRequest { data ->
            Log.d("SendInvite", "Received incoming request: $data")
            Handler(Looper.getMainLooper()).post {
                val id = data.optString("yourUserId")
                val name = data.optString("yourUserName")
                currentItems.add(InviteItem.Received(fromUser = name, userId = id))
                refreshDialog()
                if (inviteDialog?.isShowing != true) showInviteDialog()
            }
        }

        // SERVER_RETURN_USER_CANCEL_REQUEST:
        // server confirm A huỷ lời mời đi (remove from Sent)
        SocketManager.onRequestCancelled { data ->
            Handler(Looper.getMainLooper()).post {
                val cancelledId = data.optString("yourUserId")
                currentItems.removeAll { it is InviteItem.Sent && it.userId == cancelledId }
                refreshDialog()
            }
        }

        // A huỷ với B → B remove Received
        SocketManager.onCancelReceived { data ->
            Handler(Looper.getMainLooper()).post {
                val cancelledId = data.optString("yourUserId")
                currentItems.removeAll { it is InviteItem.Received && it.userId == cancelledId }
                refreshDialog()
            }
        }

        // B từ chối A → remove Received
        SocketManager.onRequestRefused { data ->
            Handler(Looper.getMainLooper()).post {
                val refusedId = data.optString("yourUserId")
                currentItems.removeAll { it is InviteItem.Received && it.userId == refusedId }
                refreshDialog()
            }
        }

        // A nhận được B từ chối → remove Sent
        SocketManager.onRefusalReceived { data ->
            Handler(Looper.getMainLooper()).post {
                val refusedId = data.optString("yourUserId")
                currentItems.removeAll { it is InviteItem.Sent && it.userId == refusedId }
                refreshDialog()
            }
        }

        //A gui B, B chap nhan → remove Received, go HomeActivity
        SocketManager.onAccepted {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), "Friend request accepted", Toast.LENGTH_SHORT)
                    .show()
                // Xoá khỏi danh sách Received
                currentItems.removeAll { it is InviteItem.Received }
                refreshDialog()

                goHomeActivity()
            }
        }

        // B nhận được A chấp nhận → remove Sent, go HomeActivity
        SocketManager.onTheyAccept {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), "Friend request accepted", Toast.LENGTH_SHORT)
                    .show()
                // Xoá khỏi danh sách Sent
                currentItems.removeAll { it is InviteItem.Sent }
                refreshDialog()

                goHomeActivity()
            }
        }

    }

    private fun refreshDialog() {
        if (inviteDialog?.isShowing == true) {
            inviteAdapter.submitList(currentItems.toList())
        }
    }

    //vao homeActivity
    private fun goHomeActivity() {
        requireActivity().runOnUiThread {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                // Xóa stack cũ để không quay lại màn này được
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
