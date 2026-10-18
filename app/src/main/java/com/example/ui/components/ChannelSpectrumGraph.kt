package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WifiBand
import com.example.data.model.WifiNetworkInfo
import kotlin.math.abs

@Composable
fun ChannelSpectrumGraph(
    networks: List<WifiNetworkInfo>,
    band: WifiBand,
    modifier: Modifier = Modifier
) {
    // Distinct vibrant color palette for networks in graph
    val curveColors = remember {
        listOf(
            Color(0xFF00D2FF), // Cyan
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFFA855F7), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFF3B82F6), // Blue
            Color(0xFF14B8A6), // Teal
            Color(0xFFE11D48)  // Rose
        )
    }

    // Determine channels to display on X axis
    val displayChannels = if (band == WifiBand.BAND_2_4_GHZ) {
        listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
    } else {
        listOf(36, 40, 44, 48, 52, 56, 60, 64, 100, 104, 149, 153, 157, 161)
    }

    val filteredNetworks = remember(networks, band) {
        networks.filter { it.band == band }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("channel_spectrum_graph")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFF090E17), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(216.dp)) {
                val leftPadding = 36.dp.toPx()
                val rightPadding = 16.dp.toPx()
                val bottomPadding = 28.dp.toPx()
                val topPadding = 16.dp.toPx()

                val graphWidth = size.width - leftPadding - rightPadding
                val graphHeight = size.height - bottomPadding - topPadding

                val minRssi = -100f
                val maxRssi = -30f

                // Draw horizontal dBm grid lines and labels
                val dbmLevels = listOf(-30, -50, -70, -90)
                val paintDbm = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                }

                dbmLevels.forEach { dbm ->
                    val y = topPadding + graphHeight * (1f - (dbm - minRssi) / (maxRssi - minRssi))
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "$dbm",
                        4.dp.toPx(),
                        y + 3.dp.toPx(),
                        paintDbm
                    )
                }

                // X-Axis mapping
                val channelPositions = displayChannels.mapIndexed { index, ch ->
                    val x = leftPadding + (index.toFloat() / (displayChannels.size - 1).toFloat()) * graphWidth
                    ch to x
                }.toMap()

                // Draw X-axis channel numbers and vertical tick marks
                val paintChannel = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                displayChannels.forEach { ch ->
                    val x = channelPositions[ch] ?: return@forEach
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(x, topPadding),
                        end = Offset(x, size.height - bottomPadding),
                        strokeWidth = 1.dp.toPx()
                    )
                    // Draw channel label
                    drawContext.canvas.nativeCanvas.drawText(
                        "$ch",
                        x,
                        size.height - 8.dp.toPx(),
                        paintChannel
                    )
                }

                // Draw Parabolic curves for each network
                val paintLabel = android.graphics.Paint().apply {
                    textSize = 9.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                filteredNetworks.forEachIndexed { index, net ->
                    val peakRssi = net.rssi.toFloat().coerceIn(minRssi, maxRssi)
                    val peakY = topPadding + graphHeight * (1f - (peakRssi - minRssi) / (maxRssi - minRssi))
                    val baseY = topPadding + graphHeight

                    val color = if (net.isCurrentConnection) {
                        Color(0xFF00D2FF)
                    } else {
                        curveColors[index % curveColors.size]
                    }

                    // Find closest x position for center channel
                    val centerX = if (channelPositions.containsKey(net.channel)) {
                        channelPositions[net.channel]!!
                    } else {
                        // interpolate
                        val first = displayChannels.first()
                        val last = displayChannels.last()
                        val ratio = ((net.channel - first).toFloat() / (last - first).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                        leftPadding + ratio * graphWidth
                    }

                    // Channel spread width in pixels (approx 4-5 channel widths on 2.4G)
                    val channelSpacingPx = if (displayChannels.size > 1) {
                        graphWidth / (displayChannels.size - 1)
                    } else graphWidth / 14f

                    val spreadHalfWidth = if (band == WifiBand.BAND_2_4_GHZ) {
                        channelSpacingPx * 2.2f // 2.4G covers ~4 channels total
                    } else {
                        channelSpacingPx * 1.5f
                    }

                    val leftX = centerX - spreadHalfWidth
                    val rightX = centerX + spreadHalfWidth

                    // Build parabolic path
                    val path = Path().apply {
                        moveTo(leftX, baseY)
                        // Cubic curve simulating bell/parabola
                        cubicTo(
                            leftX + spreadHalfWidth * 0.4f, baseY,
                            centerX - spreadHalfWidth * 0.3f, peakY,
                            centerX, peakY
                        )
                        cubicTo(
                            centerX + spreadHalfWidth * 0.3f, peakY,
                            rightX - spreadHalfWidth * 0.4f, baseY,
                            rightX, baseY
                        )
                    }

                    // Translucent fill
                    val fillBrush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = if (net.isCurrentConnection) 0.45f else 0.22f),
                            color.copy(alpha = 0.02f)
                        ),
                        startY = peakY,
                        endY = baseY
                    )
                    drawPath(path = path, brush = fillBrush)

                    // Stroke line
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = if (net.isCurrentConnection) 3.5.dp.toPx() else 2.dp.toPx()
                        )
                    )

                    // Peak dot
                    drawCircle(
                        color = color,
                        radius = if (net.isCurrentConnection) 4.5.dp.toPx() else 3.dp.toPx(),
                        center = Offset(centerX, peakY)
                    )

                    // Network SSID Label over apex
                    paintLabel.color = if (net.isCurrentConnection) {
                        android.graphics.Color.WHITE
                    } else {
                        android.graphics.Color.parseColor("#CBD5E1")
                    }
                    val truncatedSsid = if (net.ssid.length > 10) net.ssid.take(9) + "…" else net.ssid
                    drawContext.canvas.nativeCanvas.drawText(
                        truncatedSsid,
                        centerX,
                        peakY - 6.dp.toPx(),
                        paintLabel
                    )
                }
            }
        }

        // Legend below graph
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Channels (${band.displayName})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            filteredNetworks.take(4).forEachIndexed { idx, net ->
                val col = if (net.isCurrentConnection) Color(0xFF00D2FF) else curveColors[idx % curveColors.size]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(col, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = net.ssid.take(6),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
