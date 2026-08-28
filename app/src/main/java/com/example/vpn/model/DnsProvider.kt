package com.example.vpn.model

enum class DnsProvider(
    val title: String,
    val description: String,
    val primaryDns: String,
    val secondaryDns: String
) {
    CLOUDFLARE(
        title = "Cloudflare 1.1.1.1",
        description = "Ultra-fast privacy-first DNS resolver with zero logs",
        primaryDns = "1.1.1.1",
        secondaryDns = "1.0.0.1"
    ),
    ADGUARD(
        title = "AdGuard DNS (Ad-Block)",
        description = "Blocks popups, tracking telemetry, and malicious domains",
        primaryDns = "94.140.14.14",
        secondaryDns = "94.140.15.15"
    ),
    GOOGLE(
        title = "Google Public DNS",
        description = "High-availability geo-distributed worldwide DNS",
        primaryDns = "8.8.8.8",
        secondaryDns = "8.8.4.4"
    ),
    QUAD9(
        title = "Quad9 DNS",
        description = "Swiss non-profit DNS blocking known malicious phishing & botnets",
        primaryDns = "9.9.9.9",
        secondaryDns = "149.112.112.112"
    ),
    CUSTOM(
        title = "Custom DNS",
        description = "Define your own custom primary and secondary DNS servers",
        primaryDns = "1.1.1.1",
        secondaryDns = "8.8.8.8"
    )
}
