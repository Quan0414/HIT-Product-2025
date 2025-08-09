package com.example.hitproduct.screen.home_page.message

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.exifinterface.media.ExifInterface
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKey
import kotlin.math.min
import kotlin.math.roundToInt

class MessageViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>()
    private val sharedKey: SecretKey?
        get() = CryptoHelper.getSharedAesKey(context)

    companion object { private const val PAGE_SIZE = 20 }

    private val _messagesState = MutableLiveData<UiState<List<ChatItem>>>()
    val messagesState: LiveData<UiState<List<ChatItem>>> = _messagesState

    private val _loadMoreState = MutableLiveData<UiState<List<ChatItem>>>()
    val loadMoreState: LiveData<UiState<List<ChatItem>>> = _loadMoreState

    private val _sendState = MutableLiveData<UiState<Unit>>()
    val sendState: LiveData<UiState<Unit>> = _sendState

    private val _typingState = MutableLiveData<Boolean>()
    val typingState: LiveData<Boolean> = _typingState

    private var oldestSentAt: String? = null
    private var hasMore = true
    private var hasUserInteracted = false

    init { setupSocketListeners() }

    private fun setupSocketListeners() {
        SocketManager.onMessageReceived { data ->
            val senderId = data.optString("senderId", "")
            val encoded = data.optString("content", "")
            val imagesArr = data.optJSONArray("images")
            val imageUrl = imagesArr?.takeIf { it.length() > 0 }?.getString(0)

            val text = if (encoded.isNotBlank()) {
                val cipher = Base64.decode(encoded, Base64.NO_WRAP)
                val key = sharedKey ?: throw IllegalStateException("Shared key chưa derive!")
                val plain = CryptoHelper.decrypt(key, cipher)
                String(plain, Charsets.UTF_8)
            } else ""

            val myUserId = authRepository.getMyUserId()
            val fromMe = senderId == myUserId
            val id = data.optString("messageId", UUID.randomUUID().toString())
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
            val sentAt = SimpleDateFormat("HH:mm", Locale("vi", "VN")).format(Date(timestamp))

            val item = if (!imageUrl.isNullOrBlank()) {
                ChatItem.ImageMessage(id, senderId, null, imageUrl, sentAt, fromMe)
            } else {
                ChatItem.TextMessage(id, senderId, null, text, sentAt, fromMe)
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
            Handler(Looper.getMainLooper()).postDelayed({ _typingState.postValue(false) }, 3000)
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
                is DataResult.Error -> _messagesState.value = UiState.Error(result.error)
            }
        }
    }

    fun loadMore(roomId: String) {
        if (!hasMore || oldestSentAt == null) return
        _loadMoreState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.getMessages(roomId, before = oldestSentAt)) {
                is DataResult.Success -> {
                    val decryptedNew = decryptItems(result.data)
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
                is DataResult.Error -> _loadMoreState.value = UiState.Error(result.error)
            }
        }
    }

    fun sendRoomChatId(roomId: String) { SocketManager.sendRoomChatId(roomId) }

    /**
     * Chỉ 1 socket cho cả text + images (data URI)
     */
    fun sendMessage(content: String, images: List<String> = emptyList()) {
        val text = content.trim()
        if (text.isEmpty() && images.isEmpty()) return

        hasUserInteracted = true
        _sendState.value = UiState.Loading

        try {
            val key = sharedKey ?: throw IllegalStateException("Shared key chưa derive!")
            val cipher = CryptoHelper.encrypt(key, text.toByteArray(Charsets.UTF_8))
            val encoded = Base64.encodeToString(cipher, Base64.NO_WRAP)

            SocketManager.sendMessage(encoded, images) // 1 event duy nhất
            Log.d("MessageViewModel", "Send message: text=${encoded.take(24)}..., images=${images.size}")

            _sendState.value = UiState.Success(Unit)
            resetSendState()
        } catch (e: Exception) {
            _sendState.value = UiState.Error(MappedError(e.message ?: "Gửi thất bại"))
        }
    }

    fun sendIsTyping() { SocketManager.sendTyping() }

    private fun resetSendState() { hasUserInteracted = false; _sendState.value = UiState.Idle }

    override fun onCleared() { super.onCleared(); hasUserInteracted = false }

    private fun decryptItems(raw: List<ChatItem>): List<ChatItem> {
        val key = sharedKey ?: throw IllegalStateException("Shared key chưa derive!")
        return raw.map { item ->
            when (item) {
                is ChatItem.TextMessage -> {
                    val cipher = Base64.decode(item.text, Base64.NO_WRAP)
                    val plain = CryptoHelper.decrypt(key, cipher)
                    item.copy(text = String(plain, Charsets.UTF_8))
                }
                else -> item
            }
        }
    }

    // === Encode ảnh → Base64 data URI, chạy nền và trả qua callback ===
    fun encodeImageToDataUri(
        ctx: Context,
        uri: Uri,
        maxWidth: Int = 1600,
        maxHeight: Int = 1600,
        startQuality: Int = 92,
        maxBase64Bytes: Long = 2_500_000, // ~2.5MB sau Base64
        preferWebp: Boolean = Build.VERSION.SDK_INT >= 30,
        keepPngIfAlpha: Boolean = true,
        onDone: (String) -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            val dataUri = withContext(Dispatchers.IO) {
                val cr = ctx.contentResolver

                fun decodeBitmap(): Bitmap {
                    val bmp = if (Build.VERSION.SDK_INT >= 28) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(cr, uri))
                    } else {
                        cr.openInputStream(uri).use { input ->
                            BitmapFactory.decodeStream(input!!)!!
                        }
                    }
                    val orientation = try {
                        cr.openInputStream(uri).use { input ->
                            ExifInterface(input!!).getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                        }
                    } catch (_: Exception) {
                        ExifInterface.ORIENTATION_NORMAL
                    }
                    val m = android.graphics.Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
                    }
                    return if (!m.isIdentity) Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true) else bmp
                }

                val original = decodeBitmap()

                val ratio = original.width.toFloat() / original.height
                val tw = min(maxWidth, original.width)
                val th = min(maxHeight, original.height)
                val (w, h) = if (tw / ratio <= th) tw to (tw / ratio).roundToInt()
                else ((th * ratio).roundToInt() to th)
                val scaled = if (w != original.width || h != original.height)
                    Bitmap.createScaledBitmap(original, w, h, true) else original

                val hasAlpha = scaled.hasAlpha()
                val usePng = keepPngIfAlpha && hasAlpha
                val fmt = when {
                    usePng -> Bitmap.CompressFormat.PNG
                    preferWebp && Build.VERSION.SDK_INT >= 30 -> Bitmap.CompressFormat.WEBP_LOSSY
                    else -> Bitmap.CompressFormat.JPEG
                }

                var q = if (usePng) 100 else startQuality
                val baos = ByteArrayOutputStream()

                fun compress(quality: Int): ByteArray {
                    baos.reset()
                    scaled.compress(fmt, if (usePng) 100 else quality, baos)
                    return baos.toByteArray()
                }

                var bytes = compress(q)
                fun base64SizeOf(raw: Int) = ((raw + 2) / 3) * 4
                if (!usePng) {
                    while (base64SizeOf(bytes.size) > maxBase64Bytes && q > 50) {
                        q -= 5
                        bytes = compress(q)
                    }
                }

                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val mime = when {
                    usePng -> "image/png"
                    fmt == Bitmap.CompressFormat.WEBP || (preferWebp && !usePng) -> "image/webp"
                    else -> "image/jpeg"
                }
                "data:$mime;base64,$b64"
            }
            onDone(dataUri)
            _sendState.value = UiState.Success(Unit) // đánh dấu xong phần encode
        } catch (e: Exception) {
            onError(e.message ?: "Mã hoá ảnh thất bại")
            _sendState.value = UiState.Error(MappedError(e.message ?: "Mã hoá ảnh thất bại"))
        }
    }
}
