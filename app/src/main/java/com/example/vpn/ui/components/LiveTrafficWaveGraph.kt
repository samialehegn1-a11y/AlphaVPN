package com.example.vpn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianDark
import com.example.vpn.model.VpnSessionStats
import com.example.vpn.model.VpnState
import kotlin.math.sin

@Composable
fun LiveTrafficWaveGraph(
    vpnState: VpnState,
    sessionStats: VpnSessionStats,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "traffic_wave_transition")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (vpnState.isConnected) 2500 else 6000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val wavePhaseSecondary by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -(2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (vpnState.isConnected) 3200 else 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_secondary"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        ObsidianCardElevated.copy(alpha = 0.6f),
                        ObsidianDark
                    )
                )
            )
            .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp))
            .testTag("live_traffic_wave_graph"),
        contentAlignment = Alignment.Center
    ) {
        // Background animated continuous sine waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height * 0.55f

            val amplitude1 = if (vpnState.isConnected) height * 0.22f else height * 0.08f
            val amplitude2 = if (vpnState.isConnected) height * 0.16f else height * 0.05f

            // Wave 1: Neon Emerald (Download/Throughput)
            val path1 = Path()
            path1.moveTo(0f, midY)
            val steps = 60
            for (i in 0..steps) {
                val x = (width / steps) * i
                val angle = (x / width) * 4 * Math.PI + wavePhase
                val y = midY + (sin(angle) * amplitude1).toFloat()
                path1.lineTo(x, y)
            }

            drawPath(
                path = path1,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonEmerald.copy(alpha = 0.1f),
                        NeonEmerald.copy(alpha = 0.85f),
                        NeonCyan.copy(alpha = 0.9f),
                        NeonEmerald.copy(alpha = 0.2f)
                    )
                ),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Wave 2: Neon Cyan / Electric Violet (Upload)
            val path2 = Path()
            path2.moveTo(0f, midY)
            for (i in 0..steps) {
                val x = (width / steps) * i
                val angle = (x / width) * 3 * Math.PI + wavePhaseSecondary
                val y = midY + (sin(angle + 1.2) * amplitude2).toFloat()
                path2.lineTo(x, y)
            }

            drawPath(
                path = path2,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.05f),
                        NeonCyan.copy(alpha = 0.6f),
                        ElectricViolet.copy(alpha = 0.7f),
                        NeonCyan.copy(alpha = 0.1f)
                    )
                ),
                style = Stroke(width = 1.8.dp.toPx())
            )
        }

        // Central Shield / Cyber Logo floating over the wave
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ObsidianCardElevated,
                                ObsidianDark
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        if (vpnState.isConnected) NeonEmerald else NeonCyan.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Alpha VPN Central Shield",
                    tint = if (vpnState.isConnected) NeonEmerald else NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Realtime Upload / Download Speeds HUD over the wave
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianDark.copy(alpha = 0.85f))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Upload",
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sessionStats.formattedUploadSpeed,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .width(1.dp)
                        .background(ObsidianBorder)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Download",
                        tint = NeonEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sessionStats.formattedDownloadSpeed,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
