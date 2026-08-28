package com.example.vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianDark
import com.example.vpn.model.VpnState

@Composable
fun PowerConnectDial(
    vpnState: VpnState,
    protocolName: String,
    onPowerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "power_dial_anim")

    // Pulsing radar ripple scale for connecting/connected
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (vpnState == VpnState.CONNECTING) 1.28f else if (vpnState == VpnState.CONNECTED) 1.14f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (vpnState == VpnState.CONNECTING) 900 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Rotating cyber ring angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (vpnState == VpnState.CONNECTING) 2000 else 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    // Animated colors
    val glowColor by animateColorAsState(
        targetValue = when (vpnState) {
            VpnState.CONNECTED -> NeonEmerald
            VpnState.CONNECTING, VpnState.RECONNECTING -> NeonCyan
            VpnState.DISCONNECTING -> CrimsonAlert
            VpnState.ERROR -> CrimsonAlert
            VpnState.DISCONNECTED -> NeonCyan.copy(alpha = 0.5f)
        },
        animationSpec = tween(500),
        label = "glow_color"
    )

    val innerDialBg by animateColorAsState(
        targetValue = when (vpnState) {
            VpnState.CONNECTED -> Color(0xFF07271E)
            VpnState.CONNECTING, VpnState.RECONNECTING -> Color(0xFF042230)
            VpnState.ERROR, VpnState.DISCONNECTING -> Color(0xFF2B0A14)
            VpnState.DISCONNECTED -> ObsidianCardElevated
        },
        animationSpec = tween(500),
        label = "dial_bg"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Radar aura ripples (canvas)
            if (vpnState == VpnState.CONNECTED || vpnState == VpnState.CONNECTING) {
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale)
                ) {
                    drawCircle(
                        color = glowColor.copy(alpha = if (vpnState == VpnState.CONNECTING) 0.18f else 0.08f),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        color = glowColor.copy(alpha = if (vpnState == VpnState.CONNECTING) 0.35f else 0.15f),
                        radius = size.minDimension / 2.3f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Cyber orbiting dashed ring
            Canvas(
                modifier = Modifier
                    .size(210.dp)
                    .rotate(rotationAngle)
            ) {
                val strokeWidth = 3.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val brush = Brush.sweepGradient(
                    0.0f to glowColor,
                    0.4f to ElectricViolet,
                    0.8f to glowColor.copy(alpha = 0.1f),
                    1.0f to glowColor
                )
                drawCircle(
                    brush = brush,
                    radius = radius,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Main Interactive Power Button (Minimum 48dp target compliant)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(innerDialBg)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                glowColor,
                                glowColor.copy(alpha = 0.3f),
                                ElectricViolet.copy(alpha = 0.5f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = glowColor),
                        onClick = onPowerClick
                    )
                    .testTag("vpn_power_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (vpnState) {
                            VpnState.CONNECTED -> Icons.Default.Shield
                            VpnState.CONNECTING, VpnState.RECONNECTING -> Icons.Default.Security
                            VpnState.ERROR -> Icons.Default.Lock
                            else -> Icons.Default.PowerSettingsNew
                        },
                        contentDescription = "VPN Power Toggle",
                        tint = glowColor,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when (vpnState) {
                            VpnState.CONNECTED -> "CONNECTED"
                            VpnState.CONNECTING -> "SECURING..."
                            VpnState.RECONNECTING -> "RECONNECTING"
                            VpnState.DISCONNECTING -> "STOPPING"
                            VpnState.ERROR -> "RETRY"
                            VpnState.DISCONNECTED -> "CONNECT"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.8.sp,
                            fontSize = 12.sp
                        ),
                        color = glowColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Badge Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(ObsidianDark)
                .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(glowColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when (vpnState) {
                        VpnState.CONNECTED -> "SECURE & PROTECTED ($protocolName)"
                        VpnState.CONNECTING -> "Negotiating Keys & DNS Route..."
                        VpnState.RECONNECTING -> "Restoring Tunnel..."
                        VpnState.DISCONNECTING -> "Closing Interface..."
                        VpnState.ERROR -> "Connection Failed · Tap to Retry"
                        VpnState.DISCONNECTED -> "Traffic Unprotected · Tap to Secure"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
