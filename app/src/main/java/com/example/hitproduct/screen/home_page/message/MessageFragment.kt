package com.example.hitproduct.screen.home_page.message

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.model.message.ChatItem
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentMessageBinding
import com.example.hitproduct.screen.adapter.MessageAdapter
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
        MessageViewModelFactory(authRepo)
    }

    private val viewModel2 by viewModels<HomeViewModel> {
        HomeViewModelFactory(authRepo)
    }

    private val roomId by lazy {
        prefs.getString(AuthPrefersConstants.ID_ROOM_CHAT, null)
            ?: throw IllegalStateException("Room ID not found")
    }
    private lateinit var adapter: MessageAdapter
    private val currentMessages = mutableListOf<ChatItem>()


    override fun initView() {
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
        // Ví dụ: xử lý nút gửi tin (nếu có)
        binding.btnSendMessage.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // TODO: viewModel.sendMessage(roomId, text)
                binding.etMessage.text?.clear()
            }
        }
    }

    override fun initData() {
        viewModel.fetchInitialMessages(roomId)
        viewModel2.getCoupleProfile()
    }

    override fun handleEvent() {

    }

    override fun bindData() {

        viewModel2.coupleProfile.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {}
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    //lay avatar va usernam cua  my love
                    val couple = state.data
                    if ((prefs.getString(
                            AuthPrefersConstants.MY_LOVE_ID,
                            null
                        ) ?: "") == couple.userA.id
                    ) {
                        binding.tvName.text = couple.userA.username
                        val avatar = couple.userA.avatar
                            ?.takeIf { it.isNotBlank() && it != "/example.png" }
                            ?.replaceFirst("http://", "https://")
                        if (avatar != null) {
                            binding.imgAvatar.loadImage(avatar)
                        } else {
                            binding.imgAvatar.loadImage(R.drawable.avatar_default)
                        }
                    } else {
                        binding.tvName.text = couple.userB.username
                        val avatar = couple.userB.avatar
                            ?.takeIf { it.isNotBlank() && it != "/example.png" }
                            ?.replaceFirst("http://", "https://")
                        if (avatar != null) {
                            binding.imgAvatar.loadImage(avatar)
                        } else {
                            binding.imgAvatar.loadImage(R.drawable.avatar_default)
                        }
                    }

                }
            }
        }


        // Lần đầu load
        // 1) Initial load
        viewModel.messagesState.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                currentMessages.clear()
                currentMessages.addAll(state.data)
                adapter.submitList(currentMessages)
                // scroll xuống cuối
                binding.rvMessage.scrollToPosition(currentMessages.size - 1)
            }
        }

        // Load thêm
        viewModel.loadMoreState.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                val fullList = state.data

                val newItems = fullList.filter { it !in currentMessages }
                if (newItems.isEmpty()) return@observe

                // 2.1 lưu vị trí và offset trước khi insert
                val lm = binding.rvMessage.layoutManager as LinearLayoutManager
                val firstPos = lm.findFirstVisibleItemPosition()
                val firstView = lm.findViewByPosition(firstPos)
                val offset = firstView?.top ?: 0

                // 2.2 cập nhật danh sách local và adapter
                currentMessages.addAll(0, newItems)
                adapter.prependMessages(newItems)

                // 2.3 restore scroll trong next UI loop
                binding.rvMessage.post {
                    lm.scrollToPositionWithOffset(firstPos + newItems.size, offset)
                }
            }
        }


    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchInitialMessages(roomId)
        }
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(inflater, container, false)
    }


}