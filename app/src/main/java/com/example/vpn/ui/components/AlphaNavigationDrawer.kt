package com.example.vpn.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

enum class DrawerAction {
    UPDATE_NOTES,
    JOIN_TELEGRAM,
    SERVER_STATUS,
    V2RAY_SETTINGS,
    IP_HUNTER,
    TETHERING,
    ADD_OWN_SERVER,
    ADD_OWN_TWEAKS,
    CLEAR_APP_DATA,
    EXIT_APP
}

@Composable
fun AlphaNavigationDrawerContent(
    onActionClick: (DrawerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight(),
        drawerContainerColor = ObsidianDark,
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(ObsidianCardElevated, ObsidianDark)
                    )
                )
        ) {
            // Header: Brand banner with Shield & Version
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                NeonEmerald.copy(alpha = 0.2f),
                                ObsidianCardElevated
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(NeonEmerald, Color.Transparent)),
                        shape = RoundedCornerShape(bottomEnd = 24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(NeonEmerald.copy(alpha = 0.35f), ObsidianDark)
                                )
                            )
                            .border(1.5.dp, NeonEmerald, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Alpha VPN Logo",
                            tint = NeonEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Alpha VPN",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "v1.4.2 [ONLINE]",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = NeonEmerald
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Items List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerMenuItem(
                    icon = Icons.Default.NewReleases,
                    title = "Update Notes",
                    subtitle = "Release v1.4.2 changelog",
                    tint = NeonEmerald,
                    tag = "drawer_item_update_notes",
                    onClick = { onActionClick(DrawerAction.UPDATE_NOTES) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Send,
                    title = "Join Telegram",
                    subtitle = "@Big_Tech_sami channel",
                    tint = NeonCyan,
                    tag = "drawer_item_join_telegram",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback
                        }
                        onActionClick(DrawerAction.JOIN_TELEGRAM)
                    }
                )

                DrawerMenuItem(
                    icon = Icons.Default.CloudDone,
                    title = "Server Status",
                    subtitle = "Live nodes & pings",
                    tint = NeonEmerald,
                    tag = "drawer_item_server_status",
                    onClick = { onActionClick(DrawerAction.SERVER_STATUS) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = ObsidianBorder
                )

                DrawerMenuItem(
                    icon = Icons.Default.Hub,
                    title = "V2ray Settings",
                    subtitle = "VLESS / VMess / Trojan",
                    tint = NeonCyan,
                    tag = "drawer_item_v2ray_settings",
                    onClick = { onActionClick(DrawerAction.V2RAY_SETTINGS) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Radar,
                    title = "IP Hunter",
                    subtitle = "ISP subnet scanner",
                    tint = NeonEmerald,
                    tag = "drawer_item_ip_hunter",
                    onClick = { onActionClick(DrawerAction.IP_HUNTER) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.WifiTethering,
                    title = "Tethering",
                    subtitle = "Hotspot proxy share",
                    tint = NeonCyan,
                    tag = "drawer_item_tethering",
                    onClick = { onActionClick(DrawerAction.TETHERING) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = ObsidianBorder
                )

                DrawerMenuItem(
                    icon = Icons.Default.AddCircleOutline,
                    title = "Add Own Server",
                    subtitle = "Custom V2Ray / WireGuard",
                    tint = NeonEmerald,
                    tag = "drawer_item_add_server",
                    onClick = { onActionClick(DrawerAction.ADD_OWN_SERVER) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.Tune,
                    title = "Add Own Tweaks",
                    subtitle = "Custom SNI / Payload",
                    tint = NeonCyan,
                    tag = "drawer_item_add_tweaks",
                    onClick = { onActionClick(DrawerAction.ADD_OWN_TWEAKS) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "Clear App Data",
                    subtitle = "Reset cache & preferences",
                    tint = CrimsonAlert,
                    tag = "drawer_item_clear_data",
                    onClick = { onActionClick(DrawerAction.CLEAR_APP_DATA) }
                )

                DrawerMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Exit App",
                    subtitle = "Disconnect & close",
                    tint = CrimsonAlert,
                    tag = "drawer_item_exit",
                    onClick = { onActionClick(DrawerAction.EXIT_APP) }
                )
            }

            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Alpha VPN • Powered by Big Tech Sami",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondaryDark
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f))
                .border(1.dp, tint.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = TextPrimaryDark
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondaryDark
            )
        }
    }
}
