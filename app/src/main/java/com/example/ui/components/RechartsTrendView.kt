package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AnalyticsTimeframe
import com.example.data.model.UsageTrendPoint
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.util.NetworkUtils

enum class ChartDisplayType {
    AREA_CHART,
    BAR_CHART,
    RECHARTS_WEB
}

@Composable
fun RechartsTrendView(
    dailyData: List<UsageTrendPoint>,
    weeklyData: List<UsageTrendPoint>,
    hourlyData: List<UsageTrendPoint>,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf(AnalyticsTimeframe.DAILY) }
    var chartType by remember { mutableStateOf(ChartDisplayType.AREA_CHART) }

    val currentData = when (selectedTimeframe) {
        AnalyticsTimeframe.DAILY -> dailyData
        AnalyticsTimeframe.WEEKLY -> weeklyData
        AnalyticsTimeframe.HOURLY -> hourlyData
    }

    val totalDownload = currentData.sumOf { it.downloadBytes }
    val totalUpload = currentData.sumOf { it.uploadBytes }
    val totalPeriodData = totalDownload + totalUpload
    val avgData = if (currentData.isNotEmpty()) totalPeriodData / currentData.size else 0L
    val peakPoint = currentData.maxByOrNull { it.totalBytes }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_trend_view")
    ) {
        // Timeframe selector bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnalyticsTimeframe.entries.forEach { timeframe ->
                FilterChip(
                    selected = selectedTimeframe == timeframe,
                    onClick = { selectedTimeframe = timeframe },
                    label = {
                        Text(
                            text = when (timeframe) {
                                AnalyticsTimeframe.DAILY -> "Daily (7D)"
                                AnalyticsTimeframe.WEEKLY -> "Weekly (4W)"
                                AnalyticsTimeframe.HOURLY -> "Hourly (24H)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = Color(0xFF003544)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chart presentation switcher (Area vs Bar vs Recharts Web Engine)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Visualization Style",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ChartTypeChip(
                    label = "Area",
                    icon = Icons.Default.ShowChart,
                    isSelected = chartType == ChartDisplayType.AREA_CHART,
                    onClick = { chartType = ChartDisplayType.AREA_CHART }
                )
                ChartTypeChip(
                    label = "Bar",
                    icon = Icons.Default.BarChart,
                    isSelected = chartType == ChartDisplayType.BAR_CHART,
                    onClick = { chartType = ChartDisplayType.BAR_CHART }
                )
                ChartTypeChip(
                    label = "Recharts Web",
                    icon = Icons.Default.Public,
                    isSelected = chartType == ChartDisplayType.RECHARTS_WEB,
                    onClick = { chartType = ChartDisplayType.RECHARTS_WEB }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Chart Canvas or WebView
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF090E17), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (chartType == ChartDisplayType.RECHARTS_WEB) {
                RechartsEmbeddedWebView(
                    data = currentData,
                    timeframeTitle = "${selectedTimeframe.label} Hotspot Trends"
                )
            } else {
                RechartsNativeCanvasChart(
                    data = currentData,
                    isBarChart = chartType == ChartDisplayType.BAR_CHART
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recharts Key Trend Insights Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF101B2E),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HOTSPOT TREND SUMMARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(EmeraldSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+14.8% vs last cycle",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = EmeraldSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total Traffic", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = NetworkUtils.formatBytes(totalPeriodData), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column {
                        Text(text = "Period Average", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = NetworkUtils.formatBytes(avgData), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CyanPrimary)
                    }
                    Column {
                        Text(text = "Peak Period", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (peakPoint != null) "${peakPoint.label} (${NetworkUtils.formatBytes(peakPoint.totalBytes)})" else "N/A",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartTypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) CyanPrimary else Color(0xFF1E293B),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF003544) else Color(0xFF94A3B8),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) Color(0xFF003544) else Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
fun RechartsNativeCanvasChart(
    data: List<UsageTrendPoint>,
    isBarChart: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val maxVal = remember(data) {
        val peak = data.maxOfOrNull { it.totalBytes } ?: 100_000_000L
        (peak * 1.2).toLong().coerceAtLeast(100_000_000L)
    }

    val selectedPoint = if (selectedIndex in data.indices) data[selectedIndex] else null

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val leftPad = 40.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val chartW = size.width - leftPad - rightPad
                    if (data.isNotEmpty() && offset.x >= leftPad && offset.x <= size.width - rightPad) {
                        val step = chartW / data.size.coerceAtLeast(1)
                        val idx = ((offset.x - leftPad) / step).toInt().coerceIn(0, data.size - 1)
                        selectedIndex = if (selectedIndex == idx) -1 else idx
                    } else {
                        selectedIndex = -1
                    }
                }
            }
            .pointerInput(data) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val leftPad = 40.dp.toPx()
                        val rightPad = 16.dp.toPx()
                        val chartW = size.width - leftPad - rightPad
                        if (data.isNotEmpty() && offset.x >= leftPad && offset.x <= size.width - rightPad) {
                            val step = chartW / data.size.coerceAtLeast(1)
                            selectedIndex = ((offset.x - leftPad) / step).toInt().coerceIn(0, data.size - 1)
                        }
                    },
                    onDrag = { change, _ ->
                        val leftPad = 40.dp.toPx()
                        val rightPad = 16.dp.toPx()
                        val chartW = size.width - leftPad - rightPad
                        if (data.isNotEmpty()) {
                            val step = chartW / data.size.coerceAtLeast(1)
                            selectedIndex = ((change.position.x - leftPad) / step).toInt().coerceIn(0, data.size - 1)
                        }
                    },
                    onDragEnd = { /* keep selection visible */ }
                )
            }
            .padding(top = 10.dp, bottom = 10.dp, start = 8.dp, end = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val leftPad = 36.dp.toPx()
            val rightPad = 16.dp.toPx()
            val bottomPad = 28.dp.toPx()
            val topPad = 16.dp.toPx()

            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - bottomPad - topPad

            // Recharts CartesianGrid horizontal lines
            val gridCount = 4
            val paintGrid = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 9.sp.toPx()
                isAntiAlias = true
            }

            for (i in 0..gridCount) {
                val fraction = i.toFloat() / gridCount.toFloat()
                val y = topPad + chartH * (1f - fraction)
                val valueAtLevel = (maxVal * fraction).toLong()

                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                val label = NetworkUtils.formatBytes(valueAtLevel)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    2.dp.toPx(),
                    y + 3.dp.toPx(),
                    paintGrid
                )
            }

            if (data.isEmpty()) return@Canvas

            val stepX = chartW / data.size.coerceAtLeast(1)

            // Recharts XAxis labels
            val paintX = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            data.forEachIndexed { i, pt ->
                val x = leftPad + (i + 0.5f) * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    pt.label,
                    x,
                    size.height - 8.dp.toPx(),
                    paintX
                )
            }

            if (isBarChart) {
                // Recharts Stacked / Grouped Bar Columns
                val barW = (stepX * 0.55f).coerceAtMost(28.dp.toPx())
                data.forEachIndexed { i, pt ->
                    val centerX = leftPad + (i + 0.5f) * stepX
                    val dlHeight = chartH * (pt.downloadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                    val ulHeight = chartH * (pt.uploadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                    val totalH = dlHeight + ulHeight

                    val baseY = topPad + chartH

                    // Download Bar
                    drawRoundRect(
                        color = CyanPrimary,
                        topLeft = Offset(centerX - barW / 2f, baseY - dlHeight),
                        size = androidx.compose.ui.geometry.Size(barW, dlHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Upload Bar (Stacked above)
                    if (ulHeight > 1f) {
                        drawRoundRect(
                            color = EmeraldSecondary,
                            topLeft = Offset(centerX - barW / 2f, baseY - totalH),
                            size = androidx.compose.ui.geometry.Size(barW, ulHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Selection highlight bar
                    if (selectedIndex == i) {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.15f),
                            topLeft = Offset(centerX - barW / 2f - 4.dp.toPx(), topPad),
                            size = androidx.compose.ui.geometry.Size(barW + 8.dp.toPx(), chartH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            } else {
                // Recharts Monotone Area Spline Chart
                val dlPath = Path()
                val dlFillPath = Path()
                val ulPath = Path()
                val ulFillPath = Path()

                val firstX = leftPad + 0.5f * stepX
                val firstYdl = topPad + chartH * (1f - (data[0].downloadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
                val firstYul = topPad + chartH * (1f - (data[0].uploadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
                val baseY = topPad + chartH

                dlPath.moveTo(firstX, firstYdl)
                dlFillPath.moveTo(firstX, baseY)
                dlFillPath.lineTo(firstX, firstYdl)

                ulPath.moveTo(firstX, firstYul)
                ulFillPath.moveTo(firstX, baseY)
                ulFillPath.lineTo(firstX, firstYul)

                for (i in 1 until data.size) {
                    val prevX = leftPad + (i - 0.5f) * stepX
                    val prevYdl = topPad + chartH * (1f - (data[i - 1].downloadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
                    val curX = leftPad + (i + 0.5f) * stepX
                    val curYdl = topPad + chartH * (1f - (data[i].downloadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))

                    val prevYul = topPad + chartH * (1f - (data[i - 1].uploadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
                    val curYul = topPad + chartH * (1f - (data[i].uploadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))

                    val cx1 = prevX + (curX - prevX) / 2f
                    val cx2 = prevX + (curX - prevX) / 2f

                    dlPath.cubicTo(cx1, prevYdl, cx2, curYdl, curX, curYdl)
                    dlFillPath.cubicTo(cx1, prevYdl, cx2, curYdl, curX, curYdl)

                    ulPath.cubicTo(cx1, prevYul, cx2, curYul, curX, curYul)
                    ulFillPath.cubicTo(cx1, prevYul, cx2, curYul, curX, curYul)
                }

                val lastX = leftPad + (data.size - 0.5f) * stepX
                dlFillPath.lineTo(lastX, baseY)
                dlFillPath.close()

                ulFillPath.lineTo(lastX, baseY)
                ulFillPath.close()

                // Draw Download Area Gradient
                drawPath(
                    path = dlFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(CyanPrimary.copy(alpha = 0.45f), Color.Transparent),
                        startY = topPad,
                        endY = baseY
                    )
                )
                drawPath(path = dlPath, color = CyanPrimary, style = Stroke(width = 2.5.dp.toPx()))

                // Draw Upload Area Gradient
                drawPath(
                    path = ulFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(EmeraldSecondary.copy(alpha = 0.35f), Color.Transparent),
                        startY = topPad,
                        endY = baseY
                    )
                )
                drawPath(path = ulPath, color = EmeraldSecondary, style = Stroke(width = 2.dp.toPx()))

                // Draw Apex Dots
                data.forEachIndexed { i, pt ->
                    val x = leftPad + (i + 0.5f) * stepX
                    val yDl = topPad + chartH * (1f - (pt.downloadBytes.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
                    drawCircle(color = CyanPrimary, radius = 3.dp.toPx(), center = Offset(x, yDl))
                }
            }

            // Interactive Tooltip Cursor Line (Recharts Tooltip behavior)
            if (selectedIndex in data.indices) {
                val cursorX = leftPad + (selectedIndex + 0.5f) * stepX
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(cursorX, topPad),
                    end = Offset(cursorX, topPad + chartH),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
            }
        }

        // Floating Recharts Tooltip Card
        if (selectedPoint != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "${selectedPoint.fullDate} (${selectedPoint.label})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(CyanPrimary, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Download: ${NetworkUtils.formatBytes(selectedPoint.downloadBytes)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = CyanPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(EmeraldSecondary, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Upload: ${NetworkUtils.formatBytes(selectedPoint.uploadBytes)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = EmeraldSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFA855F7), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Total: ${NetworkUtils.formatBytes(selectedPoint.totalBytes)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFFA855F7)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsEmbeddedWebView(
    data: List<UsageTrendPoint>,
    timeframeTitle: String,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val jsonData = remember(data) {
        val list = data.map {
            val dlGb = it.downloadBytes / (1024.0 * 1024.0 * 1024.0)
            val ulGb = it.uploadBytes / (1024.0 * 1024.0 * 1024.0)
            val totGb = dlGb + ulGb
            """{"name":"${it.label}","download":${String.format(java.util.Locale.US, "%.2f", dlGb)},"upload":${String.format(java.util.Locale.US, "%.2f", ulGb)},"total":${String.format(java.util.Locale.US, "%.2f", totGb)}}"""
        }
        "[${list.joinToString(",")}]"
    }

    LaunchedEffect(jsonData, webViewRef) {
        webViewRef?.evaluateJavascript(
            "if(window.updateRechartsData){ window.updateRechartsData('$jsonData', 'area', '$timeframeTitle'); }",
            null
        )
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            "if(window.updateRechartsData){ window.updateRechartsData('$jsonData', 'area', '$timeframeTitle'); }",
                            null
                        )
                    }
                }
                webChromeClient = WebChromeClient()
                loadUrl("file:///android_asset/recharts_dashboard.html")
                webViewRef = this
            }
        },
        update = { webView ->
            webView.evaluateJavascript(
                "if(window.updateRechartsData){ window.updateRechartsData('$jsonData', 'area', '$timeframeTitle'); }",
                null
            )
        },
        modifier = modifier.fillMaxSize()
    )
}
