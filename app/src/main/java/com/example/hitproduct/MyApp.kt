package com.example.hitproduct

import android.app.Application
import com.example.hitproduct.socket.SocketManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SocketManager.connect() // kết nối socket 1 lần duy nhất
    }
}