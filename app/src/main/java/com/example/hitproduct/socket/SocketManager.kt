package com.example.hitproduct.socket

import android.os.Handler
import android.os.Looper
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    private lateinit var socket: Socket
    private const val SERVER_URL = "https://love-story-app-1.onrender.com"

    fun connect() {
        if (::socket.isInitialized && socket.connected()) return
        socket = IO.socket(SERVER_URL, IO.Options().apply { reconnection = true })
        socket.connect()
    }

    /** Cho phép đăng ký callback khi kết nối thành công */
    fun onConnected(listener: () -> Unit) {
        socket.on(Socket.EVENT_CONNECT) {
            Handler(Looper.getMainLooper()).post {
                listener()
            }
        }
    }


    fun disconnect() {
        if (::socket.isInitialized && socket.connected()) {
            socket.disconnect()
            socket.off()
        }
    }

    /** Listener chung cho event ERROR mà server emit không qua callback */
    fun onError(listener: (message: String) -> Unit) {
        socket.on("ERROR") { args ->
            val obj = args.getOrNull(0) as? JSONObject
            val msg = obj?.optString("message") ?: "Unknown error"
            Handler(Looper.getMainLooper()).post {
                listener(msg)
            }
        }
    }


    fun onSuccess(listener: (message: String) -> Unit) {
        socket.on("SUCCESS") { args ->
            val obj = args.getOrNull(0) as? JSONObject
            val msg = obj?.optString("message") ?: ""
            Handler(Looper.getMainLooper()).post {
                listener(msg)
            }
        }
    }


    /**
     * Gửi USER_REQUEST_FRIEND kèm ACK
     * server callback({ status:"success"|"error", message:String })
     */
    fun sendFriendRequest(
        myUserId: String,
        coupleCode: String,
        onResult: (success: Boolean, message: String?) -> Unit
    ) {
        val payload = JSONObject().apply {
            put("myUserId", myUserId)
            put("coupleCode", coupleCode)
        }

        socket.emit("USER_REQUEST_FRIEND", payload, Ack { args ->
            // args[0] là JSONObject { status, message }
            val ack = args.getOrNull(0) as? JSONObject
            val status = ack?.optString("status")
            val message = ack?.optString("message")
            val success = status == "success"
            // Đảm bảo chạy callback trên main thread
            Handler(Looper.getMainLooper()).post {
                onResult(success, message)
            }
        })
    }

    // 2. Hủy lời mời
    fun cancelFriendRequest(myUserId: String, userId: String) {
        val payload = JSONObject().apply {
            put("myUserId", myUserId)
            put("userId", userId)
        }
        socket.emit("USER_CANCEL_FRIEND", payload)
    }

    // 3. Từ chối lời mời đến
    fun refuseFriendRequest(myUserId: String, userId: String) {
        val payload = JSONObject().apply {
            put("myUserId", myUserId)
            put("userId", userId)
        }
        socket.emit("USER_REFUSE_FRIEND", payload)
    }

    // 4. Chấp nhận lời mời đến
    fun acceptFriendRequest(myUserId: String, userId: String) {
        val payload = JSONObject().apply {
            put("myUserId", myUserId)
            put("userId", userId)
        }
        socket.emit("USER_ACCEPT_FRIEND", payload)
    }

    // ==== Listeners ====

    /** Khi server confirm bạn vừa gửi thành công (đẩy vào list Sent) */
    fun onRequestSent(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_REQUEST") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /** Khi có lời mời mới gửi đến bạn (đẩy vào list Received) */
    fun onIncomingRequest(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_ACCEPT") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /** Khi server confirm bạn vừa hủy request (xoá khỏi Sent) */
    fun onRequestCancelled(listener: (data: JSONObject) -> Unit) {
        socket.on("SERVER_RETURN_USER_CANCEL") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }

    /** Khi server confirm bạn vừa chấp nhận (cập nhật Received → Friend) */
    fun onFriendAccepted(listener: (data: JSONObject) -> Unit) {
        // Tên event có thể là SERVER_RETURN_USER_ACCEPTED hoặc tương tự, tuỳ backend
        socket.on("SERVER_RETURN_USER_ACCEPTED") { args ->
            (args.getOrNull(0) as? JSONObject)?.let { data ->
                Handler(Looper.getMainLooper()).post { listener(data) }
            }
        }
    }


    fun isConnected(): Boolean {
        return ::socket.isInitialized && socket.connected()
    }

}
