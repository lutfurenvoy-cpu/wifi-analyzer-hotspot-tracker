package com.example.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AnalyzerScreen
import com.example.ui.screens.ChannelSpectrumScreen
import com.example.ui.screens.HotspotTrackerScreen
import com.example.ui.screens.NetworkToolsScreen
import com.example.ui.screens.UsageAnalyticsScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.viewmodel.MainViewModel

enum class NavigationDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    ANALYZER("Analyzer", Icons.Default.Wifi, "nav_item_analyzer"),
    SPECTRUM("Spectrum", Icons.Default.ShowChart, "nav_item_spectrum"),
    HOTSPOT("Hotspot", Icons.Default.WifiTethering, "nav_item_hotspot"),
    ANALYTICS("Analytics", Icons.Default.Insights, "nav_item_analytics"),
    TOOLS("Tools", Icons.Default.Build, "nav_item_tools")
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(NavigationDestination.ANALYZER) }

    // Runtime permissions launcher for WiFi scanning and notifications
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.triggerScan()
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0F1D),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_bottom_nav_bar")
            ) {
                NavigationDestination.entries.forEach { dest ->
                    val selected = currentDestination == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.title
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF003544),
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanPrimary,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag(dest.testTag)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { dest ->
                when (dest) {
                    NavigationDestination.ANALYZER -> AnalyzerScreen(viewModel = viewModel)
                    NavigationDestination.SPECTRUM -> ChannelSpectrumScreen(viewModel = viewModel)
                    NavigationDestination.HOTSPOT -> HotspotTrackerScreen(viewModel = viewModel)
                    NavigationDestination.ANALYTICS -> UsageAnalyticsScreen(viewModel = viewModel)
                    NavigationDestination.TOOLS -> NetworkToolsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
