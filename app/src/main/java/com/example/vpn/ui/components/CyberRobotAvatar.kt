package com.example.vpn.ui.components

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDark
import com.example.vpn.model.VpnState

@Composable
fun CyberRobotAvatar(
    vpnState: VpnState,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "robot_animation")

    // Pulsing aura animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Glow alpha animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Scanning eye movement
    val eyeScanX by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_scan"
    )

    // Antenna energy blink
    val antennaBlink by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "antenna_blink"
    )

    val primaryNeon = when (vpnState) {
        VpnState.CONNECTED -> NeonEmerald
        VpnState.CONNECTING, VpnState.RECONNECTING -> AmberAlert
        else -> NeonCyan
    }

    val secondaryNeon = when (vpnState) {
        VpnState.CONNECTED -> NeonCyan
        VpnState.CONNECTING, VpnState.RECONNECTING -> ElectricViolet
        else -> ElectricViolet
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryNeon.copy(alpha = 0.25f * glowAlpha),
                        ObsidianCard,
                        ObsidianDark
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        primaryNeon.copy(alpha = glowAlpha),
                        secondaryNeon.copy(alpha = 0.3f),
                        primaryNeon.copy(alpha = glowAlpha)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.85f)) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f

            // Outer cybernetic target ring
            drawCircle(
                color = primaryNeon.copy(alpha = 0.3f * glowAlpha),
                radius = canvasW * 0.46f * pulseScale.coerceAtMost(1.08f),
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Robot Antenna Mast
            val antennaTop = Offset(centerX, centerY - canvasH * 0.38f)
            val antennaBase = Offset(centerX, centerY - canvasH * 0.22f)
            drawLine(
                color = primaryNeon.copy(alpha = 0.9f),
                start = antennaBase,
                end = antennaTop,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Antenna Bulb with glowing pulse
            drawCircle(
                color = primaryNeon.copy(alpha = antennaBlink),
                radius = 4.5.dp.toPx(),
                center = antennaTop
            )
            drawCircle(
                color = primaryNeon.copy(alpha = 0.35f * antennaBlink),
                radius = 8.dp.toPx(),
                center = antennaTop
            )

            // Left & Right Ear Pods
            val earWidth = canvasW * 0.08f
            val earHeight = canvasH * 0.20f
            drawRoundRect(
                color = ObsidianBorder,
                topLeft = Offset(centerX - canvasW * 0.36f, centerY - earHeight / 2f),
                size = Size(earWidth, earHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawRoundRect(
                color = primaryNeon.copy(alpha = 0.8f),
                topLeft = Offset(centerX - canvasW * 0.36f, centerY - earHeight / 2f),
                size = Size(earWidth, earHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            drawRoundRect(
                color = ObsidianBorder,
                topLeft = Offset(centerX + canvasW * 0.28f, centerY - earHeight / 2f),
                size = Size(earWidth, earHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            drawRoundRect(
                color = primaryNeon.copy(alpha = 0.8f),
                topLeft = Offset(centerX + canvasW * 0.28f, centerY - earHeight / 2f),
                size = Size(earWidth, earHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Robot Head / Helmet Base
            val headW = canvasW * 0.58f
            val headH = canvasH * 0.46f
            val headTopLeft = Offset(centerX - headW / 2f, centerY - headH * 0.42f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                ),
                topLeft = headTopLeft,
                size = Size(headW, headH),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )

            // Head Neon Outline
            drawRoundRect(
                color = primaryNeon.copy(alpha = 0.85f),
                topLeft = headTopLeft,
                size = Size(headW, headH),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Visor Glass (Dark Gloss Screen)
            val visorW = headW * 0.82f
            val visorH = headH * 0.38f
            val visorTopLeft = Offset(centerX - visorW / 2f, centerY - visorH * 0.5f)

            drawRoundRect(
                color = Color(0xFF030712),
                topLeft = visorTopLeft,
                size = Size(visorW, visorH),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            drawRoundRect(
                color = primaryNeon.copy(alpha = 0.5f),
                topLeft = visorTopLeft,
                size = Size(visorW, visorH),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Visor Glowing Cyber Eyes / Cyber Visor Line
            val eyeCenterY = centerY - visorH * 0.05f
            val eyeOffset = if (vpnState == VpnState.CONNECTING) eyeScanX else 0f

            // Left Cyber Eye
            drawCircle(
                color = primaryNeon,
                radius = 3.5.dp.toPx(),
                center = Offset(centerX - visorW * 0.24f + eyeOffset, eyeCenterY)
            )
            drawCircle(
                color = primaryNeon.copy(alpha = 0.4f * glowAlpha),
                radius = 7.dp.toPx(),
                center = Offset(centerX - visorW * 0.24f + eyeOffset, eyeCenterY)
            )

            // Right Cyber Eye
            drawCircle(
                color = primaryNeon,
                radius = 3.5.dp.toPx(),
                center = Offset(centerX + visorW * 0.24f + eyeOffset, eyeCenterY)
            )
            drawCircle(
                color = primaryNeon.copy(alpha = 0.4f * glowAlpha),
                radius = 7.dp.toPx(),
                center = Offset(centerX + visorW * 0.24f + eyeOffset, eyeCenterY)
            )

            // Connection Link Bar between eyes (High Tech Hud)
            drawLine(
                color = primaryNeon.copy(alpha = 0.6f),
                start = Offset(centerX - visorW * 0.12f + eyeOffset, eyeCenterY),
                end = Offset(centerX + visorW * 0.12f + eyeOffset, eyeCenterY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Mouth Speaker / Vent Grille
            val mouthY = centerY + headH * 0.26f
            for (i in -2..2) {
                val slitX = centerX + i * 5.dp.toPx()
                drawLine(
                    color = primaryNeon.copy(alpha = 0.7f),
                    start = Offset(slitX, mouthY - 2.5.dp.toPx()),
                    end = Offset(slitX, mouthY + 2.5.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Cybernetic Collar / Neck
            val neckPath = Path().apply {
                moveTo(centerX - headW * 0.35f, centerY + headH * 0.58f)
                lineTo(centerX + headW * 0.35f, centerY + headH * 0.58f)
                lineTo(centerX + headW * 0.45f, centerY + headH * 0.82f)
                lineTo(centerX - headW * 0.45f, centerY + headH * 0.82f)
                close()
            }
            drawPath(
                path = neckPath,
                color = ObsidianCard
            )
            drawPath(
                path = neckPath,
                color = primaryNeon.copy(alpha = 0.4f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
