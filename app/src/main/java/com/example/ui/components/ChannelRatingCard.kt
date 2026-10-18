package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelRating
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary

@Composable
fun ChannelRatingCard(
    rating: ChannelRating,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (rating.isBestChannel) Color(0xFF0D2538) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (rating.isBestChannel) androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("channel_rating_card_${rating.channel}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Number Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (rating.isBestChannel) CyanPrimary else Color(0xFF1E293B),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${rating.channel}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (rating.isBestChannel) Color(0xFF003544) else Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Channel ${rating.channel} (${rating.frequencyMhz} MHz)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (rating.isBestChannel) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(EmeraldSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, EmeraldSecondary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BEST PICK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldSecondary
                            )
                        }
                    }
                }
                Text(
                    text = if (rating.overlappingCount == 0) "No interference detected" else "${rating.overlappingCount} overlapping network(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (rating.overlappingCount == 0) EmeraldSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Star Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val filled = i <= rating.ratingStars
                    Icon(
                        imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (filled) AmberTertiary else Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
