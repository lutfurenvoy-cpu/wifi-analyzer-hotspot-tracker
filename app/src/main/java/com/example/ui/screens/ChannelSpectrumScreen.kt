package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.WifiBand
import com.example.ui.components.ChannelRatingCard
import com.example.ui.components.ChannelSpectrumGraph
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ChannelSpectrumScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedBand by remember { mutableStateOf(WifiBand.BAND_2_4_GHZ) }
    val scannedNetworks by viewModel.filteredNetworks.collectAsState()
    val ratings24 by viewModel.channelRatings24.collectAsState()
    val ratings5 by viewModel.channelRatings5.collectAsState()

    val currentRatings = if (selectedBand == WifiBand.BAND_2_4_GHZ) ratings24 else ratings5
    val bestChannel = currentRatings.firstOrNull { it.isBestChannel }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("channel_spectrum_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "Spectrum & Channel Graph",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Visualize signal overlap, channel width and co-channel interference",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Band Switcher Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedBand == WifiBand.BAND_2_4_GHZ,
                    onClick = { selectedBand = WifiBand.BAND_2_4_GHZ },
                    label = { Text("2.4 GHz Band (Channels 1 - 14)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = Color(0xFF003544)
                    ),
                    modifier = Modifier.testTag("tab_band_24")
                )
                FilterChip(
                    selected = selectedBand == WifiBand.BAND_5_GHZ,
                    onClick = { selectedBand = WifiBand.BAND_5_GHZ },
                    label = { Text("5 GHz Band") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanPrimary,
                        selectedLabelColor = Color(0xFF003544)
                    ),
                    modifier = Modifier.testTag("tab_band_5")
                )
            }
        }

        // Parabolic Spectrum Graph
        item {
            ChannelSpectrumGraph(
                networks = scannedNetworks,
                band = selectedBand
            )
        }

        // Recommendation Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF101B2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OPTIMAL CHANNEL RECOMMENDATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (bestChannel != null) {
                        Text(
                            text = "Channel ${bestChannel.channel} is the cleanest pick for your router.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (bestChannel.overlappingCount == 0)
                                "Zero competing access points detected on this frequency range."
                            else
                                "Lowest co-channel and adjacent interference among nearby networks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSecondary
                        )
                    }
                }
            }
        }

        // Channel Ratings Section Header
        item {
            Text(
                text = "Channel Congestion & Quality Ratings",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(currentRatings, key = { "${it.band}_${it.channel}" }) { rating ->
            ChannelRatingCard(rating = rating)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
