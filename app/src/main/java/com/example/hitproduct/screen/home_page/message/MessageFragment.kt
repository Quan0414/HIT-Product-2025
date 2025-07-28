package com.example.hitproduct.screen.home_page.message

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentMessageBinding
import com.example.hitproduct.screen.adapter.MessageAdapter
import java.time.YearMonth


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

    private val viewModel by activityViewModels<MessageViewModel> {
        MessageViewModelFactory(authRepo)
    }

    private val roomId by lazy {
        prefs.getString(AuthPrefersConstants.ID_ROOM_CHAT, null)
            ?: throw IllegalStateException("Room ID not found")
    }
    private lateinit var adapter: MessageAdapter

    override fun initView() {
        adapter = MessageAdapter()
        binding.rvMessage.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@MessageFragment.adapter

            // Scroll listener để lazy‐load khi kéo lên đầu
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
    }

    override fun handleEvent() {

    }

    override fun bindData() {
        // Lần đầu load
        viewModel.messagesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {}
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    adapter.submitList(state.data)
                    binding.rvMessage.scrollToPosition(state.data.size - 1)
                }
            }
        }

        // Load thêm
        viewModel.loadMoreState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {}
                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    adapter.submitList(state.data)
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