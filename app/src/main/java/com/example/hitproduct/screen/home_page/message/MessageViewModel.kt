package com.example.hitproduct.screen.home_page.message

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hitproduct.base.DataResult
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.CryptoHelper
import com.example.hitproduct.common.util.MappedError
import com.example.hitproduct.data.model.message.ChatItem
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.socket.SocketManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKey

class MessageViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val sharedKey: SecretKey? =
        CryptoHelper.getSharedAesKey(getApplication())

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
        SocketManager.onMessageReceived { data ->
            val senderId = data.optString("senderId", "")
            val encoded = data.optString("content", "")
            val imagesArr = data.optJSONArray("images")
            val imageUrl = imagesArr?.takeIf { it.length() > 0 }?.getString(0)
            Log.d("MessageViewModel", "Nhận tin nhắn: $encoded")

            val text = if (encoded.isNotBlank()) {
                val cipher = Base64.decode(encoded, Base64.NO_WRAP)
                val key = sharedKey
                    ?: throw IllegalStateException("Shared key chưa derive!")
                val plain = CryptoHelper.decrypt(key, cipher)
                String(plain, Charsets.UTF_8)
            } else {
                ""
            }

            val myUserId = authRepository.getMyUserId()
            val fromMe = senderId == myUserId
            val id = data.optString("messageId", UUID.randomUUID().toString())
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
            val sentAt = SimpleDateFormat("HH:mm", Locale("vi", "VN"))
                .format(Date(timestamp))

            val item = if (!imageUrl.isNullOrBlank()) {
                ChatItem.ImageMessage(
                    id = id,
                    senderId = senderId,
                    avatarUrl = null,
                    imageUrl = imageUrl,
                    sentAt = sentAt,
                    fromMe = fromMe
                )
            } else {
                ChatItem.TextMessage(
                    id = id,
                    senderId = senderId,
                    avatarUrl = null,
                    text = text,
                    sentAt = sentAt,
                    fromMe = fromMe
                )
            }

            val current = (_messagesState.value as? UiState.Success)?.data.orEmpty()
            _messagesState.postValue(UiState.Success(current + item))
        }

        SocketManager.onError { msg ->
            if (hasUserInteracted && _sendState.value is UiState.Loading) {
                _sendState.postValue(UiState.Error(MappedError(msg)))
                hasUserInteracted = false
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
                    val decrypted = decryptItems(result.data)
                    oldestSentAt = decrypted.firstOrNull()?.sentAt
                    hasMore = decrypted.size >= PAGE_SIZE
                    _messagesState.value = UiState.Success(decrypted)
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
                    // 1. Decrypt list mới
                    val decryptedNew = decryptItems(result.data)
                    // 2. Ghép vào đầu, cập nhật sentAt & hasMore
                    val current = (_messagesState.value as? UiState.Success)?.data.orEmpty()
                    if (decryptedNew.isNotEmpty()) {
                        val updated = decryptedNew + current
                        oldestSentAt = decryptedNew.first().sentAt
                        hasMore = decryptedNew.size >= PAGE_SIZE

                        _loadMoreState.value = UiState.Success(updated)
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
    fun sendRoomChatId(roomId: String) {
        SocketManager.sendRoomChatId(roomId)
    }

    fun sendMessage(content: String, images: List<String> = emptyList()) {
        val text = content.trim()
        if (text.isEmpty() && images.isEmpty()) return

        // Đánh dấu user đã tương tác
        hasUserInteracted = true
        _sendState.value = UiState.Loading

        try {
            val key = sharedKey
                ?: throw IllegalStateException("Shared key chưa derive!")
            val cipher = CryptoHelper.encrypt(key, text.toByteArray(Charsets.UTF_8))
            val encoded = Base64.encodeToString(cipher, Base64.NO_WRAP)
            SocketManager.sendMessage(encoded, images)
            Log.d("MessageViewModel", "Gửi tin nhắn: $encoded, images: $images")
            _sendState.value = UiState.Success(Unit)
            resetSendState()
        } catch (e: Exception) {
            _sendState.value = UiState.Error(MappedError(e.message ?: "Gửi thất bại"))
        }
    }

    fun sendIsTyping() {
        SocketManager.sendTyping()
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

    private fun decryptItems(raw: List<ChatItem>): List<ChatItem> {
        val key = sharedKey
            ?: throw IllegalStateException("Shared key chưa derive!")
        return raw.map { item ->
            when (item) {
                is ChatItem.TextMessage -> {
                    // item.text đang là Base64-encoded cipher
                    val cipher = Base64.decode(item.text, Base64.NO_WRAP)
                    val plain = CryptoHelper.decrypt(key, cipher)
                    item.copy(text = String(plain, Charsets.UTF_8))
                }

                else -> item
            }
        }
    }

}