package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedClient
import com.example.data.model.DeviceType
import com.example.util.NetworkUtils

@Composable
fun ClientDataPieChart(
    clients: List<ConnectedClient>,
    totalHotspotBytes: Long,
    onClientClick: (ConnectedClient) -> Unit,
    modifier: Modifier = Modifier
) {
    val sliceColors = remember {
        listOf(
            Color(0xFF00D2FF), // Vibrant Cyan
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFFA855F7), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFF38BDF8), // Light Blue
            Color(0xFF14B8A6)  // Teal
        )
    }

    val totalClientBytes = clients.sumOf { it.totalBytes }.coerceAtLeast(1L)
    val effectiveTotal = if (totalHotspotBytes > totalClientBytes) totalHotspotBytes else totalClientBytes

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "DonutAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("client_data_pie_chart"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Donut Chart Container
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 22.dp.toPx()
                val diameter = size.minDimension - strokeWidth - 8.dp.toPx()
                val radius = diameter / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background track
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth)
                )

                if (clients.isNotEmpty()) {
                    var currentStartAngle = -90f
                    clients.forEachIndexed { index, client ->
                        val sliceShare = (client.totalBytes.toFloat() / effectiveTotal.toFloat()).coerceIn(0f, 1f)
                        val sweep = sliceShare * 360f * animationProgress
                        val color = sliceColors[index % sliceColors.size]

                        if (sweep > 0.5f) {
                            drawArc(
                                color = color,
                                startAngle = currentStartAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        currentStartAngle += sweep
                    }
                }
            }

            // Central info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = NetworkUtils.formatBytes(effectiveTotal),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Hotspot",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${clients.size} Devices",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device breakdown list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            clients.forEachIndexed { index, client ->
                val color = sliceColors[index % sliceColors.size]
                val sharePercent = ((client.totalBytes.toFloat() / effectiveTotal.toFloat()) * 100f).coerceIn(0f, 100f)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClientClick(client) }
                        .testTag("client_usage_item_${client.macAddress.replace(":", "_")}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val icon = when (client.deviceType) {
                                DeviceType.PHONE -> Icons.Default.PhoneAndroid
                                DeviceType.LAPTOP -> Icons.Default.Computer
                                DeviceType.TABLET -> Icons.Default.Tablet
                                DeviceType.SMART_TV -> Icons.Default.Tv
                                DeviceType.CONSOLE -> Icons.Default.SportsEsports
                                DeviceType.IOT -> Icons.Default.Router
                                else -> Icons.Default.Devices
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = client.deviceType.label,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = client.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${client.ipAddress} • ${client.macAddress}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = NetworkUtils.formatBytes(client.totalBytes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = color
                                )
                                Text(
                                    text = String.format("%.1f%%", sharePercent),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (sharePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = color,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }
    }
}
