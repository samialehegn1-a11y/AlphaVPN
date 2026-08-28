package com.example.vpn.model

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTING,
    ERROR;

    val isConnected: Boolean
        get() = this == CONNECTED

    val isTransitional: Boolean
        get() = this == CONNECTING || this == RECONNECTING || this == DISCONNECTING
}
