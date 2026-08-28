package com.example.vpn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.vpn.model.DnsProvider
import com.example.vpn.model.VpnConfig
import com.example.vpn.model.VpnProtocol

@Composable
fun SettingsScreen(
    config: VpnConfig,
    onUpdateConfig: (VpnConfig) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showProtocolDialog by remember { mutableStateOf(false) }
    var showDnsDialog by remember { mutableStateOf(false) }
    var showCustomDnsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
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
                            text = "Security & Settings",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Protocols, DNS shielding & parameters",
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: PROTOCOL & DNS
            SettingsSectionHeader(title = "CORE PROTOCOLS & ENCRYPTION")

            SettingsActionCard(
                icon = Icons.Default.Bolt,
                title = "VPN Protocol",
                subtitle = config.protocol.displayName,
                description = config.protocol.description,
                onClick = { showProtocolDialog = true },
                tag = "settings_protocol_card"
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsActionCard(
                icon = Icons.Default.Dns,
                title = "DNS Leak Protection",
                subtitle = config.dnsProvider.title,
                description = config.dnsProvider.description,
                onClick = { showDnsDialog = true },
                tag = "settings_dns_card"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: SECURITY SAFEGUARDS
            SettingsSectionHeader(title = "SECURITY SAFEGUARDS")

            SettingsToggleCard(
                icon = Icons.Default.Shield,
                title = "Kill Switch",
                subtitle = "Blocks all internet traffic if VPN drops unexpectedly",
                isChecked = config.killSwitchEnabled,
                onCheckedChange = { onUpdateConfig(config.copy(killSwitchEnabled = it)) },
                tag = "kill_switch_toggle"
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsToggleCard(
                icon = Icons.Default.NetworkCheck,
                title = "Auto-Reconnect",
                subtitle = "Instantly restore tunnel upon network handover or drops",
                isChecked = config.autoReconnectOnDrop,
                onCheckedChange = { onUpdateConfig(config.copy(autoReconnectOnDrop = it)) },
                tag = "auto_reconnect_toggle"
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsToggleCard(
                icon = Icons.Default.Wifi,
                title = "Auto-Connect on Wi-Fi",
                subtitle = "Automatically activate tunnel when joining public Wi-Fi",
                isChecked = config.autoConnectOnUntrustedWifi,
                onCheckedChange = { onUpdateConfig(config.copy(autoConnectOnUntrustedWifi = it)) },
                tag = "auto_wifi_toggle"
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsToggleCard(
                icon = Icons.Default.Security,
                title = "Bypass Local LAN Traffic",
                subtitle = "Direct access to printers, AirPlay & local network devices (192.168.x.x)",
                isChecked = config.bypassLocalSubnets,
                onCheckedChange = { onUpdateConfig(config.copy(bypassLocalSubnets = it)) },
                tag = "bypass_lan_toggle"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: NETWORK TUNING
            SettingsSectionHeader(title = "NETWORK PACKET TUNING")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "MTU Size",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tunnel MTU Size",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${config.mtuSize} bytes",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Lower values (1280-1420) prevent packet fragmentation on mobile networks",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = config.mtuSize.toFloat(),
                        onValueChange = { onUpdateConfig(config.copy(mtuSize = it.toInt())) },
                        valueRange = 1280f..1500f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = ObsidianBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: ABOUT ALPHA VPN
            SettingsSectionHeader(title = "ABOUT ALPHA VPN")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = ElectricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Alpha VPN v2.4.0 (Build 2026.08)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Engineered with modern WireGuard® and OpenVPN kernel tunneling protocols. Fully autonomous on-device cryptography with verified zero-logs architecture.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // Protocol Selection Dialog
        if (showProtocolDialog) {
            ProtocolSelectionDialog(
                currentProtocol = config.protocol,
                onSelectProtocol = {
                    onUpdateConfig(config.copy(protocol = it))
                    showProtocolDialog = false
                },
                onDismiss = { showProtocolDialog = false }
            )
        }

        // DNS Selection Dialog
        if (showDnsDialog) {
            DnsSelectionDialog(
                currentDns = config.dnsProvider,
                onSelectDns = { provider ->
                    if (provider == DnsProvider.CUSTOM) {
                        showDnsDialog = false
                        showCustomDnsDialog = true
                    } else {
                        onUpdateConfig(config.copy(dnsProvider = provider))
                        showDnsDialog = false
                    }
                },
                onDismiss = { showDnsDialog = false }
            )
        }

        // Custom DNS Dialog
        if (showCustomDnsDialog) {
            CustomDnsDialog(
                primaryDns = config.customPrimaryDns,
                secondaryDns = config.customSecondaryDns,
                onSave = { p, s ->
                    onUpdateConfig(
                        config.copy(
                            dnsProvider = DnsProvider.CUSTOM,
                            customPrimaryDns = p,
                            customSecondaryDns = s
                        )
                    )
                    showCustomDnsDialog = false
                },
                onDismiss = { showCustomDnsDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp
        ),
        color = TextTertiaryDark,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    description: String,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = NeonCyan),
                onClick = onClick
            )
            .padding(16.dp)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ObsidianCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = NeonCyan
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondaryDark,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCardElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = TextSecondaryDark
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ObsidianCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isChecked) NeonCyan else TextSecondaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = TextSecondaryDark
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ObsidianDark,
                    checkedTrackColor = NeonCyan,
                    uncheckedThumbColor = TextTertiaryDark,
                    uncheckedTrackColor = ObsidianBorder
                ),
                modifier = Modifier.testTag(tag)
            )
        }
    }
}

@Composable
private fun ProtocolSelectionDialog(
    currentProtocol: VpnProtocol,
    onSelectProtocol: (VpnProtocol) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCardElevated,
        title = {
            Text(
                text = "Select VPN Protocol",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VpnProtocol.values().forEach { protocol ->
                    val isSelected = protocol == currentProtocol
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF0C2B3C) else ObsidianCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NeonCyan else ObsidianBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectProtocol(protocol) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = protocol.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (isSelected) NeonCyan else Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = protocol.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondaryDark
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
private fun DnsSelectionDialog(
    currentDns: DnsProvider,
    onSelectDns: (DnsProvider) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCardElevated,
        title = {
            Text(
                text = "Select DNS Provider",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DnsProvider.values().forEach { dns ->
                    val isSelected = dns == currentDns
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF0C2B3C) else ObsidianCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NeonCyan else ObsidianBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectDns(dns) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dns.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isSelected) NeonCyan else Color.White
                                )
                                Text(
                                    text = dns.description,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondaryDark
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
private fun CustomDnsDialog(
    primaryDns: String,
    secondaryDns: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var primary by remember { mutableStateOf(primaryDns) }
    var secondary by remember { mutableStateOf(secondaryDns) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCardElevated,
        title = {
            Text("Configure Custom DNS", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = primary,
                    onValueChange = { primary = it },
                    label = { Text("Primary DNS (e.g. 1.1.1.1)", color = TextSecondaryDark) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = secondary,
                    onValueChange = { secondary = it },
                    label = { Text("Secondary DNS (e.g. 8.8.8.8)", color = TextSecondaryDark) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(primary, secondary) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianDark)
            ) {
                Text("Save DNS", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
