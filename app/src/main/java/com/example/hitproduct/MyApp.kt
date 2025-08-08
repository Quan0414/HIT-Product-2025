package com.example.hitproduct

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.util.CryptoHelper
import com.example.hitproduct.common.util.NetworkMonitor
import com.example.hitproduct.common.util.createNotificationChannel
import com.example.hitproduct.socket.SocketManager
import com.google.firebase.FirebaseApp

class MyApp : Application() {

    private val prefs by lazy {
        getSharedPreferences(AuthPrefersConstants.PREFS_NAME, MODE_PRIVATE)
    }

    lateinit var networkMonitor: NetworkMonitor private set

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        FirebaseApp.initializeApp(this)
        createNotificationChannel()

        SocketManager.init(this)
        val token = prefs.getString(AuthPrefersConstants.ACCESS_TOKEN, "")
        if (!token.isNullOrEmpty()) {
            SocketManager.connect(token)
            registerSocketListeners()
        }

        networkMonitor = NetworkMonitor(this)
        networkMonitor.start()


    }

    private fun registerSocketListeners() {
        SocketManager.onNotificationReceived { data ->
            Log.d("MyApp", "$data")
        }

        SocketManager.onNewPubKeyReceived { data ->
            val newPubKey = data.optString("public_key", "")
            Log.d("MyApp", "New pubkey receive: $newPubKey")
            CryptoHelper.storePeerPublicKey(this, newPubKey)
            CryptoHelper.deriveAndStoreSharedAesKey(this)
        }
    }
}

