package com.example.hitproduct.screen.home_page.message

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.FcmClient
import com.example.hitproduct.common.util.NotificationConfig
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.model.message.ChatItem
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentMessageBinding
import com.example.hitproduct.screen.adapter.MessageAdapter
import com.example.hitproduct.screen.dialog.emoji.DialogEmoji
import com.example.hitproduct.screen.home_page.home.HomeViewModel
import com.example.hitproduct.screen.home_page.home.HomeViewModelFactory
import io.getstream.avatarview.glide.loadImage

class MessageFragment : BaseFragment<FragmentMessageBinding>() {

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by viewModels<MessageViewModel> {
        MessageViewModelFactory(requireActivity().application, authRepo)
    }

    private val viewModel2 by viewModels<HomeViewModel> {
        HomeViewModelFactory(authRepo)
    }

    private val roomId: String by lazy {
        prefs.getString(AuthPrefersConstants.ID_ROOM_CHAT, null)
            ?: throw IllegalStateException("RoomChatId chưa được lưu trong SharedPreferences")
    }

    private lateinit var adapter: MessageAdapter
    private val currentMessages = mutableListOf<ChatItem>()

    private var selectedImageUri: Uri? = null

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMessageBinding = FragmentMessageBinding.inflate(inflater, container, false)

    // Android 13+
    private val pick13Plus = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { showPreview(it) }
    }

    // Android <= 12
    private val openDoc = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            requireActivity().contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            showPreview(it)
        }
    }

    private fun showPreview(uri: Uri) {
        selectedImageUri = uri
        binding.previewContainer.visibility = View.VISIBLE
        binding.previewImage.loadImage(uri)
    }

    private fun clearPreview() {
        selectedImageUri = null
        binding.previewContainer.visibility = View.GONE
        binding.previewImage.setImageDrawable(null)
    }

    override fun initView() {
        viewModel.sendRoomChatId(roomId)

        adapter = MessageAdapter()
        binding.rvMessage.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MessageFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    if (!rv.canScrollVertically(-1)) {
                        viewModel.loadMore(roomId)
                    }
                }
            })
        }
    }

    override fun initListener() {
        binding.btnSendImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33) {
                pick13Plus.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                openDoc.launch(arrayOf("image/*"))
            }
        }

        binding.btnRemovePreview.setOnClickListener { clearPreview() }

        // Gửi chung: text + (optional) ảnh -> 1 socket
        binding.btnSendMessage.setOnClickListener {
            val text = binding.etMessage.text?.toString()?.trim().orEmpty()
            val imgUri = selectedImageUri

            if (text.isEmpty() && imgUri == null) return@setOnClickListener

            binding.btnSendMessage.isEnabled = false

            // Nếu có ảnh: encode Base64 data URI rồi gửi kèm images
            if (imgUri != null) {
                viewModel.encodeImageToDataUri(
                    ctx = requireContext(),
                    uri = imgUri,
                    onDone = { dataUri ->
                        viewModel.sendMessage("", images = listOf(dataUri))
                        Handler(Looper.getMainLooper()).postDelayed({
                            viewModel.sendMessage(text)
                            sendFcmIfText(text)
                        }, 100)

                        binding.etMessage.text?.clear()
                        clearPreview()
                    },
                    onError = { err ->
                        Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                        binding.btnSendMessage.isEnabled = true
                    }
                )
            } else {
                // Chỉ text
                viewModel.sendMessage(text)
                binding.etMessage.text?.clear()
                sendFcmIfText(text)
            }
        }

        binding.etMessage.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) viewModel.sendIsTyping()
        }

        binding.tilMessage.setEndIconOnClickListener {
            DialogEmoji { emoji ->
                val et = binding.etMessage
                val pos = et.selectionStart.coerceAtLeast(0)
                et.text?.insert(pos, emoji)
                et.setSelection(pos + emoji.length)
            }.show(childFragmentManager, "emoji_picker")
        }
    }

    private fun sendFcmIfText(text: String) {
        if (text.isNotEmpty()) {
            val myLoveId = authRepo.getMyLoveId()
            val payload = mapOf("type" to "chat_message")
            val tpl = NotificationConfig.getTemplate("chat_message", payload)
            FcmClient.sendToTopic(
                receiverUserId = myLoveId,
                title = tpl.title,
                body = tpl.body,
                data = payload
            )
        }
    }

    override fun initData() {
        viewModel.fetchInitialMessages(roomId)
        viewModel2.getCoupleProfile()
    }

    override fun handleEvent() {}

    override fun bindData() {
        // Header tên + avatar
        viewModel2.coupleProfile.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                val couple = state.data
                val myLoveId = prefs.getString(AuthPrefersConstants.MY_LOVE_ID, null) ?: ""
                val target = if (myLoveId == couple.userA.id) couple.userA else couple.userB

                binding.tvName.text = target.nickname?.takeIf { it.isNotBlank() }
                    ?: target.firstName?.takeIf { it.isNotBlank() }
                            ?: target.username

                val avatar = target.avatar
                    ?.takeIf { it.isNotBlank() && it != "/example.png" }
                    ?.replaceFirst("http://", "https://")

                if (avatar != null) binding.imgAvatar.loadImage(avatar)
                else binding.imgAvatar.loadImage(R.drawable.avatar_default)
            }
        }

        // Lần đầu
        viewModel.messagesState.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                currentMessages.clear()
                currentMessages.addAll(state.data)
                adapter.submitList(currentMessages.toList())
                if (currentMessages.isNotEmpty()) {
                    binding.rvMessage.scrollToPosition(currentMessages.size - 1)
                }
            }
        }

        // Load thêm
        viewModel.loadMoreState.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                val full = state.data
                val newItems = full.filter { it !in currentMessages }
                if (newItems.isEmpty()) return@observe

                val lm = binding.rvMessage.layoutManager as LinearLayoutManager
                val firstPos = lm.findFirstVisibleItemPosition()
                val firstView = lm.findViewByPosition(firstPos)
                val offset = firstView?.top ?: 0

                currentMessages.addAll(0, newItems)
                adapter.prependMessages(newItems)

                binding.rvMessage.post {
                    lm.scrollToPositionWithOffset(firstPos + newItems.size, offset)
                }
            }
        }

        // Trạng thái gửi
        viewModel.sendState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.error.toString(), Toast.LENGTH_SHORT)
                        .show()
                    binding.btnSendMessage.isEnabled = true
                }

                UiState.Idle -> binding.btnSendMessage.isEnabled = true
                UiState.Loading -> binding.btnSendMessage.isEnabled = false
                is UiState.Success -> binding.btnSendMessage.isEnabled = true
                null -> Unit
            }
        }

        // Typing
        viewModel.typingState.observe(viewLifecycleOwner) { isTyping ->
            adapter.showTyping(isTyping)
            if (isTyping) {
                Log.d("MessageFragment", "User is typing...")
                binding.rvMessage.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.sendRoomChatId(roomId)
            viewModel.fetchInitialMessages(roomId)
        }
    }
}
