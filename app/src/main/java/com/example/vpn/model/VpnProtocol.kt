package com.example.vpn.model

enum class VpnProtocol(
    val displayName: String,
    val description: String,
    val defaultPort: Int,
    val isUdp: Boolean,
    val cipherDescription: String
) {
    WIREGUARD(
        displayName = "WireGuard®",
        description = "Next-gen crypto, lowest latency & instant handshake (ChaCha20-Poly1305)",
        defaultPort = 51820,
        isUdp = true,
        cipherDescription = "ChaCha20-Poly1305 (256-bit)"
    ),
    OPENVPN_UDP(
        displayName = "OpenVPN UDP",
        description = "High-throughput OpenSSL cryptographic tunnel (AES-256-GCM)",
        defaultPort = 1194,
        isUdp = true,
        cipherDescription = "AES-256-GCM"
    ),
    OPENVPN_TCP(
        displayName = "OpenVPN TCP",
        description = "Reliable packet delivery, bypasses strict school/office firewalls",
        defaultPort = 443,
        isUdp = false,
        cipherDescription = "AES-256-CBC / HMAC-SHA512"
    ),
    ALPHA_STEALTH(
        displayName = "AlphaStealth TLS",
        description = "Anti-censorship obfuscation disguised as HTTPS traffic (TLS 1.3)",
        defaultPort = 443,
        isUdp = false,
        cipherDescription = "Shadowsocks-AEAD / TLS Obfs"
    )
}
