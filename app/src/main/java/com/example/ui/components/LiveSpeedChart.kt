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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.SpeedDataPoint
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary

@Composable
fun LiveSpeedChart(
    points: List<SpeedDataPoint>,
    currentRxBps: Long,
    currentTxBps: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_speed_chart")
    ) {
        // Top stats bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Download Speed",
                    tint = CyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DL: ${formatKbps(currentRxBps)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyanPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Upload Speed",
                    tint = EmeraldSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "UL: ${formatKbps(currentTxBps)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldSecondary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Live (30s)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF090E17), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val leftPad = 36.dp.toPx()
                val bottomPad = 20.dp.toPx()
                val topPad = 12.dp.toPx()
                val rightPad = 10.dp.toPx()

                val chartW = size.width - leftPad - rightPad
                val chartH = size.height - bottomPad - topPad

                val maxRx = points.maxOfOrNull { it.rxSpeedKbps } ?: 100f
                val maxTx = points.maxOfOrNull { it.txSpeedKbps } ?: 50f
                val peakSpeed = maxOf(maxRx, maxTx, 150f) * 1.15f // margin on top

                // Horizontal grid lines
                val gridLevels = listOf(peakSpeed, peakSpeed * 0.66f, peakSpeed * 0.33f, 0f)
                val paintGridText = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                }

                gridLevels.forEach { speedVal ->
                    val y = topPad + chartH * (1f - (speedVal / peakSpeed))
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(leftPad, y),
                        end = Offset(size.width - rightPad, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                    val label = if (speedVal >= 1024) String.format("%.1fM", speedVal / 1024f) else "${speedVal.toInt()}K"
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        4.dp.toPx(),
                        y + 3.dp.toPx(),
                        paintGridText
                    )
                }

                if (points.size >= 2) {
                    val stepX = chartW / (points.size - 1).coerceAtLeast(1)

                    // Draw Download Path (Cyan)
                    val rxPath = Path()
                    val rxFillPath = Path()
                    rxPath.moveTo(leftPad, topPad + chartH * (1f - (points[0].rxSpeedKbps / peakSpeed).coerceIn(0f, 1f)))
                    rxFillPath.moveTo(leftPad, topPad + chartH)
                    rxFillPath.lineTo(leftPad, topPad + chartH * (1f - (points[0].rxSpeedKbps / peakSpeed).coerceIn(0f, 1f)))

                    for (i in 1 until points.size) {
                        val prevX = leftPad + (i - 1) * stepX
                        val prevY = topPad + chartH * (1f - (points[i - 1].rxSpeedKbps / peakSpeed).coerceIn(0f, 1f))
                        val curX = leftPad + i * stepX
                        val curY = topPad + chartH * (1f - (points[i].rxSpeedKbps / peakSpeed).coerceIn(0f, 1f))

                        val cX1 = prevX + (curX - prevX) / 2f
                        val cY1 = prevY
                        val cX2 = prevX + (curX - prevX) / 2f
                        val cY2 = curY

                        rxPath.cubicTo(cX1, cY1, cX2, cY2, curX, curY)
                        rxFillPath.cubicTo(cX1, cY1, cX2, cY2, curX, curY)
                    }
                    rxFillPath.lineTo(leftPad + (points.size - 1) * stepX, topPad + chartH)
                    rxFillPath.close()

                    // Fill gradient
                    drawPath(
                        path = rxFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CyanPrimary.copy(alpha = 0.35f), Color.Transparent),
                            startY = topPad,
                            endY = topPad + chartH
                        )
                    )
                    // Line
                    drawPath(
                        path = rxPath,
                        color = CyanPrimary,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Draw Upload Path (Emerald)
                    val txPath = Path()
                    val txFillPath = Path()
                    txPath.moveTo(leftPad, topPad + chartH * (1f - (points[0].txSpeedKbps / peakSpeed).coerceIn(0f, 1f)))
                    txFillPath.moveTo(leftPad, topPad + chartH)
                    txFillPath.lineTo(leftPad, topPad + chartH * (1f - (points[0].txSpeedKbps / peakSpeed).coerceIn(0f, 1f)))

                    for (i in 1 until points.size) {
                        val prevX = leftPad + (i - 1) * stepX
                        val prevY = topPad + chartH * (1f - (points[i - 1].txSpeedKbps / peakSpeed).coerceIn(0f, 1f))
                        val curX = leftPad + i * stepX
                        val curY = topPad + chartH * (1f - (points[i].txSpeedKbps / peakSpeed).coerceIn(0f, 1f))

                        val cX1 = prevX + (curX - prevX) / 2f
                        val cY1 = prevY
                        val cX2 = prevX + (curX - prevX) / 2f
                        val cY2 = curY

                        txPath.cubicTo(cX1, cY1, cX2, cY2, curX, curY)
                        txFillPath.cubicTo(cX1, cY1, cX2, cY2, curX, curY)
                    }
                    txFillPath.lineTo(leftPad + (points.size - 1) * stepX, topPad + chartH)
                    txFillPath.close()

                    drawPath(
                        path = txFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(EmeraldSecondary.copy(alpha = 0.25f), Color.Transparent),
                            startY = topPad,
                            endY = topPad + chartH
                        )
                    )
                    drawPath(
                        path = txPath,
                        color = EmeraldSecondary,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun formatKbps(bytesPerSec: Long): String {
    val kbps = bytesPerSec / 1024f
    return if (kbps >= 1024) {
        String.format("%.2f MB/s", kbps / 1024f)
    } else {
        String.format("%.1f KB/s", kbps)
    }
}
