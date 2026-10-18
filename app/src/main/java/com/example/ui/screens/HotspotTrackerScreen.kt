package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedClient
import com.example.data.model.DeviceType
import com.example.ui.components.LiveSpeedChart
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.SignalWeak
import com.example.ui.viewmodel.MainViewModel
import com.example.util.NetworkUtils

@Composable
fun HotspotTrackerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.hotspotTelemetry.collectAsState()
    val clients by viewModel.connectedClients.collectAsState()
    val speedHistory by viewModel.speedHistory.collectAsState()
    val clientToEdit by viewModel.clientToEdit.collectAsState()
    val showQuotaDialog by viewModel.showQuotaDialog.collectAsState()
    val isScanningClients by viewModel.isScanningClients.collectAsState()

    var showSpeedGraph by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("hotspot_tracker_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hotspot Data Tracker",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Interface traffic, live client bandwidth & quota caps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.resetSessionUsage() },
                    modifier = Modifier.testTag("reset_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Session",
                        tint = CyanPrimary
                    )
                }
            }
        }

        // Hotspot Session Overview Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hotspot_overview_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(0xFF16253B), Color(0xFF0F1A29))
                            )
                        )
                        .padding(16.dp)
                ) {
                    // Hotspot status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(CyanPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = "Hotspot",
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = telemetry.hotspotSsid,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gateway IP: ${telemetry.hotspotIp} • Interface: ${telemetry.interfaceName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(EmeraldSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, EmeraldSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Separate Breakdown of Download, Upload, and Combined Traffic
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0F1C), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Total Download
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "DL",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = NetworkUtils.formatBytes(telemetry.totalRxBytes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CyanPrimary
                                )
                            }
                            Text(
                                text = "Total Download",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Total Upload
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "UL",
                                    tint = EmeraldSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = NetworkUtils.formatBytes(telemetry.totalTxBytes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldSecondary
                                )
                            }
                            Text(
                                text = "Total Upload",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Live Speed
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = NetworkUtils.formatSpeed(telemetry.currentRxSpeedBps + telemetry.currentTxSpeedBps),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Current Rate",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quota Usage Progress & Threshold Warning Alerts
                    val totalData = telemetry.totalBytes
                    val limitData = telemetry.dataLimitBytes
                    val quotaFraction = telemetry.quotaUsageFraction
                    val isExceeded = quotaFraction >= 1.0f
                    val isNearLimit = quotaFraction >= 0.85f && !isExceeded

                    // Smooth animated progress bar
                    val animatedQuotaProgress by animateFloatAsState(
                        targetValue = quotaFraction.coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 600),
                        label = "QuotaProgress"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Session Data Consumed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = NetworkUtils.formatBytes(totalData),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isExceeded -> SignalWeak
                                        isNearLimit -> AmberTertiary
                                        else -> CyanPrimary
                                    }
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Cap: ${NetworkUtils.formatBytes(limitData)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format("%.1f%% used", quotaFraction * 100f),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        isExceeded -> SignalWeak
                                        isNearLimit -> AmberTertiary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { animatedQuotaProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            isExceeded -> SignalWeak
                            isNearLimit -> AmberTertiary
                            else -> CyanPrimary
                        },
                        trackColor = Color(0xFF1E293B)
                    )

                    // Threshold Warning Alert Banners
                    if (isExceeded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SignalWeak.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, SignalWeak.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Critical Alert",
                                tint = SignalWeak,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🚨 Data Quota Exceeded! Hotspot limit (${NetworkUtils.formatBytes(limitData)}) reached. Further client traffic restricted.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SignalWeak
                            )
                        }
                    } else if (isNearLimit) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AmberTertiary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, AmberTertiary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning Alert",
                                tint = AmberTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ Quota Warning: ${String.format("%.0f%%", quotaFraction * 100f)} of session data limit consumed.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AmberTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons: Set Quota Limit & Toggle Live Graph
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openQuotaDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_quota_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DataUsage,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF003544)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configure Quota", color = Color(0xFF003544), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showSpeedGraph = !showSpeedGraph },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_speed_chart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = CyanPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showSpeedGraph) "Hide Graph" else "Live Speed", color = Color.White)
                        }
                    }
                }
            }
        }

        // Live Real-Time Throughput Graph
        if (showSpeedGraph) {
            item {
                LiveSpeedChart(
                    points = speedHistory,
                    currentRxBps = telemetry.currentRxSpeedBps,
                    currentTxBps = telemetry.currentTxSpeedBps
                )
            }
        }

        // Connected Clients Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Connected Clients & Devices (${clients.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Discovered via ARP cache & subnet probing",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Scan / Refresh Clients Button
                OutlinedButton(
                    onClick = { viewModel.refreshClients() },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("refresh_clients_button")
                ) {
                    if (isScanningClients) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = CyanPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Probing...", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Clients", style = MaterialTheme.typography.labelSmall, color = CyanPrimary)
                    }
                }
            }
        }

        // Connected Clients List
        items(clients, key = { it.macAddress }) { client ->
            ClientCard(
                client = client,
                onEditClick = { viewModel.openEditClient(client) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Dialog: Edit Client Alias, Quota, Block
    if (clientToEdit != null) {
        EditClientDialog(
            client = clientToEdit!!,
            onDismiss = { viewModel.closeEditClient() },
            onSave = { mac, alias, type, quotaGb, isBlocked ->
                viewModel.saveClientDetails(mac, alias, type, quotaGb, isBlocked)
            }
        )
    }

    // Modal Dialog: Set Hotspot Total Quota
    if (showQuotaDialog) {
        SetHotspotQuotaDialog(
            currentLimitBytes = telemetry.dataLimitBytes,
            onDismiss = { viewModel.closeQuotaDialog() },
            onSave = { limitGb, enabled ->
                viewModel.setHotspotDataLimit(limitGb, enabled)
            }
        )
    }
}

@Composable
fun ClientCard(
    client: ConnectedClient,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceIcon = when (client.deviceType) {
        DeviceType.PHONE -> Icons.Default.PhoneAndroid
        DeviceType.LAPTOP -> Icons.Default.Computer
        DeviceType.TABLET -> Icons.Default.Tablet
        DeviceType.SMART_TV -> Icons.Default.Tv
        DeviceType.CONSOLE -> Icons.Default.SportsEsports
        DeviceType.IOT -> Icons.Default.Router
        DeviceType.UNKNOWN -> Icons.Default.Devices
    }

    val vendorName = NetworkUtils.lookupVendor(client.macAddress)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (client.isBlocked) Color(0xFF261414) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = when {
            client.isBlocked -> androidx.compose.foundation.BorderStroke(1.5.dp, SignalWeak)
            client.isQuotaExceeded -> androidx.compose.foundation.BorderStroke(1.5.dp, SignalWeak)
            else -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("client_card_${client.macAddress.replace(":", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Icon, Name/Alias, Device Type, Vendor, Total Data
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            when {
                                client.isBlocked -> SignalWeak.copy(alpha = 0.2f)
                                client.isQuotaExceeded -> SignalWeak.copy(alpha = 0.15f)
                                else -> CyanPrimary.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (client.isBlocked) Icons.Default.Block else deviceIcon,
                        contentDescription = client.deviceType.label,
                        tint = when {
                            client.isBlocked -> SignalWeak
                            client.isQuotaExceeded -> SignalWeak
                            else -> CyanPrimary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name, IP, MAC & Vendor badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = client.displayName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (client.customAlias != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Custom Alias",
                                tint = CyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${client.ipAddress} • ${client.macAddress}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Vendor & Device Type Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = vendorName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = CyanPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = client.deviceType.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Total Data & Block status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = NetworkUtils.formatBytes(client.totalBytes),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (client.isQuotaExceeded) SignalWeak else CyanPrimary
                    )
                    Text(
                        text = if (client.isBlocked) "BLOCKED" else "Total Usage",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (client.isBlocked) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (client.isBlocked) SignalWeak else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quota Violation / Remaining Alert Badge
            if (client.isQuotaExceeded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SignalWeak.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, SignalWeak.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Violation",
                        tint = SignalWeak,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🚨 QUOTA EXCEEDED (Cap: ${NetworkUtils.formatBytes(client.dataQuotaBytes ?: 0L)}). Usage: ${NetworkUtils.formatBytes(client.totalBytes)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SignalWeak
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } else if (client.dataQuotaBytes != null) {
                val remaining = (client.dataQuotaBytes - client.totalBytes).coerceAtLeast(0L)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmberTertiary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cap: ${NetworkUtils.formatBytes(client.dataQuotaBytes)} • Remaining: ${NetworkUtils.formatBytes(remaining)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberTertiary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Live Transfer Speeds and Cumulative DL/UL breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E17), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Download Speed & Total DL
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "DL",
                            tint = CyanPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = NetworkUtils.formatSpeed(client.currentRxSpeedBps),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanPrimary
                        )
                    }
                    Text(
                        text = "DL: ${NetworkUtils.formatBytes(client.totalBytesDownloaded)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Live Upload Speed & Total UL
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "UL",
                            tint = EmeraldSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = NetworkUtils.formatSpeed(client.currentTxSpeedBps),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldSecondary
                        )
                    }
                    Text(
                        text = "UL: ${NetworkUtils.formatBytes(client.totalBytesUploaded)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Edit indicator
                Text(
                    text = "Edit ⚙️",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = CyanPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditClientDialog(
    client: ConnectedClient,
    onDismiss: () -> Unit,
    onSave: (mac: String, alias: String, type: DeviceType, quotaGb: Double?, isBlocked: Boolean) -> Unit
) {
    var aliasText by remember { mutableStateOf(client.customAlias ?: "") }
    var selectedType by remember { mutableStateOf(client.deviceType) }
    var quotaInput by remember {
        mutableStateOf(client.dataQuotaBytes?.let { String.format("%.1f", it / (1024.0 * 1024.0 * 1024.0)) } ?: "")
    }
    var isBlocked by remember { mutableStateOf(client.isBlocked) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Manage Client Device",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "MAC: ${client.macAddress} • IP: ${client.ipAddress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Custom Name / Alias Input
                OutlinedTextField(
                    value = aliasText,
                    onValueChange = { aliasText = it },
                    label = { Text("Custom Alias / Device Name") },
                    placeholder = { Text("e.g. John's Laptop, Living Room TV") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_alias_input")
                )

                // Device Type Selection Chips
                Text(
                    text = "Device Category",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf(
                        DeviceType.PHONE,
                        DeviceType.LAPTOP,
                        DeviceType.TABLET,
                        DeviceType.SMART_TV,
                        DeviceType.CONSOLE,
                        DeviceType.IOT
                    )
                    types.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Color(0xFF003544)
                            )
                        )
                    }
                }

                // Individual Data Cap
                OutlinedTextField(
                    value = quotaInput,
                    onValueChange = { quotaInput = it },
                    label = { Text("Data Quota Limit (GB)") },
                    placeholder = { Text("e.g. 2.0 (empty for no cap)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_quota_input")
                )

                // Quick Quota Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("0.5", "1.0", "2.0", "5.0", "").forEach { preset ->
                        val label = if (preset.isEmpty()) "None" else "${preset}GB"
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .clickable { quotaInput = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = if (quotaInput == preset) CyanPrimary else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                // Restrict / Block Access
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restrict / Block Device",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Block data transmission for this client",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isBlocked,
                        onCheckedChange = { isBlocked = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SignalWeak)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quotaGb = quotaInput.toDoubleOrNull()
                    onSave(client.macAddress, aliasText.trim(), selectedType, quotaGb, isBlocked)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("dialog_save_client_button")
            ) {
                Text("Save Changes", color = Color(0xFF003544), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetHotspotQuotaDialog(
    currentLimitBytes: Long,
    onDismiss: () -> Unit,
    onSave: (limitGb: Double, isEnabled: Boolean) -> Unit
) {
    val initialGb = String.format("%.1f", currentLimitBytes / (1024.0 * 1024.0 * 1024.0))
    var quotaInput by remember { mutableStateOf(initialGb) }
    var isEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Session Data Quota",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Configure maximum total data allocated to all connected hotspot clients during this session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = quotaInput,
                    onValueChange = { quotaInput = it },
                    label = { Text("Hotspot Session Cap (GB)") },
                    placeholder = { Text("e.g. 5.0") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hotspot_quota_input")
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1.0", "2.0", "5.0", "10.0", "20.0").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .clickable { quotaInput = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${preset}GB",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = if (quotaInput == preset) CyanPrimary else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Threshold Warning Alerts",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Notify when usage reaches 85% and 100%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitGb = quotaInput.toDoubleOrNull() ?: 5.0
                    onSave(limitGb, isEnabled)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("save_hotspot_quota_button")
            ) {
                Text("Apply Cap", color = Color(0xFF003544), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
