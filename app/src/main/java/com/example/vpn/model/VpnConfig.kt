package com.example.vpn.model

enum class SplitTunnelMode {
    BYPASS_SELECTED, // Exclude selected apps from VPN
    ROUTE_ONLY_SELECTED // Only route selected apps through VPN
}

data class VpnConfig(
    val selectedServerId: String = "us-nyc-1",
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val killSwitchEnabled: Boolean = false,
    val splitTunnelingEnabled: Boolean = false,
    val splitTunnelMode: SplitTunnelMode = SplitTunnelMode.BYPASS_SELECTED,
    val splitTunnelApps: Set<String> = emptySet(),
    val dnsProvider: DnsProvider = DnsProvider.CLOUDFLARE,
    val customPrimaryDns: String = "1.1.1.1",
    val customSecondaryDns: String = "1.0.0.1",
    val mtuSize: Int = 1420,
    val autoConnectOnUntrustedWifi: Boolean = false,
    val autoReconnectOnDrop: Boolean = true,
    val bypassLocalSubnets: Boolean = true,
    val notificationStatsEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = true
)
