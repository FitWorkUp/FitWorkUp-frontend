package com.fitworkup.app.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val status: Flow<ConnectivityStatus> = callbackFlow {
        fun currentStatus(): ConnectivityStatus {
            val capabilities = connectivityManager
                .getNetworkCapabilities(connectivityManager.activeNetwork)

            // NET_CAPABILITY_VALIDATED testa acesso à internet pública, não a
            // disponibilidade de uma API local. No emulador, 10.0.2.2 pode estar
            // acessível mesmo quando o Android ainda não marcou a rede como validada.
            val hasNetwork = capabilities
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            return if (hasNetwork) {
                ConnectivityStatus.ONLINE
            } else {
                ConnectivityStatus.OFFLINE
            }
        }

        fun publishCurrentStatus() {
            trySend(currentStatus())
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = publishCurrentStatus()

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) = publishCurrentStatus()

            override fun onLost(network: Network) = publishCurrentStatus()

            override fun onUnavailable() = publishCurrentStatus()
        }

        publishCurrentStatus()
        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}
