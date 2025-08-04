package com.example.hitproduct.screen.authentication.send_invite_code

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.MainActivity
import com.example.hitproduct.R
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.CryptoHelper
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.model.invite.InviteItem
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentSendInviteCodeBinding
import com.example.hitproduct.screen.adapter.InviteAdapter
import com.example.hitproduct.screen.authentication.login.LoginViewModel
import com.example.hitproduct.screen.authentication.login.LoginViewModelFactory
import com.example.hitproduct.socket.SocketManager

class SendInviteCodeFragment : Fragment() {

    private var _binding: FragmentSendInviteCodeBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }
    private val viewModel by viewModels<SendInviteCodeViewModel> {
        SendInviteCodeViewModelFactory(authRepo)
    }

    private val viewModel2 by viewModels<LoginViewModel> {
        LoginViewModelFactory(authRepo)
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
        viewModel.checkInvite()
        viewModel2.checkProfile()
        registerSocketListeners()


        viewModel.inviteResult.observe(viewLifecycleOwner) { result ->
            if (result is DataResult.Success) {
                val hasUnread = result.data.acceptFriends.isNotEmpty()
                // Cập nhật icon
                val iconRes = if (hasUnread)
                    R.drawable.ic_have_request
                else
                    R.drawable.ic_no_request
                binding.btnNotification.setImageResource(iconRes)

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

        viewModel2.profileState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UiState.Error -> {}
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    val myUserId = result.data.id
                    prefs.edit()
                        .putString(AuthPrefersConstants.MY_USER_ID, myUserId)
                        .apply()

                    // Cập nhật mã mời
                    val coupleCode = result.data.coupleCode
                    binding.tvInviteCode.text = coupleCode
                }
            }
        }

        viewModel2.coupleProfile.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {}
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    Log.d("SendInvite", "Couple profile loaded: ${state.data}")
                    val myLovePubKey = state.data.myLovePubKey
                    Log.d("SendInvite", "My love public key: $myLovePubKey")
                    if (myLovePubKey != null) {
                        CryptoHelper.storePeerPublicKey(requireContext(), myLovePubKey)
                        CryptoHelper.deriveAndStoreSharedAesKey(requireContext())
                        val key = CryptoHelper.getSharedAesKey(requireContext())
                        Log.d("SendInvite", "Shared AES key: $key")
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        )

        setupInviteInput()
//        setupBackButton()
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

//    private fun setupBackButton() {
//        binding.backIcon.setOnClickListener {
//            Log.d("SendInvite", "Back button clicked")
//            Log.d("SendInvite", "BackStack count: ${parentFragmentManager.backStackEntryCount}")
//
//            if (parentFragmentManager.backStackEntryCount > 0) {
//                Log.d("SendInvite", "Popping backstack")
//                parentFragmentManager.popBackStack()
//            } else {
//                Log.d("SendInvite", "No backstack, finishing activity")
//                requireActivity().finish()
//            }
//        }
//    }

//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupCopyInviteCode() {
//        binding.tvInviteCode.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                val drawableEnd = binding.tvInviteCode.compoundDrawablesRelative[2]
//                    ?: return@setOnTouchListener false
//                if (event.x >= binding.tvInviteCode.width
//                    - binding.tvInviteCode.paddingEnd
//                    - drawableEnd.bounds.width()
//                ) {
//                    val code = binding.tvInviteCode.text.toString()
//                    val cm =
//                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                    cm.setPrimaryClip(ClipData.newPlainText("invite_code", code))
//                    Toast.makeText(requireContext(), "Copied: $code", Toast.LENGTH_SHORT).show()
//                    return@setOnTouchListener true
//                }
//            }
//            false
//        }
//    }

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
                    val textToShare = binding.tvInviteCode.text.toString().trim()

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, textToShare)
                    }

                    startActivity(Intent.createChooser(shareIntent, null))
                }
            }
            false
        }
    }

    private fun setupNotificationButton() {
        binding.btnNotification.setOnClickListener {
            // Mở dialog chỉ dựa trên dữ liệu socket
            showInviteDialog()
            binding.btnNotification.setImageResource(R.drawable.ic_no_request)
        }
    }

    private fun setupSendInviteButton() {
        binding.tvConnect.setOnClickListener {
            val code = binding.edtInviteCode.text.toString().trim()
            if (code.isEmpty()) return@setOnClickListener

            SocketManager.sendFriendRequest(code)
            Log.d("SendInvite", "Sent friend request with code=$code")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        inviteDialog?.dismiss()
//        SocketManager.disconnect()
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
//                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                Log.e("SendInvite", "Socket error: $message")
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

        //A gui B, B chap nhan → remove Received
        SocketManager.onAccepted {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    requireContext(),
                    "Bạn và người ấy đã trở thành một đôi!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                // Xoá khỏi danh sách Received
                currentItems.removeAll { it is InviteItem.Received }
                refreshDialog()
            }
        }

        // B nhận được A chấp nhận → remove Sent
        SocketManager.onTheyAccept {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    requireContext(),
                    "Bạn và người ấy đã trở thành một đôi!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                // Xoá khỏi danh sách Sent
                currentItems.removeAll { it is InviteItem.Sent }
                refreshDialog()
            }
        }

        SocketManager.onListenCouple { data ->
            Log.d("SendInvite", "Received couple data: $data")
            Handler(Looper.getMainLooper()).post {
                val roomId = data.optString("roomChatId")
                val myUserId = data.optString("myUserId")
                val myLoveId = data.optString("myLoveId")
                val coupleId = data.optString("coupleId")
                prefs.edit()
                    .putString(AuthPrefersConstants.ID_ROOM_CHAT, roomId)
                    .putString(AuthPrefersConstants.MY_USER_ID, myUserId)
                    .putString(AuthPrefersConstants.MY_LOVE_ID, myLoveId)
                    .putString(AuthPrefersConstants.COUPLE_ID, coupleId)
                    .apply()

                viewModel2.getCoupleProfile()
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
