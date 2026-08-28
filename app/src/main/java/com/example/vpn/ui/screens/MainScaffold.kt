package com.example.vpn.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark
import com.example.vpn.ui.components.AlphaNavigationDrawerContent
import com.example.vpn.ui.components.ClearDataConfirmDialog
import com.example.vpn.ui.components.CustomPayloadEditorDialog
import com.example.vpn.ui.components.DrawerAction
import com.example.vpn.ui.components.ExitConfirmDialog
import com.example.vpn.ui.components.FollowUsDialog
import com.example.vpn.ui.components.IpHunterDialog
import com.example.vpn.ui.components.TELEGRAM_CHANNEL_URL
import com.example.vpn.ui.components.TetheringDialog
import com.example.vpn.ui.components.UpdateNotesDialog
import com.example.vpn.ui.components.V2raySettingsDialog
import com.example.vpn.viewmodel.VpnScreenTab
import com.example.vpn.viewmodel.VpnUiEvent
import com.example.vpn.viewmodel.VpnViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScaffold(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()
    val sessionStats by viewModel.sessionStats.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val servers by viewModel.filteredServers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val speedTest by viewModel.speedTest.collectAsStateWithLifecycle()

    val timeLeftSeconds by viewModel.timeLeftSeconds.collectAsStateWithLifecycle()
    val payloadsList by viewModel.payloads.collectAsStateWithLifecycle()
    val selectedPayload by viewModel.selectedPayload.collectAsStateWithLifecycle()
    val showStartupDialog by viewModel.showStartupDialog.collectAsStateWithLifecycle()
    val isUpdatingConfig by viewModel.isUpdatingConfig.collectAsStateWithLifecycle()
    val useConfigEnabled by viewModel.useConfigEnabled.collectAsStateWithLifecycle()

    // Dialog States
    var showExitDialog by remember { mutableStateOf(false) }
    var showUpdateNotesDialog by remember { mutableStateOf(false) }
    var showV2rayDialog by remember { mutableStateOf(false) }
    var showIpHunterDialog by remember { mutableStateOf(false) }
    var showTetheringDialog by remember { mutableStateOf(false) }
    var showCustomPayloadDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    // Spin animation for update button
    val infiniteTransition = rememberInfiniteTransition(label = "update_spin")
    val updateRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    // VPN System Permission Launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted()
        } else {
            Toast.makeText(context, "VPN permission required to create secure tunnel", Toast.LENGTH_SHORT).show()
        }
    }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Notification permission handled
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is VpnUiEvent.RequestVpnPermission -> {
                    vpnPermissionLauncher.launch(event.prepareIntent)
                }
                is VpnUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Startup Follow Us Pop-up Dialog
    if (showStartupDialog) {
        FollowUsDialog(
            onDismiss = { dontShowAgain ->
                viewModel.onDismissStartupDialog(dontShowAgain)
            },
            onFollow = { dontShowAgain ->
                viewModel.onDismissStartupDialog(dontShowAgain)
            }
        )
    }

    // Drawer Feature Dialogs
    if (showUpdateNotesDialog) {
        UpdateNotesDialog(onDismiss = { showUpdateNotesDialog = false })
    }

    if (showV2rayDialog) {
        V2raySettingsDialog(
            onDismiss = { showV2rayDialog = false },
            onSave = { protocol, transport, sni, path, mux ->
                viewModel.onCreateCustomPayload("V2Ray $protocol Direct", "V2Ray Engine", sni, "$protocol/$transport")
            }
        )
    }

    if (showIpHunterDialog) {
        IpHunterDialog(onDismiss = { showIpHunterDialog = false })
    }

    if (showTetheringDialog) {
        TetheringDialog(onDismiss = { showTetheringDialog = false })
    }

    if (showCustomPayloadDialog) {
        CustomPayloadEditorDialog(
            onDismiss = { showCustomPayloadDialog = false },
            onCreatePayload = { name, operator, sni, protocol ->
                viewModel.onCreateCustomPayload(name, operator, sni, protocol)
                showCustomPayloadDialog = false
            }
        )
    }

    if (showClearDataDialog) {
        ClearDataConfirmDialog(
            onConfirm = { viewModel.onClearAppData() },
            onDismiss = { showClearDataDialog = false }
        )
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        ExitConfirmDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                viewModel.onPowerClicked() // disconnect if active
                (context as? Activity)?.finish()
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AlphaNavigationDrawerContent(
                onActionClick = { action ->
                    coroutineScope.launch { drawerState.close() }
                    when (action) {
                        DrawerAction.UPDATE_NOTES -> showUpdateNotesDialog = true
                        DrawerAction.JOIN_TELEGRAM -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                viewModel.onOpenFollowDialog()
                            }
                        }
                        DrawerAction.SERVER_STATUS -> viewModel.setTab(VpnScreenTab.SERVERS)
                        DrawerAction.V2RAY_SETTINGS -> showV2rayDialog = true
                        DrawerAction.IP_HUNTER -> showIpHunterDialog = true
                        DrawerAction.TETHERING -> showTetheringDialog = true
                        DrawerAction.ADD_OWN_SERVER -> viewModel.setTab(VpnScreenTab.SERVERS)
                        DrawerAction.ADD_OWN_TWEAKS -> showCustomPayloadDialog = true
                        DrawerAction.CLEAR_APP_DATA -> showClearDataDialog = true
                        DrawerAction.EXIT_APP -> showExitDialog = true
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = ObsidianDark,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(ObsidianCard)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar"),
                    containerColor = ObsidianCard,
                    tonalElevation = 8.dp
                ) {
                    // Button 1: Update (Checks / updates servers and tweaks)
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            viewModel.onUpdateOnlineConfig()
                        },
                        icon = {
                            Icon(
                                imageVector = if (isUpdatingConfig) Icons.Default.Sync else Icons.Outlined.Refresh,
                                contentDescription = "Update Configs",
                                modifier = Modifier
                                    .size(22.dp)
                                    .rotate(if (isUpdatingConfig) updateRotation else 0f),
                                tint = if (isUpdatingConfig) NeonEmerald else NeonEmerald.copy(alpha = 0.8f)
                            )
                        },
                        label = {
                            Text(
                                text = if (isUpdatingConfig) "Updating..." else "Update",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianDark,
                            selectedTextColor = NeonEmerald,
                            indicatorColor = NeonEmerald,
                            unselectedIconColor = NeonEmerald,
                            unselectedTextColor = NeonEmerald
                        ),
                        modifier = Modifier.testTag("nav_btn_update")
                    )

                    // Button 2: Telegram (Opens https://t.me/Big_Tech_sami)
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                viewModel.onOpenFollowDialog()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = "Telegram Channel",
                                modifier = Modifier.size(22.dp),
                                tint = NeonEmerald
                            )
                        },
                        label = {
                            Text(
                                text = "Telegram",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianDark,
                            selectedTextColor = NeonEmerald,
                            indicatorColor = NeonEmerald,
                            unselectedIconColor = NeonEmerald,
                            unselectedTextColor = NeonEmerald
                        ),
                        modifier = Modifier.testTag("nav_btn_telegram")
                    )

                    // Button 3: EXIT (Disconnect & Confirmation Dialog)
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            showExitDialog = true
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ExitToApp,
                                contentDescription = "Exit App",
                                modifier = Modifier.size(22.dp),
                                tint = CrimsonAlert
                            )
                        },
                        label = {
                            Text(
                                text = "EXIT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianDark,
                            selectedTextColor = CrimsonAlert,
                            indicatorColor = CrimsonAlert.copy(alpha = 0.2f),
                            unselectedIconColor = CrimsonAlert,
                            unselectedTextColor = CrimsonAlert
                        ),
                        modifier = Modifier.testTag("nav_btn_exit")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    VpnScreenTab.CONNECT -> {
                        ConnectScreen(
                            vpnState = vpnState,
                            sessionStats = sessionStats,
                            selectedServer = selectedServer,
                            selectedPayload = selectedPayload,
                            payloadsList = payloadsList,
                            timeLeftSeconds = timeLeftSeconds,
                            config = config,
                            useConfigEnabled = useConfigEnabled,
                            onToggleUseConfig = { viewModel.onToggleUseConfig(it) },
                            onPowerClick = { viewModel.onPowerClicked() },
                            onServerCardClick = { viewModel.setTab(VpnScreenTab.SERVERS) },
                            onSelectPayload = { viewModel.onSelectPayload(it) },
                            onCreateCustomPayload = { name, op, sni, proto ->
                                viewModel.onCreateCustomPayload(name, op, sni, proto)
                            },
                            onAddTime = { viewModel.onAddTime(it) },
                            onImportConfig = { name, content ->
                                viewModel.onImportConfig(name, content)
                            },
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onOpenServers = { viewModel.setTab(VpnScreenTab.SERVERS) },
                            onOpenSpeedTest = { viewModel.setTab(VpnScreenTab.SPEED_TEST) },
                            onOpenSplitTunnel = { viewModel.setTab(VpnScreenTab.SPLIT_TUNNEL) },
                            onOpenSettings = { viewModel.setTab(VpnScreenTab.SETTINGS) },
                            onOpenLogs = { viewModel.setTab(VpnScreenTab.LOGS) }
                        )
                    }

                    VpnScreenTab.SERVERS -> {
                        ServerListScreen(
                            servers = servers,
                            selectedServer = selectedServer,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onCategoryChange = { viewModel.setCategory(it) },
                            onServerSelect = {
                                viewModel.onSelectServer(it)
                                viewModel.setTab(VpnScreenTab.CONNECT)
                            },
                            onToggleFavorite = { viewModel.onToggleFavorite(it) },
                            onRefreshPings = { viewModel.onRefreshPings() },
                            onImportServer = { name, ip, port, country, flag, key ->
                                viewModel.onImportCustomServer(name, ip, port, country, flag, key)
                            },
                            onBackClick = { viewModel.setTab(VpnScreenTab.CONNECT) }
                        )
                    }

                    VpnScreenTab.SPEED_TEST -> {
                        SpeedTestScreen(
                            speedTest = speedTest,
                            onStartTest = { viewModel.onStartSpeedTest() },
                            onStopTest = { viewModel.onStopSpeedTest() },
                            onBackClick = { viewModel.setTab(VpnScreenTab.CONNECT) }
                        )
                    }

                    VpnScreenTab.SPLIT_TUNNEL -> {
                        SplitTunnelScreen(
                            config = config,
                            installedApps = installedApps,
                            onUpdateConfig = { viewModel.onUpdateConfig(it) },
                            onToggleApp = { viewModel.onToggleSplitTunnelApp(it) },
                            onBackClick = { viewModel.setTab(VpnScreenTab.CONNECT) }
                        )
                    }

                    VpnScreenTab.LOGS -> {
                        LogsScreen(
                            logs = logs,
                            onClearLogs = { viewModel.onClearLogs() },
                            onBackClick = { viewModel.setTab(VpnScreenTab.CONNECT) }
                        )
                    }

                    VpnScreenTab.SETTINGS -> {
                        SettingsScreen(
                            config = config,
                            onUpdateConfig = { viewModel.onUpdateConfig(it) },
                            onBackClick = { viewModel.setTab(VpnScreenTab.CONNECT) }
                        )
                    }
                }
            }
        }
    }
}
