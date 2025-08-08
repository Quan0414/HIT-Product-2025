package com.example.hitproduct.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkMonitor(private val context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val onlineNetworks = mutableSetOf<Network>()
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val callback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            // chờ onCapabilitiesChanged để biết có INTERNET/VALIDATED không
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            val ok = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (ok) {
                onlineNetworks.add(network)
            } else {
                onlineNetworks.remove(network)
            }
            _isOnline.value = onlineNetworks.isNotEmpty()
        }

        override fun onLost(network: Network) {
            onlineNetworks.remove(network)
            _isOnline.value = onlineNetworks.isNotEmpty()
        }
    }

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(callback)
        } else {
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(req, callback)
        }

        // cập nhật lần đầu (best-effort)
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val ok = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        if (ok) _isOnline.value = true
    }

    fun stop() {
        runCatching { cm.unregisterNetworkCallback(callback) }
        onlineNetworks.clear()
        _isOnline.value = false
    }
}