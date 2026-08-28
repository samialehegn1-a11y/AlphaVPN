package com.example.vpn.service

import com.example.vpn.model.LogLevel
import com.example.vpn.model.VpnConfig
import com.example.vpn.model.VpnLog
import com.example.vpn.model.VpnServer
import com.example.vpn.model.VpnSessionStats
import com.example.vpn.model.VpnState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object VpnServiceManager {
    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _sessionStats = MutableStateFlow(VpnSessionStats())
    val sessionStats: StateFlow<VpnSessionStats> = _sessionStats.asStateFlow()

    private val _activeServer = MutableStateFlow<VpnServer?>(null)
    val activeServer: StateFlow<VpnServer?> = _activeServer.asStateFlow()

    private val _logs = MutableStateFlow<List<VpnLog>>(emptyList())
    val logs: StateFlow<List<VpnLog>> = _logs.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun updateState(newState: VpnState) {
        _vpnState.value = newState
        addLog(
            LogLevel.INFO,
            "STATE",
            "VPN state transition -> ${newState.name}"
        )
    }

    fun updateStats(stats: VpnSessionStats) {
        _sessionStats.value = stats
    }

    fun updateStatsTransform(transform: (VpnSessionStats) -> VpnSessionStats) {
        _sessionStats.update(transform)
    }

    fun setActiveServer(server: VpnServer?) {
        _activeServer.value = server
    }

    fun setLastError(error: String?) {
        _lastError.value = error
        if (error != null) {
            addLog(LogLevel.ERROR, "ERROR", error)
        }
    }

    fun addLog(level: LogLevel, tag: String, message: String) {
        val newLog = VpnLog(level = level, tag = tag, message = message)
        _logs.update { currentList ->
            // Keep max 200 logs
            (listOf(newLog) + currentList).take(200)
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
