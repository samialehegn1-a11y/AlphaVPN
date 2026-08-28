package com.example.vpn.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldGlow
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.vpn.model.VpnConfig
import com.example.vpn.model.VpnServer
import com.example.vpn.model.VpnSessionStats
import com.example.vpn.model.VpnState
import com.example.vpn.model.VpnTweakPayload
import com.example.vpn.ui.components.AddTimeDialog
import com.example.vpn.ui.components.ConfigImportDialog
import com.example.vpn.ui.components.LiveTrafficWaveGraph
import com.example.vpn.ui.components.PayloadSelectorDialog
import com.example.vpn.ui.components.RewardAdDialog

@Composable
fun ConnectScreen(
    vpnState: VpnState,
    sessionStats: VpnSessionStats,
    selectedServer: VpnServer,
    selectedPayload: VpnTweakPayload,
    payloadsList: List<VpnTweakPayload>,
    timeLeftSeconds: Long,
    config: VpnConfig,
    useConfigEnabled: Boolean,
    onToggleUseConfig: (Boolean) -> Unit,
    onPowerClick: () -> Unit,
    onServerCardClick: () -> Unit,
    onSelectPayload: (VpnTweakPayload) -> Unit,
    onCreateCustomPayload: (name: String, operator: String, sni: String, protocol: String) -> Unit,
    onAddTime: (seconds: Long) -> Unit,
    onImportConfig: (name: String, content: String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenSpeedTest: () -> Unit,
    onOpenSplitTunnel: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddTimeDialog by remember { mutableStateOf(false) }
    var showRewardAdDialog by remember { mutableStateOf(false) }
    var showPayloadDialog by remember { mutableStateOf(false) }

    if (showImportDialog) {
        ConfigImportDialog(
            onDismiss = { showImportDialog = false },
            onImportConfigContent = { name, content ->
                onImportConfig(name, content)
                showImportDialog = false
            }
        )
    }

    if (showRewardAdDialog) {
        RewardAdDialog(
            onRewardGranted = {
                onAddTime(7200L) // +2 hours (7200 seconds)
            },
            onDismiss = { showRewardAdDialog = false }
        )
    }

    if (showAddTimeDialog) {
        AddTimeDialog(
            onDismiss = { showAddTimeDialog = false },
            onAddTime = { seconds ->
                onAddTime(seconds)
                showAddTimeDialog = false
            }
        )
    }

    if (showPayloadDialog) {
        PayloadSelectorDialog(
            currentPayload = selectedPayload,
            payloadList = payloadsList,
            onSelectPayload = { payload ->
                onSelectPayload(payload)
                showPayloadDialog = false
            },
            onCreateCustomPayload = { name, operator, sni, protocol ->
                onCreateCustomPayload(name, operator, sni, protocol)
                showPayloadDialog = false
            },
            onDismiss = { showPayloadDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP BAR & HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Drawer Hamburger Icon
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ObsidianCard)
                        .border(1.dp, ObsidianBorder, CircleShape)
                        .testTag("drawer_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Drawer",
                        tint = NeonEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Center: App Title & Sub-text (Active IP / Host)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Alpha VPN",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                    }

                    Text(
                        text = if (vpnState.isConnected)
                            "IP: 196.188.241.10 • Host: ${selectedPayload.sniHost} [Secured]"
                        else
                            "IP: 10.150.84.192 • Host: ${selectedPayload.sniHost} [Direct]",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp
                        ),
                        color = if (vpnState.isConnected) NeonEmerald else TextSecondaryDark,
                        maxLines = 1
                    )
                }

                // Right: Import Config & Options Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ObsidianCard)
                            .border(1.dp, ObsidianBorder, CircleShape)
                            .testTag("import_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Import Config",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ObsidianCard)
                                .border(1.dp, ObsidianBorder, CircleShape)
                                .testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(ObsidianCardElevated)
                                .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Server Nodes", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onOpenServers()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Speed Diagnostics", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onOpenSpeedTest()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Split Tunneling", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onOpenSplitTunnel()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Diagnostic Logs", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onOpenLogs()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Security Settings", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. REAL-TIME LIVE TRAFFIC WAVE GRAPH
            LiveTrafficWaveGraph(
                vpnState = vpnState,
                sessionStats = sessionStats,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. STATUS DATA ROW
            StatusDataRow(
                vpnState = vpnState,
                durationFormatted = sessionStats.formattedDuration
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. LOCATION SELECTION CARD (Pill style with green border)
            LocationSelectionCard(
                server = selectedServer,
                onClick = onServerCardClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. NETWORK SELECTION CARD (Pill style labeled Network with 'Use Config' switch)
            NetworkSelectionCard(
                payload = selectedPayload,
                useConfig = useConfigEnabled,
                onToggleUseConfig = onToggleUseConfig,
                onClick = { showPayloadDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6. TIMER & REWARD AD SECTION
            TimerRewardSection(
                timeLeftSeconds = timeLeftSeconds,
                onAdd2Hours = { showRewardAdDialog = true },
                onCustomTime = { showAddTimeDialog = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 7. LARGE CIRCULAR NEON GREEN CONNECT BUTTON
            LargeNeonConnectButton(
                vpnState = vpnState,
                onClick = onPowerClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusDataRow(
    vpnState: VpnState,
    durationFormatted: String
) {
    val statusColor = when (vpnState) {
        VpnState.CONNECTED -> NeonEmerald
        VpnState.CONNECTING, VpnState.RECONNECTING -> AmberAlert
        else -> TextTertiaryDark
    }

    val statusText = when (vpnState) {
        VpnState.CONNECTED -> "Connected"
        VpnState.CONNECTING -> "Connecting..."
        VpnState.RECONNECTING -> "Reconnecting..."
        VpnState.DISCONNECTING -> "Disconnecting..."
        VpnState.ERROR -> "Error"
        else -> "Disconnected"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status State
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = statusColor
            )
        }

        // Config Version
        Text(
            text = "Config: v1.4.2",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = NeonCyan
        )

        // VPN Elapsed Duration
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = durationFormatted,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun LocationSelectionCard(
    server: VpnServer,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(ObsidianCard)
            .border(1.2.dp, NeonEmerald, RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("location_selection_card"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(NeonEmerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = server.flagEmoji.ifEmpty { "🌐" },
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = NeonEmerald
                )
                Text(
                    text = if (server.id.contains("auto", ignoreCase = true) || server.countryCode == "AUTO") "Auto Select Server" else "${server.cityName}, ${server.countryCode}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        // Ping Badge & Arrow
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonEmerald.copy(alpha = 0.15f))
                    .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${server.pingMs} ms",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = NeonEmerald
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun NetworkSelectionCard(
    payload: VpnTweakPayload,
    useConfig: Boolean,
    onToggleUseConfig: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(ObsidianCard)
            .border(1.2.dp, NeonEmerald, RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("network_selection_card"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Network",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = NeonEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = payload.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = NeonEmerald
                        )
                    }
                }
                Text(
                    text = payload.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        // Toggle Switch: 'Use Config'
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Use Config",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.width(4.dp))
            Switch(
                checked = useConfig,
                onCheckedChange = onToggleUseConfig,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ObsidianDark,
                    checkedTrackColor = NeonEmerald,
                    uncheckedThumbColor = TextSecondaryDark,
                    uncheckedTrackColor = ObsidianCardElevated
                ),
                modifier = Modifier.scale(0.8f).testTag("use_config_switch")
            )
        }
    }
}

@Composable
private fun TimerRewardSection(
    timeLeftSeconds: Long,
    onAdd2Hours: () -> Unit,
    onCustomTime: () -> Unit
) {
    val hours = timeLeftSeconds / 3600
    val minutes = (timeLeftSeconds % 3600) / 60
    val seconds = timeLeftSeconds % 60

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("timer_reward_section"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Countdown Timer Badges: [01] H : [59] M : [45] S
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onCustomTime)
        ) {
            TimerBadge(value = "%02d".format(hours), unit = "H")
            Text(
                text = ":",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonEmerald,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            TimerBadge(value = "%02d".format(minutes), unit = "M")
            Text(
                text = ":",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonEmerald,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            TimerBadge(value = "%02d".format(seconds), unit = "S")
        }

        // Prominent "Add 2 Hrs" Button with Rewarded Ad
        Button(
            onClick = onAdd2Hours,
            modifier = Modifier
                .height(40.dp)
                .testTag("add_2_hrs_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonEmerald,
                contentColor = ObsidianDark
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ObsidianDark
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add 2 Hrs",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.5.sp
                )
            )
        }
    }
}

@Composable
private fun TimerBadge(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianDark)
                .border(1.dp, NeonEmerald.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                color = Color.White
            )
        }
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = NeonEmerald
        )
    }
}

@Composable
private fun LargeNeonConnectButton(
    vpnState: VpnState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "power_glow_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (vpnState.isConnected) 1.12f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val activeGlowColor = when (vpnState) {
        VpnState.CONNECTED -> NeonEmerald
        VpnState.CONNECTING, VpnState.RECONNECTING -> AmberAlert
        else -> NeonEmerald
    }

    val buttonText = when (vpnState) {
        VpnState.CONNECTED -> "STOP"
        VpnState.CONNECTING -> "CONNECTING"
        VpnState.RECONNECTING -> "RECONNECTING"
        VpnState.DISCONNECTING -> "STOPPING"
        else -> "START"
    }

    Box(
        modifier = Modifier
            .size(175.dp)
            .testTag("power_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer animated glowing aura
        Box(
            modifier = Modifier
                .size(165.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(activeGlowColor.copy(alpha = if (vpnState.isConnected) 0.22f else 0.12f))
        )

        // Outer Cyber Ring with Dashed/Border styling
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            activeGlowColor,
                            NeonCyan,
                            activeGlowColor
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main Circular Button
        Box(
            modifier = Modifier
                .size(126.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (vpnState.isConnected) listOf(
                            NeonEmerald,
                            Color(0xFF00B359),
                            Color(0xFF007A3D)
                        ) else listOf(
                            ObsidianCardElevated,
                            ObsidianCard,
                            ObsidianDark
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = if (vpnState.isConnected) NeonEmerald else NeonEmerald.copy(alpha = 0.8f),
                    shape = CircleShape
                )
                .clickable(onClick = onClick)
                .testTag("power_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "VPN Power Toggle",
                    tint = if (vpnState.isConnected) ObsidianDark else NeonEmerald,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.5.sp
                    ),
                    color = if (vpnState.isConnected) ObsidianDark else Color.White
                )
            }
        }
    }
}
