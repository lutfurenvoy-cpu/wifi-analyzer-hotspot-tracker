package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SignalExcellent
import com.example.ui.theme.SignalFair
import com.example.ui.theme.SignalGood
import com.example.ui.theme.SignalWeak
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SignalGauge(
    rssi: Int,
    linkSpeedMbps: Int,
    modifier: Modifier = Modifier
) {
    // RSSI ranges from -100 dBm (poor) to -30 dBm (excellent)
    val clampedRssi = rssi.coerceIn(-100, -30)
    val fraction = (clampedRssi - (-100f)) / 70f // 0.0 to 1.0

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600),
        label = "GaugeSweep"
    )

    val signalColor = when {
        clampedRssi >= -55 -> SignalExcellent
        clampedRssi >= -68 -> SignalGood
        clampedRssi >= -78 -> SignalFair
        else -> SignalWeak
    }

    val signalQualityText = when {
        clampedRssi >= -55 -> "Excellent"
        clampedRssi >= -68 -> "Good"
        clampedRssi >= -78 -> "Fair"
        else -> "Weak"
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .testTag("signal_gauge_component"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth - 16.dp.toPx()
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f + 10.dp.toPx())

            val startAngle = 145f
            val totalSweep = 250f

            // Background track arc
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic Gradient active arc
            val gradientColors = listOf(
                SignalWeak,
                SignalFair,
                SignalGood,
                SignalExcellent
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = gradientColors,
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = totalSweep * animatedFraction,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Needle pointer dot
            val currentAngleRad = Math.toRadians((startAngle + totalSweep * animatedFraction).toDouble())
            val needleX = (center.x + radius * cos(currentAngleRad)).toFloat()
            val needleY = (center.y + radius * sin(currentAngleRad)).toFloat()

            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(needleX, needleY)
            )
            drawCircle(
                color = signalColor,
                radius = 4.dp.toPx(),
                center = Offset(needleX, needleY)
            )
        }

        // Center content metrics
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "$rssi",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "dBm",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = signalQualityText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = signalColor
            )
            if (linkSpeedMbps > 0) {
                Text(
                    text = "$linkSpeedMbps Mbps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
