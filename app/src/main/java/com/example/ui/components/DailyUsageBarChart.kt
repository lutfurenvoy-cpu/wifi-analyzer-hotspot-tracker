package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyUsageSummary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.util.NetworkUtils

@Composable
fun DailyUsageBarChart(
    summaries: List<DailyUsageSummary>,
    modifier: Modifier = Modifier
) {
    val maxBytes = summaries.maxOfOrNull { it.downloadBytes + it.uploadBytes }?.coerceAtLeast(100_000_000L) ?: 100_000_000L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_usage_bar_chart")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Data History (7 Days)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(CyanPrimary, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Download", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(8.dp).background(EmeraldSecondary, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Upload", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF090E17), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                summaries.forEach { item ->
                    val total = item.downloadBytes + item.uploadBytes
                    val fraction = (total.toFloat() / maxBytes.toFloat()).coerceIn(0.08f, 1f)
                    val downFraction = (item.downloadBytes.toFloat() / total.toFloat()).coerceIn(0.1f, 0.9f)
                    val upFraction = 1f - downFraction

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Stacked Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            ) {
                                // Upload (top segment)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(upFraction)
                                        .background(EmeraldSecondary)
                                )
                                // Download (bottom segment)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(downFraction)
                                        .background(CyanPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (item.dayLabel == "Today") CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (item.dayLabel == "Today") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
