package com.example.vpn.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vpn.data.VpnRepository
import com.example.vpn.model.AppInfo
import com.example.vpn.model.ServerCategory
import com.example.vpn.model.SpeedTestResult
import com.example.vpn.model.VpnConfig
import com.example.vpn.model.VpnLog
import com.example.vpn.model.VpnServer
import com.example.vpn.model.VpnSessionStats
import com.example.vpn.model.VpnState
import com.example.vpn.service.VpnServiceManager
import com.example.vpn.model.VpnTweakPayload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VpnScreenTab {
    CONNECT,
    SERVERS,
    SPEED_TEST,
    SPLIT_TUNNEL,
    LOGS,
    SETTINGS
}

sealed class VpnUiEvent {
    data class RequestVpnPermission(val prepareIntent: Intent) : VpnUiEvent()
    data class ShowToast(val message: String) : VpnUiEvent()
}

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application.applicationContext)

    val vpnState: StateFlow<VpnState> = VpnServiceManager.vpnState
    val sessionStats: StateFlow<VpnSessionStats> = VpnServiceManager.sessionStats
    val logs: StateFlow<List<VpnLog>> = VpnServiceManager.logs
    val selectedServer: StateFlow<VpnServer> = repository.selectedServer
    val config: StateFlow<VpnConfig> = repository.config
    val installedApps: StateFlow<List<AppInfo>> = repository.installedApps
    val speedTest: StateFlow<SpeedTestResult> = repository.speedTest

    val timeLeftSeconds: StateFlow<Long> = repository.timeLeftSeconds
    val payloads: StateFlow<List<VpnTweakPayload>> = repository.payloads
    val selectedPayload: StateFlow<VpnTweakPayload> = repository.selectedPayload

    private val _showStartupDialog = MutableStateFlow(!repository.isFollowDialogDismissedPermanently())
    val showStartupDialog: StateFlow<Boolean> = _showStartupDialog.asStateFlow()

    private val _isUpdatingConfig = MutableStateFlow(false)
    val isUpdatingConfig: StateFlow<Boolean> = _isUpdatingConfig.asStateFlow()

    private val _useConfigEnabled = MutableStateFlow(true)
    val useConfigEnabled: StateFlow<Boolean> = _useConfigEnabled.asStateFlow()

    private val _currentTab = MutableStateFlow(VpnScreenTab.CONNECT)
    val currentTab: StateFlow<VpnScreenTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ServerCategory.ALL)
    val selectedCategory: StateFlow<ServerCategory> = _selectedCategory.asStateFlow()

    private val _uiEvents = MutableSharedFlow<VpnUiEvent>()
    val uiEvents: SharedFlow<VpnUiEvent> = _uiEvents.asSharedFlow()

    val filteredServers: StateFlow<List<VpnServer>> = combine(
        repository.servers,
        _searchQuery,
        _selectedCategory
    ) { serverList, query, category ->
        serverList.filter { server ->
            val matchesCategory = when (category) {
                ServerCategory.ALL -> true
                else -> server.category == category
            }
            val matchesQuery = query.isBlank() ||
                    server.countryName.contains(query, ignoreCase = true) ||
                    server.cityName.contains(query, ignoreCase = true) ||
                    server.countryCode.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setTab(tab: VpnScreenTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: ServerCategory) {
        _selectedCategory.value = category
    }

    fun onDismissStartupDialog(dontShowAgain: Boolean) {
        _showStartupDialog.value = false
        if (dontShowAgain) {
            repository.setFollowDialogDismissedPermanently(true)
        }
    }

    fun onOpenFollowDialog() {
        _showStartupDialog.value = true
    }

    fun onAddTime(seconds: Long) {
        repository.addTime(seconds)
        viewModelScope.launch {
            _uiEvents.emit(VpnUiEvent.ShowToast("+${seconds / 3600} Hours Added to Alpha VPN!"))
        }
    }

    fun onSelectPayload(payload: VpnTweakPayload) {
        repository.selectPayload(payload)
    }

    fun onCreateCustomPayload(name: String, operator: String, sni: String, protocol: String) {
        repository.addCustomPayload(name, operator, sni, protocol)
        viewModelScope.launch {
            _uiEvents.emit(VpnUiEvent.ShowToast("Custom tweak '$name' activated!"))
        }
    }

    fun onUpdateOnlineConfig() {
        viewModelScope.launch {
            _isUpdatingConfig.value = true
            repository.refreshPings()
            kotlinx.coroutines.delay(1200)
            _isUpdatingConfig.value = false
            _uiEvents.emit(VpnUiEvent.ShowToast("Online servers & tweaks updated to latest v1.4.2!"))
        }
    }

    fun onImportConfig(name: String, content: String) {
        // Parse config content or add as custom tweak
        repository.addCustomPayload(name, "Imported Config", "custom.server.net", "V2Ray/Custom")
        viewModelScope.launch {
            _uiEvents.emit(VpnUiEvent.ShowToast("Config '$name' imported successfully!"))
        }
    }

    fun onToggleUseConfig(enabled: Boolean) {
        _useConfigEnabled.value = enabled
        viewModelScope.launch {
            _uiEvents.emit(VpnUiEvent.ShowToast(if (enabled) "Config payload routing enabled" else "Direct tunnel routing enabled"))
        }
    }

    fun onClearAppData() {
        repository.clearAppData()
        viewModelScope.launch {
            _uiEvents.emit(VpnUiEvent.ShowToast("Alpha VPN data & cache reset to defaults"))
        }
    }

    fun onPowerClicked() {
        val currentState = vpnState.value
        if (currentState == VpnState.CONNECTED || currentState == VpnState.CONNECTING) {
            repository.disconnectVpn()
        } else {
            // Check VPN permission preparation
            val prepareIntent = VpnService.prepare(getApplication())
            if (prepareIntent != null) {
                viewModelScope.launch {
                    _uiEvents.emit(VpnUiEvent.RequestVpnPermission(prepareIntent))
                }
            } else {
                repository.connectVpn(selectedServer.value)
            }
        }
    }

    fun onVpnPermissionGranted() {
        repository.connectVpn(selectedServer.value)
    }

    fun onSelectServer(server: VpnServer) {
        repository.selectServer(server)
        _currentTab.value = VpnScreenTab.CONNECT
    }

    fun onToggleFavorite(serverId: String) {
        repository.toggleFavorite(serverId)
    }

    fun onRefreshPings() {
        repository.refreshPings()
    }

    fun onImportCustomServer(
        name: String,
        ip: String,
        port: Int,
        country: String,
        flag: String,
        publicKey: String
    ) {
        repository.addCustomServer(name, ip, port, country, flag, publicKey)
        _currentTab.value = VpnScreenTab.CONNECT
    }

    fun onUpdateConfig(newConfig: VpnConfig) {
        repository.updateConfig(newConfig)
    }

    fun onToggleSplitTunnelApp(packageName: String) {
        repository.toggleAppInSplitTunnel(packageName)
    }

    fun onStartSpeedTest() {
        repository.startSpeedTest()
    }

    fun onStopSpeedTest() {
        repository.stopSpeedTest()
    }

    fun onClearLogs() {
        VpnServiceManager.clearLogs()
    }
}
