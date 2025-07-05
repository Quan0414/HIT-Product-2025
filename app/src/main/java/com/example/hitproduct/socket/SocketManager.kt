package com.example.hitproduct.socket

import android.os.Handler
import android.os.Looper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

object SocketManager {
    private lateinit var socket: Socket
    private const val SERVER_URL = "http://your-socket-url.com"

    fun connect() {
        if (::socket.isInitialized && socket.connected()) return

        try {
            val opts = IO.Options()
            opts.reconnection = true
            socket = IO.socket(SERVER_URL, opts)
            socket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        if (::socket.isInitialized && socket.connected()) {
            socket.disconnect()
            socket.off() // ngắt toàn bộ listener
        }
    }

    /**
     * Gửi lời mời (invite) đến user khác
     */
    fun sendInvite(fromUser: String, toUser: String) {
        val json = JSONObject().apply {
            put("from", fromUser)
            put("to", toUser)
        }
        socket.emit("SEND_INVITE", json)
    }

    /**
     * Đăng ký callback khi có lời mời đến
     */
    fun listenForInvites(onInviteReceived: (JSONObject) -> Unit) {
        socket.on("RECEIVE_INVITE") { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                val data = args[0] as JSONObject
                // Chạy callback trên Main Thread để cập nhật UI
                Handler(Looper.getMainLooper()).post {
                    onInviteReceived(data)
                }
            }
        }
    }

    // Các listener mặc định
    private val onConnect = Emitter.Listener {
        println("🟢 Socket connected: ${'$'}{socket.id()}")
    }
    private val onDisconnect = Emitter.Listener {
        println("🔴 Socket disconnected")
    }
    private val onError = Emitter.Listener { args ->
        println("❗ Socket error: ${'$'}{args.joinToString()}")
    }

    // Listener nội bộ cho sự kiện RECEIVE_INVITE (fallback)
    private val onReceiveInvite = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val data = args[0] as JSONObject
            Handler(Looper.getMainLooper()).post {
                // Thông báo mặc định nếu chưa override bằng listenForInvites
                println("📩 Invite received: $data")
            }
        }
    }

}

