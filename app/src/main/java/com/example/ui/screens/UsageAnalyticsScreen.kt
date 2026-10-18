package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedClient
import com.example.ui.components.ClientDataPieChart
import com.example.ui.components.DailyUsageBarChart
import com.example.ui.components.LiveSpeedChart
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.SignalWeak
import com.example.ui.viewmodel.MainViewModel
import com.example.util.NetworkUtils

@Composable
fun UsageAnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.hotspotTelemetry.collectAsState()
    val clients by viewModel.connectedClients.collectAsState()
    val speedHistory by viewModel.speedHistory.collectAsState()
    val dailySummaries by viewModel.dailyUsageHistory.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    val sortedClients = remember(clients) {
        clients.sortedByDescending { it.totalBytes }
    }

    val totalClientData = sortedClients.sumOf { it.totalBytes }.coerceAtLeast(1L)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("usage_analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Data Analytics",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visual breakdown of hotspot traffic & consumers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.testTag("reset_analytics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Reset Stats",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Download",
                    value = NetworkUtils.formatBytes(telemetry.totalRxBytes),
                    icon = Icons.Default.ArrowDownward,
                    accentColor = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Upload",
                    value = NetworkUtils.formatBytes(telemetry.totalTxBytes),
                    icon = Icons.Default.ArrowUpward,
                    accentColor = EmeraldSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 1: Client Data Usage Distribution Donut Chart
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF101B2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Client Bandwidth Distribution",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    ClientDataPieChart(
                        clients = sortedClients,
                        totalHotspotBytes = telemetry.totalBytes,
                        onClientClick = { client ->
                            viewModel.openEditClient(client)
                        }
                    )
                }
            }
        }

        // Section 2: Real-time Live Bandwidth Line Chart
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF101B2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Throughput Graph",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    LiveSpeedChart(
                        points = speedHistory,
                        currentRxBps = telemetry.currentRxSpeedBps,
                        currentTxBps = telemetry.currentTxSpeedBps
                    )
                }
            }
        }

        // Section 3: Daily History Bar Chart
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF101B2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = EmeraldSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Weekly Consumption Trends",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    DailyUsageBarChart(summaries = dailySummaries)
                }
            }
        }

        // Section 4: Top Data Consumers Ranking
        item {
            Text(
                text = "Top Consumer Ranking",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        itemsIndexed(sortedClients, key = { _, client -> client.macAddress }) { index, client ->
            RankedConsumerCard(
                rank = index + 1,
                client = client,
                totalHotspotBytes = totalClientData,
                onEditClick = { viewModel.openEditClient(client) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Session Usage?") },
            text = { Text("This will clear live accumulated client traffic statistics and speed history for the current session.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSessionUsage()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalWeak)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF101B2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun RankedConsumerCard(
    rank: Int,
    client: ConnectedClient,
    totalHotspotBytes: Long,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val share = (client.totalBytes.toFloat() / totalHotspotBytes.toFloat()).coerceIn(0f, 1f)

    val medalColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF475569)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("rank_item_$rank")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(medalColor.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, medalColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = medalColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "↓ ${NetworkUtils.formatBytes(client.totalBytesDownloaded)}  •  ↑ ${NetworkUtils.formatBytes(client.totalBytesUploaded)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = NetworkUtils.formatBytes(client.totalBytes),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyanPrimary
                )
                Text(
                    text = String.format("%.1f%% of total", share * 100f),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
