package com.example.hitproduct.socket

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hitproduct.common.constants.ApiConstants
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.data.model.message.ChatItem
import io.socket.client.IO
import io.socket.client.IO.Options
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * SocketManager handles real-time friend request events:
 * - sendFriendRequest ↔︎ USER_REQUEST_FRIEND
 * - cancelFriendRequest ↔︎ USER_CANCEL_FRIEND
 * - refuseFriendRequest ↔︎ USER_REFUSE_FRIEND
 * - acceptFriendRequest ↔︎ USER_ACCEPT_FRIEND
 *
 * Listeners for server responses:
 * - onRequestSent ↔︎ SERVER_RETURN_USER_REQUEST
 * - onIncomingRequest ↔︎ SERVER_RETURN_USER_ACCEPT
 * - onRequestCancelled ↔︎ SERVER_RETURN_USER_CANCEL_REQUEST
 * - onCancelReceived ↔︎ SERVER_RETURN_USER_CANCEL_ACCEPT
 * - onRequestRefused ↔︎ SERVER_RETURN_USER_REFUSE_ACCEPT
 * - onRefusalReceived ↔︎ SERVER_RETURN_USER_REFUSE_REQUEST
 */
object SocketManager {
    private lateinit var socket: Socket
    private const val SERVER_URL = ApiConstants.BASE_URL
    private var authToken: String? = null

    private lateinit var prefs: SharedPreferences
    fun init(context: Context) {
        prefs = context.getSharedPreferences(
            AuthPrefersConstants.PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }


    private val _notifications = MutableLiveData<JSONObject>()
    val notifications: LiveData<JSONObject> = _notifications

    /**
     * Kết nối tới Socket server kèm token (server lấy myUserId từ token)
     */
    fun connect(token: String) {
        if (::socket.isInitialized && socket.connected() && authToken == token) return
        authToken = token
        if (::socket.isInitialized) {
            socket.disconnect()
            socket.off()
        }
        val opts = Options().apply {
            reconnection = true
            auth = mapOf("token" to token)
        }
        socket = IO.socket(SERVER_URL, opts)
        socket.connect()
        Log.d("SocketManager", "Connecting to socket with token: $token")
    }

    /**
     * Callback khi kết nối thành công
     */
    fun onConnected(listener: () -> Unit) {
        socket.on(Socket.EVENT_CONNECT) {
            Handler(Looper.getMainLooper()).post { listener() }
        }
    }

    /**
     * Kiểm tra trạng thái kết nối hiện tại
     */
    fun isConnected(): Boolean = ::socket.isInitialized && socket.connected()

    /**
     * Ngắt kết nối và xóa listeners
     */
    fun disconnect() {
        if (::socket.isInitialized && socket.connected()) {
            socket.disconnect()
            socket.off()
        }
        authToken = null
    }

    /**
     * Lắng nghe lỗi chung từ server (event 'ERROR')
     */
    fun onError(listener: (message: String) -> Unit) {
        socket.on("ERROR") { args ->
            val msg = (args.getOrNull(0) as? JSONObject)?.optString("message") ?: "Unknown error"
            Handler(Looper.getMainLooper()).post { listener(msg) }
        }
    }

    /**
     * Lắng nghe thông báo thành công chung từ server (event 'SUCCESS')
     */
    fun onSuccess(listener: (message: String) -> Unit) {
        socket.on("SUCCESS") { args ->
            val msg = (args.getOrNull(0) as? JSONObject)?.optString("message") ?: ""
            Handler(Looper.getMainLooper()).post { listener(msg) }
        }
    }

    // === Emitting events to server ===

    /**
     * Gửi USER_REQUEST_FRIEND để mời kết đôi chỉ với coupleCode
     */
    fun sendFriendRequest(coupleCode: String) {
        val payload = JSONObject().apply { put("coupleCode", coupleCode) }
        socket.emit("USER_REQUEST_FRIEND", payload)
    }

    /**
     * Gửi USER_CANCEL_FRIEND để huỷ lời mời đã gửi chỉ với yourUserId
     */
    fun cancelFriendRequest(yourUserId: String) {
        val payload = JSONObject().apply { put("yourUserId", yourUserId) }
        socket.emit("USER_CANCEL_FRIEND", payload)
    }

    /**
     * Gửi USER_REFUSE_FRIEND để từ chối lời mời đến chỉ với userId
     */
    fun refuseFriendRequest(userId: String) {
        val payload = JSONObject().apply { put("userId", userId) }
        socket.emit("USER_REFUSE_FRIEND", payload)
    }

    /**
     * Gửi USER_ACCEPT_FRIEND để chấp nhận lời mời đến chỉ với yourUserId
     */
    fun acceptFriendRequest(yourUserId: String) {
        val payload = JSONObject().apply { put("yourUserId", yourUserId) }
        socket.emit("USER_ACCEPT_FRIEND", payload)
    }

    // === Listeners cho event server trả về ===

    /**
     * SERVER_RETURN_USER_REQUEST: server xác nhận đã gửi lời mời
     * Thêm vào list "Sent"
     */
    fun onRequestSent(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_REQUEST") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /**
     * SERVER_RETURN_USER_ACCEPT: có lời mời mới đến
     * Thêm vào list "Received"
     */
    fun onIncomingRequest(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_ACCEPT") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /**
     * SERVER_RETURN_USER_CANCEL_REQUEST: server xác nhận huỷ lời mời của A
     * Xóa khỏi list "Sent"
     */
    fun onRequestCancelled(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_CANCEL_REQUEST") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /**
     * SERVER_RETURN_USER_CANCEL_ACCEPT: A huỷ lời mời, B sẽ xóa khỏi "Received"
     */
    fun onCancelReceived(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_CANCEL_ACCEPT") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /**
     * SERVER_RETURN_USER_REFUSE_ACCEPT: A từ chối lời mời, server xác nhận xóa khỏi "Received"
     */
    fun onRequestRefused(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_REFUSE_ACCEPT") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /**
     * SERVER_RETURN_USER_REFUSE_REQUEST: B nhận được A từ chối, server xóa khỏi "Sent"
     */
    fun onRefusalReceived(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_REFUSE_REQUEST") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }


    //B chap nhan loi moi, xoa thong bao ben B
    fun onAccepted(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_ACCEPT_ACCEPT") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    //B chap nhan loi moi, xoa thong bao ben A
    fun onTheyAccept(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_REQUEST_REQUEST") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    fun onListenCouple(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_COUPLE") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Log.d("SocketManager", "Received: $data")
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    //=====================================================
    // Check start date
    fun onCheckStartDate(listener: (data: JSONObject) -> Unit) {
        Log.d("SocketManager", "onCheckStartDate called")
        socket.on("LOVE_DATE_UPDATED_BY_PARTNER") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Log.d("SocketManager", "abc$data")
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }


    //====================================== Nuoi pet
    // Gửi trạng thái mèo qua Socket với key PET_ACTIVE
    fun sendCatStateToSocket(state: String, myLoveId: String) {
        val payLoad = JSONObject().apply {
            put("active", state)
            put("myLoveId", myLoveId)
        }
        socket.emit("USER_SEND_PET_ACTIVE", payLoad)
        Log.d("SocketManager", "Sent cat state: $state, sendTo: $myLoveId")
    }

    //Listener
    fun onListenForPetActive(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_SEND_PET_ACTIVE") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            val sendTo = data.optString("myLoveId", "")
            val myId = prefs.getString(AuthPrefersConstants.MY_USER_ID, "") ?: ""
            Log.d("SocketManager", "onListenForPetActive: data=$data, myLoveId=$sendTo, myId=$myId")
            // Nếu không phải gửi cho mình thì bỏ qua
            if (sendTo != myId) return@on

            Log.d("SocketManager", "Received pet active: $data")
            Handler(Looper.getMainLooper()).post { listener(data) }
        }
    }

    fun onFeedPetSuccess(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_FEED_PET_SUCCESS") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    fun onDecreaseHunger(listener: (data: JSONObject) -> Unit) {
        socket.on("PET_DECREASE_HUNGER") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }


    //=====================================================
    // Mission
    fun onMissionCompleted(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_MISSION_COMPLETED") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Log.d("SocketManager", "Mission completed: $data")
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    //=====================================================
    // Notification

    fun onNotificationReceived(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_SEND_NOT_TO_USER") { args ->
            // 1. Lấy wrapper JSON
            val wrapper = args.getOrNull(0) as? JSONObject ?: return@on
            // 2. Unwrap object "not" (nếu không có thì bỏ qua)
            val notObj = wrapper.optJSONObject("not") ?: return@on

            Log.d("SocketManager", "unwrapped notification: $notObj")
            Handler(Looper.getMainLooper()).post {
                // 3. Gọi callback với object đã unwrap
                listener(notObj)
                // 4. Đẩy vào LiveData cũng là object unwrap
                _notifications.postValue(notObj)
            }
        }
    }

    //=====================================================
    // Message

    fun sendRoomChatId(roomId: String) {
        val payload = JSONObject().apply {
            put("roomChatId", roomId)
        }
        socket.emit("USER_SEND_ROOM_CHAT_ID", payload)
        Log.d("SocketManager", "Sending roomChatId: $roomId")
    }

    fun sendMessage(content: String, images: List<String> = emptyList()) {
        val myUserId = prefs.getString(AuthPrefersConstants.MY_USER_ID, "") ?: ""
        val myLoveId = prefs.getString(AuthPrefersConstants.MY_LOVE_ID, "") ?: ""
        val coupleId = prefs.getString(AuthPrefersConstants.COUPLE_ID, "") ?: ""
        val payload = JSONObject().apply {
            put("coupleId", coupleId)
            put("senderId", myUserId)
            put("toUserId", myLoveId)
            put("content", content)
            put("images", JSONArray(images))
            put("coupleId", coupleId)
        }
        Log.d("SocketManager", "Sending message: $payload")
        socket.emit("USER_SEND_MESSAGE", payload)
    }

    fun sendTyping() {
        val myUserId = prefs.getString(AuthPrefersConstants.MY_USER_ID, "") ?: ""
        val payload = JSONObject().apply {
            put("senderId", myUserId)
        }
        socket.emit("CLIENT_SEND_TYPING", payload)
        Log.d("SocketManager", "emit CLIENT_SEND_TYPING: $payload")
    }

    /**
     * Lắng nghe sự kiện gửi tin nhắn từ server
     */
    fun onMessageReceived(listener: (JSONObject) -> Unit): Emitter =
        socket.on("SERVER_RETURN_MESSAGE") { args ->
            (args.firstOrNull() as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }

    fun onTypingReceived(listener: (senderId: String) -> Unit) {
        socket.on("SERVER_RETURN_TYPING") { args ->
            (args.firstOrNull() as? JSONObject)?.let { data ->
                Log.d("SocketManager", "Received typing from: ${data.optString("senderId")}")
                val senderId = data.optString("senderId", "")
                Handler(Looper.getMainLooper()).post {
                    listener(senderId)
                }
            }
        }
    }

    //=======================================================
    // AES
    fun sendNewPubKey(pubKey: String, myLoveId: String) {
        val payload = JSONObject().apply {
            put("public_key", pubKey)
            put("myLoveId", myLoveId)
        }
        socket.emit("USER_SEND_PUBLIC_KEY", payload)
        Log.d("SocketManager", "Sending key: $pubKey, to: $myLoveId")
    }

    fun onNewPubKeyReceived(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_PUBLIC_KEY") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Log.d("SocketManager", "Received new public key: $data")
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

}
