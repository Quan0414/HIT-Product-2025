package com.example.hitproduct

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApp : Application() {
    private lateinit var token: String
    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()

//        // 1) Kết nối socket
//        token = getSharedPreferences(
//            "auth_prefs",
//            MODE_PRIVATE
//        ).getString("access_token", "").orEmpty()
//        SocketManager.connect(token)
//
//        // 2) Đăng ký lắng nghe connect thành công, và show Toast ở đây
//        SocketManager.onConnected {
//            Toast.makeText(
//                this,
//                "Đã kết nối tới https://love-story-app-1.onrender.com",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }
}
