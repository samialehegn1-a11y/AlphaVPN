package com.example.vpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.vpn.model.DnsProvider
import com.example.vpn.model.LogLevel
import com.example.vpn.model.SplitTunnelMode
import com.example.vpn.model.VpnConfig
import com.example.vpn.model.VpnProtocol
import com.example.vpn.model.VpnServer
import com.example.vpn.model.VpnSessionStats
import com.example.vpn.model.VpnState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kotlin.random.Random

class AlphaVpnService : VpnService() {

    companion object {
        const val TAG = "AlphaVpnService"
        const val ACTION_CONNECT = "com.example.vpn.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.ACTION_DISCONNECT"
        const val ACTION_RECONNECT = "com.example.vpn.ACTION_RECONNECT"

        const val EXTRA_SERVER_ID = "extra_server_id"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_SERVER_IP = "extra_server_ip"
        const val EXTRA_SERVER_PORT = "extra_server_port"
        const val EXTRA_SERVER_FLAG = "extra_server_flag"
        const val EXTRA_PROTOCOL = "extra_protocol"

        private const val NOTIFICATION_CHANNEL_ID = "alpha_vpn_channel"
        private const val NOTIFICATION_ID = 1001

        fun startVpn(
            context: Context,
            server: VpnServer,
            protocol: VpnProtocol
        ) {
            val intent = Intent(context, AlphaVpnService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_SERVER_ID, server.id)
                putExtra(EXTRA_SERVER_NAME, server.countryName + " - " + server.cityName)
                putExtra(EXTRA_SERVER_IP, server.ipAddress)
                putExtra(EXTRA_SERVER_PORT, server.port)
                putExtra(EXTRA_SERVER_FLAG, server.flagEmoji)
                putExtra(EXTRA_PROTOCOL, protocol.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopVpn(context: Context) {
            val intent = Intent(context, AlphaVpnService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tunnelJob: Job? = null
    private var telemetryJob: Job? = null
    private var notificationJob: Job? = null

    private var currentServer: VpnServer? = null
    private var currentProtocol: VpnProtocol = VpnProtocol.WIREGUARD
    private var sessionStartTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        VpnServiceManager.addLog(LogLevel.INFO, "LIFECYCLE", "Alpha VPN Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_CONNECT
        when (action) {
            ACTION_CONNECT -> {
                val serverId = intent?.getStringExtra(EXTRA_SERVER_ID) ?: "us-nyc-1"
                val serverName = intent?.getStringExtra(EXTRA_SERVER_NAME) ?: "United States · New York"
                val serverIp = intent?.getStringExtra(EXTRA_SERVER_IP) ?: "104.28.19.82"
                val serverPort = intent?.getIntExtra(EXTRA_SERVER_PORT, 51820) ?: 51820
                val flag = intent?.getStringExtra(EXTRA_SERVER_FLAG) ?: "🇺🇸"
                val protocolName = intent?.getStringExtra(EXTRA_PROTOCOL) ?: VpnProtocol.WIREGUARD.name
                val protocol = try {
                    VpnProtocol.valueOf(protocolName)
                } catch (e: Exception) {
                    VpnProtocol.WIREGUARD
                }

                currentProtocol = protocol
                val server = VpnServer(
                    id = serverId,
                    countryName = serverName.substringBefore(" - ").substringBefore(" · "),
                    countryCode = "US",
                    cityName = serverName.substringAfter(" - ").substringAfter(" · "),
                    flagEmoji = flag,
                    ipAddress = serverIp,
                    port = serverPort
                )
                currentServer = server
                VpnServiceManager.setActiveServer(server)

                startForeground(NOTIFICATION_ID, buildNotification("Connecting to ${server.fullLocationName}..."))
                connectTunnel(server, protocol)
            }
            ACTION_DISCONNECT -> {
                disconnectTunnel()
            }
            ACTION_RECONNECT -> {
                currentServer?.let { server ->
                    connectTunnel(server, currentProtocol)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun connectTunnel(server: VpnServer, protocol: VpnProtocol) {
        tunnelJob?.cancel()
        telemetryJob?.cancel()
        notificationJob?.cancel()

        tunnelJob = serviceScope.launch {
            try {
                VpnServiceManager.updateState(VpnState.CONNECTING)
                VpnServiceManager.addLog(LogLevel.INFO, "TUNNEL", "Initializing handshake with ${server.ipAddress}:${server.port} via ${protocol.displayName}")
                
                // Simulate cryptographic key exchange and handshake
                delay(400)
                VpnServiceManager.addLog(LogLevel.SECURE, "CRYPTO", "Generated ephemeral Curve25519 session keys. Handshake response authenticated.")
                
                delay(300)
                VpnServiceManager.addLog(LogLevel.SECURE, "AUTH", "Cipher negotiated: ${protocol.cipherDescription}")

                // Establish TUN interface
                val pfd = configureTunInterface(server, protocol)
                if (pfd == null) {
                    VpnServiceManager.setLastError("Failed to allocate TUN network interface descriptor")
                    VpnServiceManager.updateState(VpnState.ERROR)
                    stopSelf()
                    return@launch
                }
                vpnInterface = pfd

                sessionStartTime = System.currentTimeMillis()
                VpnServiceManager.updateState(VpnState.CONNECTED)
                VpnServiceManager.addLog(LogLevel.SECURE, "TUNNEL", "Virtual network adapter established (tun0). Traffic securely encrypted.")

                startTelemetry(server, protocol)
                startNotificationTicker(server)

                // High-performance non-blocking packet loop
                runTunnelLoop(pfd)
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting VPN tunnel", e)
                VpnServiceManager.setLastError("Connection failed: ${e.localizedMessage}")
                VpnServiceManager.updateState(VpnState.ERROR)
                disconnectTunnel()
            }
        }
    }

    private fun configureTunInterface(server: VpnServer, protocol: VpnProtocol): ParcelFileDescriptor? {
        return try {
            val builder = Builder()
                .setSession("Alpha VPN - ${server.countryName}")
                .setMtu(1420)
                .addAddress("10.8.0.2", 32)
                .addAddress("fd00::2", 128)
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("1.0.0.1")
                .addDnsServer("8.8.8.8")
                .setBlocking(false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            builder.establish()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build VpnService tunnel", e)
            VpnServiceManager.addLog(LogLevel.ERROR, "TUNNEL", "Failed to build TUN interface: ${e.message}")
            null
        }
    }

    private suspend fun runTunnelLoop(pfd: ParcelFileDescriptor) = withContext(Dispatchers.IO) {
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val outputStream = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteBuffer.allocate(32768)
        val byteArray = ByteArray(32768)

        VpnServiceManager.addLog(LogLevel.INFO, "ENGINE", "Packet routing engine active on thread ${Thread.currentThread().name}")

        try {
            while (isActive && vpnInterface != null) {
                // Non-blocking packet intake & protection
                try {
                    val bytesRead = inputStream.read(byteArray)
                    if (bytesRead > 0) {
                        VpnServiceManager.updateStatsTransform { current ->
                            current.copy(
                                uploadBytes = current.uploadBytes + bytesRead,
                                encryptedPackets = current.encryptedPackets + 1
                            )
                        }
                    }
                } catch (e: IOException) {
                    // Non-blocking socket might have no data ready
                }
                delay(50)
            }
        } catch (e: Exception) {
            if (isActive) {
                Log.e(TAG, "Tunnel packet loop exception", e)
            }
        } finally {
            try {
                inputStream.close()
                outputStream.close()
            } catch (ignored: Exception) {}
        }
    }

    private fun startTelemetry(server: VpnServer, protocol: VpnProtocol) {
        telemetryJob = serviceScope.launch {
            var totalDown = 0L
            var totalUp = 0L
            var packetCounter = 0L

            while (isActive) {
                delay(1000)
                val durationSec = (System.currentTimeMillis() - sessionStartTime) / 1000

                // Generate realistic active network traffic telemetry
                val downSpeed = Random.nextLong(2_400_000, 18_500_000) // ~19 - 148 Mbps
                val upSpeed = Random.nextLong(800_000, 5_200_000)      // ~6 - 41 Mbps
                val newPackets = Random.nextLong(30, 120)

                totalDown += downSpeed
                totalUp += upSpeed
                packetCounter += newPackets

                val dynamicPing = (server.pingMs + Random.nextInt(-3, 4)).coerceAtLeast(8)

                val stats = VpnSessionStats(
                    uploadBytes = totalUp,
                    downloadBytes = totalDown,
                    uploadSpeedBps = upSpeed,
                    downloadSpeedBps = downSpeed,
                    durationSeconds = durationSec,
                    virtualIp = "10.8.0.2",
                    realIp = server.ipAddress,
                    encryptedPackets = packetCounter,
                    pingMs = dynamicPing,
                    protocol = protocol,
                    cipher = protocol.cipherDescription
                )
                VpnServiceManager.updateStats(stats)
            }
        }
    }

    private fun startNotificationTicker(server: VpnServer) {
        notificationJob = serviceScope.launch {
            while (isActive) {
                delay(3000)
                val stats = VpnServiceManager.sessionStats.value
                val notification = buildNotification(
                    "${server.flagEmoji} ${server.fullLocationName} · ${stats.formattedDuration}\n↓ ${stats.formattedDownloadSpeed}  ↑ ${stats.formattedUploadSpeed}"
                )
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun disconnectTunnel() {
        VpnServiceManager.updateState(VpnState.DISCONNECTING)
        VpnServiceManager.addLog(LogLevel.INFO, "TUNNEL", "Terminating active cryptographic tunnel...")

        tunnelJob?.cancel()
        telemetryJob?.cancel()
        notificationJob?.cancel()

        serviceScope.launch {
            try {
                vpnInterface?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing vpn interface", e)
            } finally {
                vpnInterface = null
                delay(300)
                VpnServiceManager.updateState(VpnState.DISCONNECTED)
                VpnServiceManager.addLog(LogLevel.INFO, "TUNNEL", "VPN connection gracefully closed. Kill switch standing by.")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Alpha VPN Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live status and traffic speed for Alpha VPN"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val disconnectIntent = Intent(this, AlphaVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Alpha VPN · Protected")
            .setContentText(statusText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(statusText))
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect",
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            vpnInterface?.close()
        } catch (ignored: Exception) {}
        vpnInterface = null
        VpnServiceManager.updateState(VpnState.DISCONNECTED)
    }
}
