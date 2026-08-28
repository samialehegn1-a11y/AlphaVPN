package com.example.vpn.model

import java.util.Locale

data class VpnSessionStats(
    val uploadBytes: Long = 0L,
    val downloadBytes: Long = 0L,
    val uploadSpeedBps: Long = 0L,
    val downloadSpeedBps: Long = 0L,
    val durationSeconds: Long = 0L,
    val virtualIp: String = "10.8.0.2",
    val realIp: String = "192.0.2.45",
    val encryptedPackets: Long = 0L,
    val pingMs: Int = 24,
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val cipher: String = "ChaCha20-Poly1305 (256-bit)"
) {
    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

    val formattedDownloadSpeed: String
        get() = formatSpeed(downloadSpeedBps)

    val formattedUploadSpeed: String
        get() = formatSpeed(uploadSpeedBps)

    val formattedTotalDownload: String
        get() = formatBytes(downloadBytes)

    val formattedTotalUpload: String
        get() = formatBytes(uploadBytes)

    companion object {
        fun formatSpeed(bytesPerSec: Long): String {
            val bitsPerSec = bytesPerSec * 8
            return when {
                bitsPerSec >= 1_000_000_000 -> String.format(Locale.US, "%.1f Gbps", bitsPerSec / 1_000_000_000.0)
                bitsPerSec >= 1_000_000 -> String.format(Locale.US, "%.1f Mbps", bitsPerSec / 1_000_000.0)
                bitsPerSec >= 1_000 -> String.format(Locale.US, "%.1f Kbps", bitsPerSec / 1_000.0)
                else -> "$bitsPerSec bps"
            }
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024L * 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f TB", bytes / (1024.0 * 1024 * 1024 * 1024))
                bytes >= 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024))
                bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
                bytes >= 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }
}
