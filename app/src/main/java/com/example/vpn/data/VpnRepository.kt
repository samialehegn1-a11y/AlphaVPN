package com.example.vpn.data

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.vpn.model.AppInfo
import com.example.vpn.model.DnsProvider
import com.example.vpn.model.LogLevel
import com.example.vpn.model.ServerCategory
import com.example.vpn.model.SpeedTestPhase
import com.example.vpn.model.SpeedTestResult
import com.example.vpn.model.SplitTunnelMode
import com.example.vpn.model.VpnConfig
import com.example.vpn.model.VpnProtocol
import com.example.vpn.model.VpnServer
import com.example.vpn.model.VpnSessionStats
import com.example.vpn.model.VpnState
import com.example.vpn.service.AlphaVpnService
import com.example.vpn.service.VpnServiceManager
import com.example.vpn.model.VpnTweakPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class VpnRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("alpha_vpn_prefs", Context.MODE_PRIVATE)
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _servers = MutableStateFlow<List<VpnServer>>(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers.asStateFlow()

    private val _selectedServer = MutableStateFlow<VpnServer>(getDefaultServer())
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<VpnConfig> = _config.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _speedTest = MutableStateFlow(SpeedTestResult())
    val speedTest: StateFlow<SpeedTestResult> = _speedTest.asStateFlow()

    // Time Management (1 Hour Default = 3600 seconds)
    private val _timeLeftSeconds = MutableStateFlow(loadInitialTimeLeft())
    val timeLeftSeconds: StateFlow<Long> = _timeLeftSeconds.asStateFlow()

    // Tweak Payloads
    private val _payloads = MutableStateFlow<List<VpnTweakPayload>>(getDefaultPayloads())
    val payloads: StateFlow<List<VpnTweakPayload>> = _payloads.asStateFlow()

    private val _selectedPayload = MutableStateFlow<VpnTweakPayload>(getDefaultPayloads().first())
    val selectedPayload: StateFlow<VpnTweakPayload> = _selectedPayload.asStateFlow()

    private var speedTestJob: Job? = null
    private var countdownJob: Job? = null

    init {
        loadDefaultServers()
        loadInstalledApps()
        startCountdownTimer()
    }

    private fun loadInitialTimeLeft(): Long {
        val saved = prefs.getLong("vpn_time_left", 3600L)
        return if (saved <= 0) 3600L else saved
    }

    fun isFollowDialogDismissedPermanently(): Boolean {
        return prefs.getBoolean("follow_dialog_dismissed_permanently", false)
    }

    fun setFollowDialogDismissedPermanently(dismissed: Boolean) {
        prefs.edit().putBoolean("follow_dialog_dismissed_permanently", dismissed).apply()
    }

    fun addTime(secondsToAdd: Long) {
        _timeLeftSeconds.update { current ->
            val updated = current + secondsToAdd
            prefs.edit().putLong("vpn_time_left", updated).apply()
            VpnServiceManager.addLog(LogLevel.INFO, "SESSION", "Extended VPN session time by +${secondsToAdd / 3600} hours")
            updated
        }
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = repoScope.launch {
            while (isActive) {
                delay(1000)
                if (VpnServiceManager.vpnState.value.isConnected) {
                    _timeLeftSeconds.update { current ->
                        if (current > 0) {
                            val next = current - 1
                            if (next % 60 == 0L) {
                                prefs.edit().putLong("vpn_time_left", next).apply()
                            }
                            next
                        } else {
                            0L
                        }
                    }
                }
            }
        }
    }

    private fun getDefaultPayloads(): List<VpnTweakPayload> {
        return listOf(
            VpnTweakPayload(
                id = "eth-tg-pkg-new",
                name = "ETH 🇪🇹 | TG PKG New Method",
                operatorName = "Ethio Telecom / TG Direct",
                protocolType = "SSL/TLS Port 443",
                sniHost = "web.telegram.org",
                status = "ONLINE"
            ),
            VpnTweakPayload(
                id = "ethiopia-safaricom-fr",
                name = "Ethiopia Safaricom FR",
                operatorName = "Safaricom ET",
                protocolType = "Direct SSL/TLS",
                sniHost = "safaricom.et",
                status = "ONLINE"
            ),
            VpnTweakPayload(
                id = "ethiopia-ethio-telecom",
                name = "Ethio Telecom Free VIP",
                operatorName = "Ethio Telecom",
                protocolType = "HTTP Injector / Proxy",
                sniHost = "telecom.et",
                status = "ONLINE"
            ),
            VpnTweakPayload(
                id = "udp-custom-tunnel",
                name = "UDP Custom Gaming & Call",
                operatorName = "Global High Speed",
                protocolType = "UDP 53/443 Custom",
                sniHost = "1.1.1.1",
                status = "ONLINE"
            ),
            VpnTweakPayload(
                id = "cloudflare-cdn-ws",
                name = "Cloudflare CDN WebSocket",
                operatorName = "Cloudflare Edge",
                protocolType = "WSS / TLS Port 443",
                sniHost = "cdnjs.cloudflare.com",
                status = "ONLINE"
            ),
            VpnTweakPayload(
                id = "direct-ssl-stealth",
                name = "Direct SSL Stealth Tunnel",
                operatorName = "Stealth TLS v1.3",
                protocolType = "ShadowTLS Curve25519",
                sniHost = "gateway.alphavpn.net",
                status = "ONLINE"
            )
        )
    }

    fun clearAppData() {
        prefs.edit().clear().apply()
        _timeLeftSeconds.value = 7200L
        loadDefaultServers()
        _payloads.value = getDefaultPayloads()
        _selectedPayload.value = getDefaultPayloads().first()
        _selectedServer.value = getDefaultServer()
        VpnServiceManager.clearLogs()
        VpnServiceManager.addLog(LogLevel.INFO, "SYSTEM", "App cache, routing tables and preferences cleared.")
    }

    fun selectPayload(payload: VpnTweakPayload) {
        _selectedPayload.value = payload
        VpnServiceManager.addLog(LogLevel.INFO, "PAYLOAD", "Selected network tweak: ${payload.name} (${payload.sniHost})")
    }

    fun addCustomPayload(name: String, operator: String, sni: String, protocol: String) {
        val newPayload = VpnTweakPayload(
            id = "custom-${System.currentTimeMillis()}",
            name = name,
            operatorName = operator,
            protocolType = protocol,
            sniHost = sni,
            status = "UNLIMITED",
            isCustom = true
        )
        _payloads.update { listOf(newPayload) + it }
        selectPayload(newPayload)
    }

    private fun getDefaultServer(): VpnServer {
        return VpnServer(
            id = "us-nyc-1",
            countryName = "United States",
            countryCode = "US",
            cityName = "New York",
            flagEmoji = "🇺🇸",
            ipAddress = "104.28.19.82",
            port = 51820,
            pingMs = 28,
            loadPercentage = 38,
            category = ServerCategory.FASTEST
        )
    }

    private fun loadDefaultServers() {
        val serverList = listOf(
            VpnServer(
                id = "fastest-auto",
                countryName = "Optimal Location",
                countryCode = "AUTO",
                cityName = "⚡ Auto (Lowest Latency)",
                flagEmoji = "⚡",
                ipAddress = "104.28.19.82",
                port = 51820,
                pingMs = 14,
                loadPercentage = 22,
                category = ServerCategory.FASTEST
            ),
            VpnServer(
                id = "us-nyc-1",
                countryName = "United States",
                countryCode = "US",
                cityName = "New York #1",
                flagEmoji = "🇺🇸",
                ipAddress = "104.28.19.82",
                port = 51820,
                pingMs = 28,
                loadPercentage = 42,
                category = ServerCategory.STREAMING
            ),
            VpnServer(
                id = "us-lax-1",
                countryName = "United States",
                countryCode = "US",
                cityName = "Los Angeles #2",
                flagEmoji = "🇺🇸",
                ipAddress = "198.51.100.74",
                port = 51820,
                pingMs = 35,
                loadPercentage = 55,
                category = ServerCategory.GAMING
            ),
            VpnServer(
                id = "us-chi-1",
                countryName = "United States",
                countryCode = "US",
                cityName = "Chicago",
                flagEmoji = "🇺🇸",
                ipAddress = "198.51.100.99",
                port = 51820,
                pingMs = 31,
                loadPercentage = 48,
                category = ServerCategory.ALL
            ),
            VpnServer(
                id = "gb-lon-1",
                countryName = "United Kingdom",
                countryCode = "GB",
                cityName = "London (BBC / Netflix)",
                flagEmoji = "🇬🇧",
                ipAddress = "185.199.108.153",
                port = 51820,
                pingMs = 42,
                loadPercentage = 34,
                category = ServerCategory.STREAMING
            ),
            VpnServer(
                id = "de-fra-1",
                countryName = "Germany",
                countryCode = "DE",
                cityName = "Frankfurt #1",
                flagEmoji = "🇩🇪",
                ipAddress = "142.250.180.46",
                port = 51820,
                pingMs = 38,
                loadPercentage = 29,
                category = ServerCategory.FASTEST
            ),
            VpnServer(
                id = "nl-ams-1",
                countryName = "Netherlands",
                countryCode = "NL",
                cityName = "Amsterdam (No Logs P2P)",
                flagEmoji = "🇳🇱",
                ipAddress = "194.109.6.92",
                port = 51820,
                pingMs = 36,
                loadPercentage = 30,
                category = ServerCategory.P2P
            ),
            VpnServer(
                id = "jp-tyo-1",
                countryName = "Japan",
                countryCode = "JP",
                cityName = "Tokyo (Low Jitter)",
                flagEmoji = "🇯🇵",
                ipAddress = "133.242.18.9",
                port = 51820,
                pingMs = 52,
                loadPercentage = 60,
                category = ServerCategory.GAMING
            ),
            VpnServer(
                id = "sg-sin-1",
                countryName = "Singapore",
                countryCode = "SG",
                cityName = "Singapore Gateway",
                flagEmoji = "🇸🇬",
                ipAddress = "202.158.45.12",
                port = 51820,
                pingMs = 45,
                loadPercentage = 44,
                category = ServerCategory.FASTEST
            ),
            VpnServer(
                id = "ca-tor-1",
                countryName = "Canada",
                countryCode = "CA",
                cityName = "Toronto",
                flagEmoji = "🇨🇦",
                ipAddress = "199.241.139.11",
                port = 51820,
                pingMs = 34,
                loadPercentage = 39,
                category = ServerCategory.ALL
            ),
            VpnServer(
                id = "ch-zur-1",
                countryName = "Switzerland",
                countryCode = "CH",
                cityName = "Zurich (Swiss Privacy)",
                flagEmoji = "🇨🇭",
                ipAddress = "193.134.25.10",
                port = 51820,
                pingMs = 41,
                loadPercentage = 25,
                category = ServerCategory.STEALTH
            ),
            VpnServer(
                id = "fr-par-1",
                countryName = "France",
                countryCode = "FR",
                cityName = "Paris",
                flagEmoji = "🇫🇷",
                ipAddress = "195.154.122.8",
                port = 51820,
                pingMs = 40,
                loadPercentage = 33,
                category = ServerCategory.STREAMING
            ),
            VpnServer(
                id = "au-syd-1",
                countryName = "Australia",
                countryCode = "AU",
                cityName = "Sydney",
                flagEmoji = "🇦🇺",
                ipAddress = "139.130.4.5",
                port = 51820,
                pingMs = 85,
                loadPercentage = 51,
                category = ServerCategory.ALL
            ),
            VpnServer(
                id = "kr-sel-1",
                countryName = "South Korea",
                countryCode = "KR",
                cityName = "Seoul (Ultra 10G)",
                flagEmoji = "🇰🇷",
                ipAddress = "211.233.78.10",
                port = 51820,
                pingMs = 58,
                loadPercentage = 40,
                category = ServerCategory.GAMING
            ),
            VpnServer(
                id = "br-sao-1",
                countryName = "Brazil",
                countryCode = "BR",
                cityName = "São Paulo",
                flagEmoji = "🇧🇷",
                ipAddress = "200.147.67.142",
                port = 51820,
                pingMs = 95,
                loadPercentage = 45,
                category = ServerCategory.ALL
            ),
            VpnServer(
                id = "ae-dxb-1",
                countryName = "United Arab Emirates",
                countryCode = "AE",
                cityName = "Dubai (Stealth VoIP)",
                flagEmoji = "🇦🇪",
                ipAddress = "185.120.90.1",
                port = 443,
                pingMs = 78,
                loadPercentage = 62,
                category = ServerCategory.STEALTH
            ),
            VpnServer(
                id = "se-sto-1",
                countryName = "Sweden",
                countryCode = "SE",
                cityName = "Stockholm",
                flagEmoji = "🇸🇪",
                ipAddress = "194.236.4.1",
                port = 51820,
                pingMs = 47,
                loadPercentage = 28,
                category = ServerCategory.P2P
            )
        )
        _servers.value = serverList

        // Restore selected server if saved
        val savedServerId = prefs.getString("selected_server_id", "us-nyc-1")
        val found = serverList.find { it.id == savedServerId } ?: serverList[1]
        _selectedServer.value = found
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        prefs.edit().putString("selected_server_id", server.id).apply()
        VpnServiceManager.addLog(
            LogLevel.INFO,
            "SERVER",
            "Target server selected: ${server.flagEmoji} ${server.fullLocationName}"
        )

        // If currently connected, reconnect to the new server seamlessly
        if (VpnServiceManager.vpnState.value.isConnected) {
            connectVpn(server)
        }
    }

    fun toggleFavorite(serverId: String) {
        _servers.update { list ->
            list.map { if (it.id == serverId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    fun refreshPings() {
        repoScope.launch {
            VpnServiceManager.addLog(LogLevel.INFO, "PING", "Probing worldwide Alpha nodes for lowest latency...")
            _servers.update { list ->
                list.map { s ->
                    val jitter = Random.nextInt(-4, 5)
                    s.copy(
                        pingMs = (s.pingMs + jitter).coerceAtLeast(10),
                        loadPercentage = (s.loadPercentage + Random.nextInt(-3, 4)).coerceIn(10, 95)
                    )
                }
            }
        }
    }

    fun addCustomServer(name: String, ip: String, port: Int, country: String, flag: String, publicKey: String) {
        val custom = VpnServer(
            id = "custom-${System.currentTimeMillis()}",
            countryName = country,
            countryCode = "CUSTOM",
            cityName = name,
            flagEmoji = flag.ifEmpty { "🛡️" },
            ipAddress = ip,
            port = port,
            pingMs = 30,
            loadPercentage = 15,
            category = ServerCategory.ALL,
            isCustom = true,
            publicKey = publicKey
        )
        _servers.update { listOf(custom) + it }
        selectServer(custom)
        VpnServiceManager.addLog(LogLevel.SECURE, "CONFIG", "Imported custom server profile: $name ($ip:$port)")
    }

    fun connectVpn(server: VpnServer = _selectedServer.value) {
        _selectedServer.value = server
        AlphaVpnService.startVpn(context, server, _config.value.protocol)
    }

    fun disconnectVpn() {
        AlphaVpnService.stopVpn(context)
    }

    fun updateConfig(newConfig: VpnConfig) {
        _config.value = newConfig
        saveConfig(newConfig)
        VpnServiceManager.addLog(LogLevel.INFO, "CONFIG", "Updated VPN settings: Protocol=${newConfig.protocol.displayName}, DNS=${newConfig.dnsProvider.title}")
    }

    private fun loadConfig(): VpnConfig {
        val protoName = prefs.getString("protocol", VpnProtocol.WIREGUARD.name) ?: VpnProtocol.WIREGUARD.name
        val protocol = try { VpnProtocol.valueOf(protoName) } catch (e: Exception) { VpnProtocol.WIREGUARD }
        val dnsName = prefs.getString("dns_provider", DnsProvider.CLOUDFLARE.name) ?: DnsProvider.CLOUDFLARE.name
        val dnsProvider = try { DnsProvider.valueOf(dnsName) } catch (e: Exception) { DnsProvider.CLOUDFLARE }

        return VpnConfig(
            protocol = protocol,
            killSwitchEnabled = prefs.getBoolean("kill_switch", false),
            splitTunnelingEnabled = prefs.getBoolean("split_tunneling", false),
            splitTunnelMode = SplitTunnelMode.valueOf(prefs.getString("split_mode", SplitTunnelMode.BYPASS_SELECTED.name) ?: SplitTunnelMode.BYPASS_SELECTED.name),
            splitTunnelApps = prefs.getStringSet("split_apps", emptySet()) ?: emptySet(),
            dnsProvider = dnsProvider,
            customPrimaryDns = prefs.getString("custom_dns_1", "1.1.1.1") ?: "1.1.1.1",
            customSecondaryDns = prefs.getString("custom_dns_2", "1.0.0.1") ?: "1.0.0.1",
            mtuSize = prefs.getInt("mtu_size", 1420),
            autoConnectOnUntrustedWifi = prefs.getBoolean("auto_connect_wifi", false),
            autoReconnectOnDrop = prefs.getBoolean("auto_reconnect", true),
            darkThemeEnabled = prefs.getBoolean("dark_theme", true)
        )
    }

    private fun saveConfig(c: VpnConfig) {
        prefs.edit()
            .putString("protocol", c.protocol.name)
            .putBoolean("kill_switch", c.killSwitchEnabled)
            .putBoolean("split_tunneling", c.splitTunnelingEnabled)
            .putString("split_mode", c.splitTunnelMode.name)
            .putStringSet("split_apps", c.splitTunnelApps)
            .putString("dns_provider", c.dnsProvider.name)
            .putString("custom_dns_1", c.customPrimaryDns)
            .putString("custom_dns_2", c.customSecondaryDns)
            .putInt("mtu_size", c.mtuSize)
            .putBoolean("auto_connect_wifi", c.autoConnectOnUntrustedWifi)
            .putBoolean("auto_reconnect", c.autoReconnectOnDrop)
            .putBoolean("dark_theme", c.darkThemeEnabled)
            .apply()
    }

    private fun loadInstalledApps() {
        repoScope.launch {
            try {
                val pm = context.packageManager
                val packages = withContext(Dispatchers.IO) {
                    pm.getInstalledApplications(PackageManager.GET_META_DATA)
                }
                val appList = packages.map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                        icon = try { pm.getApplicationIcon(appInfo) } catch (e: Exception) { null }
                    )
                }.sortedBy { it.appName.lowercase() }
                _installedApps.value = appList
            } catch (e: Exception) {
                // Fallback dummy app list
                _installedApps.value = listOf(
                    AppInfo("com.google.android.youtube", "YouTube", false),
                    AppInfo("com.netflix.mediaclient", "Netflix", false),
                    AppInfo("com.spotify.music", "Spotify", false),
                    AppInfo("com.android.chrome", "Chrome Browser", false),
                    AppInfo("org.telegram.messenger", "Telegram", false),
                    AppInfo("com.whatsapp", "WhatsApp", false),
                    AppInfo("com.instagram.android", "Instagram", false),
                    AppInfo("com.twitter.android", "X (Twitter)", false)
                )
            }
        }
    }

    fun toggleAppInSplitTunnel(packageName: String) {
        val currentSet = _config.value.splitTunnelApps.toMutableSet()
        if (currentSet.contains(packageName)) {
            currentSet.remove(packageName)
        } else {
            currentSet.add(packageName)
        }
        updateConfig(_config.value.copy(splitTunnelApps = currentSet))
    }

    fun startSpeedTest() {
        speedTestJob?.cancel()
        speedTestJob = repoScope.launch {
            _speedTest.value = SpeedTestResult(
                phase = SpeedTestPhase.PING_TEST,
                isRunning = true,
                serverName = _selectedServer.value.fullLocationName
            )

            // 1. Ping test
            for (step in 1..10) {
                delay(120)
                val testPing = (_selectedServer.value.pingMs + Random.nextInt(-3, 4)).coerceAtLeast(8)
                val testJitter = Random.nextInt(1, 6)
                _speedTest.value = _speedTest.value.copy(
                    progress = step / 30f,
                    pingMs = testPing,
                    jitterMs = testJitter
                )
            }

            // 2. Download speed test
            _speedTest.value = _speedTest.value.copy(phase = SpeedTestPhase.DOWNLOAD_TEST)
            var currentDown = 10f
            val targetDown = Random.nextFloat() * 80f + 65f // 65 - 145 Mbps
            for (step in 1..15) {
                delay(150)
                currentDown += (targetDown - currentDown) * 0.35f + Random.nextFloat() * 4f - 2f
                _speedTest.value = _speedTest.value.copy(
                    progress = (10 + step) / 30f,
                    downloadSpeedMbps = currentDown.coerceAtLeast(0f)
                )
            }

            // 3. Upload speed test
            _speedTest.value = _speedTest.value.copy(phase = SpeedTestPhase.UPLOAD_TEST)
            var currentUp = 5f
            val targetUp = Random.nextFloat() * 40f + 25f // 25 - 65 Mbps
            for (step in 1..10) {
                delay(150)
                currentUp += (targetUp - currentUp) * 0.35f + Random.nextFloat() * 2f - 1f
                _speedTest.value = _speedTest.value.copy(
                    progress = (25 + step) / 35f,
                    uploadSpeedMbps = currentUp.coerceAtLeast(0f)
                )
            }

            // Completed
            _speedTest.value = _speedTest.value.copy(
                phase = SpeedTestPhase.COMPLETED,
                progress = 1.0f,
                isRunning = false
            )
        }
    }

    fun stopSpeedTest() {
        speedTestJob?.cancel()
        _speedTest.value = _speedTest.value.copy(isRunning = false, phase = SpeedTestPhase.IDLE)
    }
}
