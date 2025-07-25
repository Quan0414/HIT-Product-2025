package com.example.hitproduct.socket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.hitproduct.R
import com.example.hitproduct.common.constants.ApiConstants
import io.socket.client.IO
import io.socket.client.IO.Options
import io.socket.client.Socket
import org.json.JSONObject

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


    // Nuoi pet


    // Gửi trạng thái mèo qua Socket với key PET_ACTIVE
    fun sendCatStateToSocket(state: String, myLoveId: String) {
        val payLoad = JSONObject().apply {
            put("active", state)
            put("myLoveId", myLoveId)
        }
        socket.emit("USER_SEND_PET_ACTIVE", payLoad)
        Log.d("SocketManager", "Sent cat state: $state, myLoveId: $myLoveId")
    }

    //Listener
    fun onListenForPetActive(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_SEND_PET_ACTIVE") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Log.d("SocketManager", "Received pet active data: $data")
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
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

    /**
     * Kiểm tra trạng thái kết nối hiện tại
     */
    fun isConnected(): Boolean = ::socket.isInitialized && socket.connected()

}
