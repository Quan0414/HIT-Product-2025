package com.example.hitproduct.screen.home_page.message

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.MappedError
import com.example.hitproduct.data.model.message.ChatItem
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.socket.SocketManager
import kotlinx.coroutines.launch

class MessageViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
    }

    private val _messagesState = MutableLiveData<UiState<List<ChatItem>>>()
    val messagesState: LiveData<UiState<List<ChatItem>>> = _messagesState

    private val _loadMoreState = MutableLiveData<UiState<List<ChatItem>>>()
    val loadMoreState: LiveData<UiState<List<ChatItem>>> = _loadMoreState

    private val _sendState = MutableLiveData<UiState<Unit>>()
    val sendState: LiveData<UiState<Unit>> = _sendState

    private val _typingState = MutableLiveData<Boolean>()
    val typingState: LiveData<Boolean> = _typingState


    // Giữ trường sentAt cũ nhất để lazy–load
    private var oldestSentAt: String? = null
    private var hasMore = true

    // Flag để theo dõi xem user đã tương tác chưa
    private var hasUserInteracted = false

    init {
        setupSocketListeners()
    }


    private fun setupSocketListeners() {
        SocketManager.onMessageReceived { item ->
            val current = (_messagesState.value as? UiState.Success)?.data.orEmpty()
            _messagesState.postValue(UiState.Success(current + item))
        }

        SocketManager.onError { msg ->
            // Chỉ post error nếu user đã tương tác (đang gửi tin nhắn)
            if (hasUserInteracted && _sendState.value is UiState.Loading) {
                _sendState.postValue(UiState.Error(MappedError(msg)))
                hasUserInteracted = false // Reset flag
            }
        }

        SocketManager.onTypingReceived { senderId ->
            val myLoveId = authRepository.getMyLoveId()
            _typingState.postValue(senderId == myLoveId)
            Handler(Looper.getMainLooper()).postDelayed({
                _typingState.postValue(false)
            }, 3000)
        }
    }

    fun fetchInitialMessages(roomId: String) {
        _messagesState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getMessages(roomId, before = null)) {
                is DataResult.Success -> {
                    val list = result.data
                    // Lưu sentAt và hasMore
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

    fun loadMore(roomId: String) {
        // Nếu không còn dữ liệu hoặc đang load, bỏ qua
        if (!hasMore || oldestSentAt == null) return

        _loadMoreState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getMessages(roomId, before = oldestSentAt)) {
                is DataResult.Success -> {
                    val newItems = result.data
                    val current = (_messagesState.value as? UiState.Success)?.data.orEmpty()

                    if (newItems.isNotEmpty()) {
                        // Ghép vào đầu danh sách cũ
                        val updated = newItems + current
                        oldestSentAt = newItems.first().sentAt
                        hasMore = newItems.size >= PAGE_SIZE

                        _loadMoreState.value = UiState.Success(updated)
                        // Cũng cập nhật luôn messagesState để UI có thể scrollToPosition
                        _messagesState.value = UiState.Success(updated)
                    } else {
                        hasMore = false
                        _loadMoreState.value = UiState.Success(current)
                    }
                }

                is DataResult.Error -> {
                    _loadMoreState.value = UiState.Error(result.error)
                }
            }
        }
    }

    /**
     * Gửi tin nhắn
     */
    fun joinRoom(roomId: String) {
        SocketManager.joinRoom(roomId)
    }


    fun sendMessage(content: String, images: List<String> = emptyList()) {
        val text = content.trim()
        if (text.isEmpty() && images.isEmpty()) return

        // Đánh dấu user đã tương tác
        hasUserInteracted = true

        _sendState.value = UiState.Loading

        try {
            SocketManager.sendMessage(text, images)

            hasUserInteracted = false
            _sendState.value = UiState.Success(Unit)

        } catch (e: Exception) {
            hasUserInteracted = false
            _sendState.value = UiState.Error(MappedError(e.message ?: "Failed to send message"))
        }
    }

    /**
     * Reset send state về trạng thái ban đầu
     */
    fun resetSendState() {
        hasUserInteracted = false
        _sendState.value = UiState.Idle
    }

    /**
     * Kiểm tra xem có đang trong quá trình gửi tin nhắn không
     */
    fun isSending(): Boolean {
        return _sendState.value is UiState.Loading
    }

    override fun onCleared() {
        super.onCleared()
        hasUserInteracted = false
    }
}