package com.example.hitproduct

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.socket.SocketManager

class MyApp : Application() {

    private val prefs by lazy {
        getSharedPreferences(AuthPrefersConstants.PREFS_NAME, MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()

        SocketManager.init(this)

        val token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "")

        if (!token.isNullOrEmpty()) {
            SocketManager.connect(token)
            registerSocketListeners()
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun registerSocketListeners() {
        SocketManager.onNotificationReceived { data ->
            Log.d("MyApp", "$data")
        }
    }
}

