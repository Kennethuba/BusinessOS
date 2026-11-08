package com.example.businessos.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.businessos.data.repository.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SyncUiState(val label: String) {
    OFFLINE("Offline"),
    SYNCING("Syncing…"),
    SYNCED("All synced")
}

class SyncManager(
    private val context: Context,
    private val syncRepository: SyncRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _syncState = MutableStateFlow(SyncUiState.SYNCED)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    init {
        checkInitialConnectivity()
        registerNetworkCallback()
    }

    private fun checkInitialConnectivity() {
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _syncState.value = if (isConnected) SyncUiState.SYNCED else SyncUiState.OFFLINE
    }

    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch {
                        triggerSync()
                    }
                }

                override fun onLost(network: Network) {
                    _syncState.value = SyncUiState.OFFLINE
                }
            })
        } catch (_: Exception) {}
    }

    suspend fun triggerSync() {
        _syncState.value = SyncUiState.SYNCING
        try {
            syncRepository.syncAllPending()
            _syncState.value = SyncUiState.SYNCED
        } catch (e: Exception) {
            _syncState.value = SyncUiState.OFFLINE
        }
    }
}
