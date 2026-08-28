package com.example.vpn.model

data class VpnTweakPayload(
    val id: String,
    val name: String,
    val operatorName: String,
    val protocolType: String,
    val sniHost: String,
    val status: String = "UNLIMITED",
    val isCustom: Boolean = false,
    val payloadString: String = ""
)
