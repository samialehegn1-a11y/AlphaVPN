package com.example.vpn.model

enum class SpeedTestPhase {
    IDLE,
    PING_TEST,
    DOWNLOAD_TEST,
    UPLOAD_TEST,
    COMPLETED
}

data class SpeedTestResult(
    val phase: SpeedTestPhase = SpeedTestPhase.IDLE,
    val progress: Float = 0f, // 0.0 to 1.0
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val downloadSpeedMbps: Float = 0f,
    val uploadSpeedMbps: Float = 0f,
    val serverName: String = "Alpha Fast CDN · New York",
    val isRunning: Boolean = false
)
