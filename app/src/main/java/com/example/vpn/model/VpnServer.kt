package com.example.vpn.model

enum class ServerCategory(val title: String, val iconEmoji: String) {
    ALL("All Locations", "🌐"),
    FASTEST("Fastest", "⚡"),
    STREAMING("Streaming", "🎬"),
    GAMING("Low Ping Gaming", "🎮"),
    STEALTH("Stealth / Anti-DPI", "🛡️"),
    P2P("P2P / Torrent", "📁")
}

data class VpnServer(
    val id: String,
    val countryName: String,
    val countryCode: String,
    val cityName: String,
    val flagEmoji: String,
    val ipAddress: String,
    val port: Int = 51820,
    val pingMs: Int = 28,
    val loadPercentage: Int = 34,
    val category: ServerCategory = ServerCategory.ALL,
    val supportedProtocols: List<VpnProtocol> = listOf(
        VpnProtocol.WIREGUARD,
        VpnProtocol.OPENVPN_UDP,
        VpnProtocol.OPENVPN_TCP,
        VpnProtocol.ALPHA_STEALTH
    ),
    val isPremium: Boolean = false,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val publicKey: String = "k7W1Gg29QxLzN7...wireguard_pubkey",
    val allowedIps: String = "0.0.0.0/0, ::/0",
    val endpoint: String = "$ipAddress:$port"
) {
    val fullLocationName: String
        get() = "$countryName · $cityName"
}
