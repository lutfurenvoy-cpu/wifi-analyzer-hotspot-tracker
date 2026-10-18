package com.example.data.model

enum class WifiBand(val displayName: String) {
    BAND_2_4_GHZ("2.4 GHz"),
    BAND_5_GHZ("5 GHz"),
    BAND_6_GHZ("6 GHz")
}

enum class DeviceType(val label: String) {
    PHONE("Smartphone"),
    LAPTOP("Laptop / PC"),
    TABLET("Tablet"),
    SMART_TV("Smart TV / Cast"),
    CONSOLE("Gaming Console"),
    IOT("IoT / Smart Home"),
    UNKNOWN("Network Device")
}

data class WifiNetworkInfo(
    val bssid: String,
    val ssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val band: WifiBand,
    val channelWidthMhz: Int = 20,
    val capabilities: String = "",
    val securityType: String = "WPA2",
    val distanceMeters: Double = 0.0,
    val vendor: String = "Unknown",
    val isCurrentConnection: Boolean = false
) {
    val signalLevelPercent: Int
        get() = when {
            rssi <= -100 -> 0
            rssi >= -50 -> 100
            else -> 2 * (rssi + 100)
        }
}

data class CurrentWifiConnection(
    val isConnected: Boolean = false,
    val ssid: String = "Not Connected",
    val bssid: String = "--:--:--:--:--:--",
    val rssi: Int = -100,
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val channel: Int = 0,
    val ipAddress: String = "0.0.0.0",
    val gateway: String = "0.0.0.0",
    val subnetMask: String = "255.255.255.0",
    val dns1: String = "8.8.8.8",
    val dns2: String = "1.1.1.1",
    val macAddress: String = "--:--:--:--:--:--",
    val band: WifiBand = WifiBand.BAND_2_4_GHZ
) {
    val signalLevelPercent: Int
        get() = when {
            rssi <= -100 -> 0
            rssi >= -50 -> 100
            else -> 2 * (rssi + 100)
        }
}

data class ConnectedClient(
    val ipAddress: String,
    val macAddress: String,
    val hostname: String,
    val customAlias: String? = null,
    val deviceType: DeviceType = DeviceType.UNKNOWN,
    val totalBytesDownloaded: Long = 0L,
    val totalBytesUploaded: Long = 0L,
    val currentRxSpeedBps: Long = 0L,
    val currentTxSpeedBps: Long = 0L,
    val isOnline: Boolean = true,
    val firstSeenMillis: Long = System.currentTimeMillis(),
    val lastSeenMillis: Long = System.currentTimeMillis(),
    val dataQuotaBytes: Long? = null,
    val isBlocked: Boolean = false
) {
    val displayName: String
        get() = customAlias?.takeIf { it.isNotBlank() } ?: hostname.takeIf { it.isNotBlank() && it != "?" } ?: "Device ${macAddress.takeLast(5)}"

    val totalBytes: Long
        get() = totalBytesDownloaded + totalBytesUploaded

    val isQuotaExceeded: Boolean
        get() = dataQuotaBytes != null && totalBytes >= dataQuotaBytes
}

data class HotspotTelemetry(
    val isHotspotActive: Boolean = true,
    val hotspotSsid: String = "AndroidHotspot",
    val hotspotIp: String = "192.168.43.1",
    val interfaceName: String = "ap0 / wlan0",
    val totalRxBytes: Long = 0L,
    val totalTxBytes: Long = 0L,
    val currentRxSpeedBps: Long = 0L,
    val currentTxSpeedBps: Long = 0L,
    val dataLimitBytes: Long = 5_368_709_120L, // 5 GB default
    val isDataLimitEnabled: Boolean = true,
    val connectedClientsCount: Int = 0
) {
    val totalBytes: Long
        get() = totalRxBytes + totalTxBytes

    val quotaUsageFraction: Float
        get() = if (dataLimitBytes > 0) (totalBytes.toFloat() / dataLimitBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

data class SpeedDataPoint(
    val timestamp: Long,
    val rxSpeedKbps: Float,
    val txSpeedKbps: Float
)

data class DailyUsageSummary(
    val dayLabel: String,
    val downloadBytes: Long,
    val uploadBytes: Long
)

data class UsageTrendPoint(
    val label: String,
    val fullDate: String,
    val downloadBytes: Long,
    val uploadBytes: Long
) {
    val totalBytes: Long
        get() = downloadBytes + uploadBytes
}

enum class AnalyticsTimeframe(val label: String) {
    DAILY("Daily (7 Days)"),
    WEEKLY("Weekly (4 Weeks)"),
    HOURLY("Hourly (24 Hours)")
}

data class ChannelRating(
    val channel: Int,
    val band: WifiBand,
    val frequencyMhz: Int,
    val ratingStars: Int,
    val overlappingCount: Int,
    val isBestChannel: Boolean = false
)

data class PingResult(
    val host: String,
    val isReachable: Boolean,
    val avgLatencyMs: Float,
    val packetLossPercent: Float,
    val hops: List<Float> = emptyList()
)

data class SubnetHost(
    val ip: String,
    val hostname: String,
    val isReachable: Boolean,
    val latencyMs: Long
)

sealed class WifiScanState {
    object Idle : WifiScanState()
    object Scanning : WifiScanState()
    data class Success(
        val networks: List<WifiNetworkInfo>,
        val timestamp: Long = System.currentTimeMillis(),
        val isFresh: Boolean = true
    ) : WifiScanState()
    data class Error(val message: String) : WifiScanState()
}

enum class SpeedTestStage(val label: String) {
    IDLE("Ready"),
    PING("Testing Latency..."),
    DOWNLOAD("Testing Download..."),
    UPLOAD("Testing Upload..."),
    COMPLETE("Test Completed"),
    ERROR("Test Interrupted")
}

data class SpeedTestState(
    val stage: SpeedTestStage = SpeedTestStage.IDLE,
    val currentSpeedMbps: Double = 0.0,
    val downloadSpeedMbps: Double = 0.0,
    val uploadSpeedMbps: Double = 0.0,
    val pingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val progress: Float = 0f, // overall 0.0 to 1.0
    val stageProgress: Float = 0f, // 0.0 to 1.0 for current phase
    val graphPoints: List<Float> = emptyList(),
    val serverName: String = "Fast CDN Server (Auto)",
    val errorMessage: String? = null
) {
    val isRunning: Boolean
        get() = stage == SpeedTestStage.PING || stage == SpeedTestStage.DOWNLOAD || stage == SpeedTestStage.UPLOAD
}

data class SpeedTestResult(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Double,
    val jitterMs: Double,
    val wifiSsid: String,
    val serverName: String = "Global CDN Edge"
)
