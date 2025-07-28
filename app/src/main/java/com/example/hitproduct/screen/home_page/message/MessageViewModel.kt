package com.example.hitproduct.screen.home_page.message

import androidx.lifecycle.*
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.data.model.message.ChatItem
import com.example.hitproduct.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MessageViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
    }

    // state cho lần fetch đầu
    private val _messagesState = MutableLiveData<UiState<List<ChatItem>>>()
    val messagesState: LiveData<UiState<List<ChatItem>>> = _messagesState

    // state cho tải thêm
    private val _loadMoreState = MutableLiveData<UiState<List<ChatItem>>>()
    val loadMoreState: LiveData<UiState<List<ChatItem>>> = _loadMoreState

    // Giữ trường sentAt cũ nhất để lazy–load
    private var oldestSentAt: String? = null
    private var hasMore = true

    /** 1) Initial load */
    fun fetchInitialMessages(roomId: String) {
        // bắt đầu Loading
        _messagesState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getMessages(roomId, before = null)) {
                is DataResult.Success -> {
                    val list = result.data
                    // lưu sentAt và hasMore
                    oldestSentAt = list.firstOrNull()?.sentAt
                    hasMore = list.size >= PAGE_SIZE

                    _messagesState.value = UiState.Success(list)
                }

                is DataResult.Error -> {
                    _messagesState.value = UiState.Error(result.error)
                }
            }
        }
    }

    /** 2) Lazy–load thêm */
    fun loadMore(roomId: String) {
        // nếu không còn dữ liệu hoặc đang load, bỏ qua
        if (!hasMore || oldestSentAt == null) return

        _loadMoreState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getMessages(roomId, before = oldestSentAt)) {
                is DataResult.Success -> {
                    val newItems = result.data
                    val current = (_messagesState.value as? UiState.Success)?.data.orEmpty()
                    // nếu có dữ liệu mới
                    if (newItems.isNotEmpty()) {
                        // ghép vào đầu danh sách cũ
                        val updated = newItems + current
                        oldestSentAt = newItems.first().sentAt
                        hasMore = newItems.size >= PAGE_SIZE

                        _loadMoreState.value = UiState.Success(updated)
                        // cũng cập nhật luôn messagesState để UI có thể scrollToPosition
                        _messagesState.value = UiState.Success(updated)
                    } else {
                        hasMore = false
                        // nothing to load
                        _loadMoreState.value = UiState.Success(current)
                    }
                }

                is DataResult.Error -> {
                    _loadMoreState.value = UiState.Error(result.error)
                }
            }
        }
    }
}
