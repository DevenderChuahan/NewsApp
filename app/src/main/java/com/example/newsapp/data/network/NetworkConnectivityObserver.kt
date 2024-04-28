package com.example.newsapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkConnectivityObserver @Inject constructor(@ApplicationContext private val context: Context) : ConnectivityObserver {
    private val connectivityService = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    override fun observer(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            val callBack = object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        send(ConnectivityObserver.Status.Available)
                    }
                }
                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch {
                        send(ConnectivityObserver.Status.Losing)
                    }
                }
                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch {
                        send(ConnectivityObserver.Status.Lost)
                    }
                }
                override fun onUnavailable() {
                    super.onUnavailable()
                    launch {
                        send(ConnectivityObserver.Status.Unavailable)
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityService.registerDefaultNetworkCallback(callBack)
            } else {
                val networkChangeFilter = NetworkRequest.Builder().build()
                connectivityService.registerNetworkCallback(networkChangeFilter, callBack)
            }
            awaitClose {
                connectivityService.unregisterNetworkCallback(callBack)
            }
        }.distinctUntilChanged()
    }
}