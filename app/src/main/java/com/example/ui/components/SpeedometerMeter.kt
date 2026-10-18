package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedTestStage
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin

@Composable
fun SpeedometerMeter(
    speedMbps: Double,
    stage: SpeedTestStage,
    maxScaleMbps: Double = 500.0,
    modifier: Modifier = Modifier
) {
    // Map speed to progress between 0.0 and 1.0 using non-linear scale for good low & high speed visibility
    val normalizedProgress = rememberSpeedProgress(speedMbps, maxScaleMbps)

    val animatedProgress by animateFloatAsState(
        targetValue = normalizedProgress,
        animationSpec = tween(durationMillis = 250),
        label = "SpeedometerProgress"
    )

    val trackColor = Color(0xFF1E293B)
    val startAngle = 135f
    val sweepTotalAngle = 270f
    val currentSweep = sweepTotalAngle * animatedProgress

    val activeColor = when {
        speedMbps > 150 -> Color(0xFFA855F7) // Purple
        speedMbps > 75 -> EmeraldSecondary
        speedMbps > 25 -> CyanPrimary
        speedMbps > 5 -> AmberTertiary
        else -> Color(0xFF00D2FF)
    }

    Box(
        modifier = modifier
            .testTag("speedometer_meter")
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .testTag("speedometer_canvas")
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 30.dp.toPx()
            val strokeWidth = 14.dp.toPx()

            // 1. Background Arc Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepTotalAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Progress Arc with Gradient
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(
                    CyanPrimary,
                    EmeraldSecondary,
                    AmberTertiary,
                    Color(0xFFA855F7),
                    CyanPrimary
                ),
                center = center
            )

            if (currentSweep > 0.5f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth + 2f, cap = StrokeCap.Round)
                )
            }

            // 3. Scale Ticks & Labels
            val ticks = listOf(
                0 to "0",
                10 to "10",
                25 to "25",
                50 to "50",
                100 to "100",
                250 to "250",
                500 to "500+"
            )

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 10.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            ticks.forEach { (tickValue, label) ->
                val tickProgress = speedToProgress(tickValue.toDouble(), maxScaleMbps)
                val tickAngleDeg = startAngle + (sweepTotalAngle * tickProgress)
                val tickAngleRad = (tickAngleDeg * PI / 180f).toFloat()

                val innerR = radius - 16.dp.toPx()
                val outerR = radius - 8.dp.toPx()
                val textR = radius - 28.dp.toPx()

                val startP = Offset(
                    center.x + innerR * cos(tickAngleRad),
                    center.y + innerR * sin(tickAngleRad)
                )
                val endP = Offset(
                    center.x + outerR * cos(tickAngleRad),
                    center.y + outerR * sin(tickAngleRad)
                )

                drawLine(
                    color = if (tickProgress <= animatedProgress) activeColor else Color(0xFF334155),
                    start = startP,
                    end = endP,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val textX = center.x + textR * cos(tickAngleRad)
                val textY = center.y + textR * sin(tickAngleRad) + 4.dp.toPx()
                drawContext.canvas.nativeCanvas.drawText(label, textX, textY, textPaint)
            }

            // 4. Needle Pointer
            val needleAngleDeg = startAngle + currentSweep
            val needleAngleRad = (needleAngleDeg * PI / 180f).toFloat()
            val needleLength = radius - 10.dp.toPx()

            val needleTip = Offset(
                center.x + needleLength * cos(needleAngleRad),
                center.y + needleLength * sin(needleAngleRad)
            )

            // Needle base width
            val perpAngleRad = needleAngleRad + (PI / 2).toFloat()
            val baseRadius = 7.dp.toPx()
            val base1 = Offset(
                center.x + baseRadius * cos(perpAngleRad),
                center.y + baseRadius * sin(perpAngleRad)
            )
            val base2 = Offset(
                center.x - baseRadius * cos(perpAngleRad),
                center.y - baseRadius * sin(perpAngleRad)
            )

            val needlePath = Path().apply {
                moveTo(needleTip.x, needleTip.y)
                lineTo(base1.x, base1.y)
                lineTo(base2.x, base2.y)
                close()
            }

            // Draw glowing needle
            drawPath(
                path = needlePath,
                color = activeColor
            )

            // 5. Center Hub
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 16.dp.toPx(),
                center = center
            )
            drawCircle(
                color = activeColor,
                radius = 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // Center Digital Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 100.dp)
        ) {
            Text(
                text = String.format("%.1f", speedMbps.coerceAtLeast(0.0)),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black
                ),
                color = if (speedMbps > 0) activeColor else Color.White
            )
            Text(
                text = "Mbps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stage pill
            StageIndicatorBadge(stage = stage)
        }
    }
}

@Composable
fun StageIndicatorBadge(stage: SpeedTestStage) {
    val (label, tint) = when (stage) {
        SpeedTestStage.IDLE -> "READY TO TEST" to Color(0xFF94A3B8)
        SpeedTestStage.PING -> "TESTING PING" to AmberTertiary
        SpeedTestStage.DOWNLOAD -> "TESTING DOWNLOAD" to CyanPrimary
        SpeedTestStage.UPLOAD -> "TESTING UPLOAD" to EmeraldSecondary
        SpeedTestStage.COMPLETE -> "TEST COMPLETED" to EmeraldSecondary
        SpeedTestStage.ERROR -> "INTERRUPTED" to Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .background(tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(tint, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = tint
            )
        }
    }
}

private fun speedToProgress(mbps: Double, maxScale: Double): Float {
    if (mbps <= 0.0) return 0f
    // Logarithmic curve: 0 -> 0.0, 10 -> 0.22, 25 -> 0.38, 50 -> 0.52, 100 -> 0.68, 250 -> 0.85, 500 -> 1.0
    val logValue = ln(1.0 + mbps)
    val maxLog = ln(1.0 + maxScale)
    return (logValue / maxLog).toFloat().coerceIn(0f, 1f)
}

@Composable
private fun rememberSpeedProgress(speedMbps: Double, maxScaleMbps: Double): Float {
    return speedToProgress(speedMbps, maxScaleMbps)
}
