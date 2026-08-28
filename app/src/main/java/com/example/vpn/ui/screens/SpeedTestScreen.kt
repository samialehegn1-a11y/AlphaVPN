package com.example.vpn.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.vpn.model.SpeedTestPhase
import com.example.vpn.model.SpeedTestResult
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedTestScreen(
    speedTest: SpeedTestResult,
    onStartTest: () -> Unit,
    onStopTest: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val displaySpeed = when (speedTest.phase) {
        SpeedTestPhase.UPLOAD_TEST -> speedTest.uploadSpeedMbps
        else -> speedTest.downloadSpeedMbps
    }

    val animatedSpeed by animateFloatAsState(
        targetValue = displaySpeed,
        animationSpec = tween(300),
        label = "gauge_speed"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObsidianCard)
                                .border(1.dp, ObsidianBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = "Speed & Latency Test",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Realtime network throughput diagnostics",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondaryDark
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianCard)
                        .border(1.dp, ObsidianBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Speedometer Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                SpeedometerGauge(speedMbps = animatedSpeed)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Text(
                        text = when (speedTest.phase) {
                            SpeedTestPhase.IDLE -> "READY"
                            SpeedTestPhase.PING_TEST -> "PROBING PING"
                            SpeedTestPhase.DOWNLOAD_TEST -> "TESTING DOWNLOAD"
                            SpeedTestPhase.UPLOAD_TEST -> "TESTING UPLOAD"
                            SpeedTestPhase.COMPLETED -> "TEST COMPLETED"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontSize = 10.sp
                        ),
                        color = when (speedTest.phase) {
                            SpeedTestPhase.DOWNLOAD_TEST -> NeonCyan
                            SpeedTestPhase.UPLOAD_TEST -> NeonEmerald
                            SpeedTestPhase.COMPLETED -> NeonEmerald
                            else -> TextSecondaryDark
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format(Locale.US, "%.1f", animatedSpeed),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 42.sp
                        ),
                        color = Color.White
                    )

                    Text(
                        text = "Mbps",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Test target server pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Server",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = speedTest.serverName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = TextPrimaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4 Metrics Grid (Ping, Jitter, Download, Upload)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedGridItem(
                    title = "PING",
                    value = "${speedTest.pingMs} ms",
                    icon = Icons.Default.Timer,
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )

                SpeedGridItem(
                    title = "JITTER",
                    value = "${speedTest.jitterMs} ms",
                    icon = Icons.Default.Speed,
                    accentColor = ElectricViolet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedGridItem(
                    title = "DOWNLOAD",
                    value = String.format(Locale.US, "%.1f Mbps", speedTest.downloadSpeedMbps),
                    icon = Icons.Default.ArrowDownward,
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )

                SpeedGridItem(
                    title = "UPLOAD",
                    value = String.format(Locale.US, "%.1f Mbps", speedTest.uploadSpeedMbps),
                    icon = Icons.Default.ArrowUpward,
                    accentColor = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Button
            Button(
                onClick = {
                    if (speedTest.isRunning) onStopTest() else onStartTest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("speed_test_action_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (speedTest.isRunning) AmberAlert else NeonCyan,
                    contentColor = ObsidianDark
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (speedTest.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Test Action",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (speedTest.isRunning) "Stop Speed Test" else "Start Speed Test",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SpeedometerGauge(
    speedMbps: Float,
    maxSpeed: Float = 200f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val sweepAngle = 240f
        val startAngle = 150f
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth * 2) / 2f
        val center = Offset(size.width / 2f, size.height / 2f + 10.dp.toPx())

        // Background Track Arc
        drawArc(
            color = Color(0xFF162035),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Active Speed Arc Gradient
        val progressFraction = (speedMbps / maxSpeed).coerceIn(0f, 1f)
        val activeSweep = sweepAngle * progressFraction

        if (activeSweep > 0) {
            val brush = Brush.sweepGradient(
                0.0f to NeonCyan,
                0.5f to ElectricViolet,
                1.0f to NeonEmerald
            )
            drawArc(
                brush = brush,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Ticks
        val tickCount = 10
        for (i in 0..tickCount) {
            val fraction = i.toFloat() / tickCount
            val angle = (startAngle + fraction * sweepAngle) * (PI / 180f)
            val outerRadius = radius - strokeWidth / 2 - 4.dp.toPx()
            val innerRadius = outerRadius - 8.dp.toPx()

            val startX = (center.x + innerRadius * cos(angle)).toFloat()
            val startY = (center.y + innerRadius * sin(angle)).toFloat()
            val endX = (center.x + outerRadius * cos(angle)).toFloat()
            val endY = (center.y + outerRadius * sin(angle)).toFloat()

            drawLine(
                color = if (fraction <= progressFraction) NeonCyan else Color(0xFF2C3E60),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun SpeedGridItem(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondaryDark
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
        }
    }
}
