package com.example

import com.example.data.model.WifiBand
import com.example.util.NetworkUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testFrequencyToChannelCalculations() {
        assertEquals(1, NetworkUtils.frequencyToChannel(2412))
        assertEquals(6, NetworkUtils.frequencyToChannel(2437))
        assertEquals(11, NetworkUtils.frequencyToChannel(2462))
        assertEquals(36, NetworkUtils.frequencyToChannel(5180))
        assertEquals(48, NetworkUtils.frequencyToChannel(5240))
        assertEquals(149, NetworkUtils.frequencyToChannel(5745))
    }

    @Test
    fun testFrequencyToBand() {
        assertEquals(WifiBand.BAND_2_4_GHZ, NetworkUtils.frequencyToBand(2412))
        assertEquals(WifiBand.BAND_5_GHZ, NetworkUtils.frequencyToBand(5240))
        assertEquals(WifiBand.BAND_6_GHZ, NetworkUtils.frequencyToBand(6100))
    }

    @Test
    fun testFormatBytes() {
        assertEquals("0 B", NetworkUtils.formatBytes(0))
        assertEquals("1.0 KB", NetworkUtils.formatBytes(1024))
        assertEquals("1.5 MB", NetworkUtils.formatBytes((1.5 * 1024 * 1024).toLong()))
        assertEquals("2.0 GB", NetworkUtils.formatBytes((2.0 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testDistanceEstimation() {
        val distStrong = NetworkUtils.calculateDistance(-45, 2412)
        val distWeak = NetworkUtils.calculateDistance(-85, 2412)
        assertTrue(distStrong < distWeak)
        assertTrue(distStrong > 0)
    }

    @Test
    fun testChannelWidthParsing() {
        assertEquals(20, NetworkUtils.parseChannelWidth(0))
        assertEquals(40, NetworkUtils.parseChannelWidth(1))
        assertEquals(80, NetworkUtils.parseChannelWidth(2))
        assertEquals(160, NetworkUtils.parseChannelWidth(3))
        assertEquals(160, NetworkUtils.parseChannelWidth(4))
    }

    @Test
    fun testScanningServiceActions() {
        assertEquals("com.example.service.TRIGGER_SCAN", com.example.service.WifiScanningService.ACTION_TRIGGER_SCAN)
        assertEquals("com.example.service.START_CONTINUOUS", com.example.service.WifiScanningService.ACTION_START_CONTINUOUS)
    }

    @Test
    fun testClientQuotaAndViolationDetection() {
        val clientUnderCap = com.example.data.model.ConnectedClient(
            ipAddress = "192.168.43.10",
            macAddress = "AA:BB:CC:DD:EE:FF",
            hostname = "Laptop",
            totalBytesDownloaded = 500_000_000L,
            totalBytesUploaded = 100_000_000L,
            dataQuotaBytes = 1_000_000_000L
        )
        assertEquals(600_000_000L, clientUnderCap.totalBytes)
        org.junit.Assert.assertFalse(clientUnderCap.isQuotaExceeded)

        val clientOverCap = clientUnderCap.copy(
            totalBytesDownloaded = 900_000_000L,
            totalBytesUploaded = 200_000_000L
        )
        assertEquals(1_100_000_000L, clientOverCap.totalBytes)
        assertTrue(clientOverCap.isQuotaExceeded)
    }

    @Test
    fun testHotspotTelemetryQuotaFraction() {
        val telemetry = com.example.data.model.HotspotTelemetry(
            totalRxBytes = 4_000_000_000L,
            totalTxBytes = 1_000_000_000L,
            dataLimitBytes = 5_000_000_000L
        )
        assertEquals(5_000_000_000L, telemetry.totalBytes)
        assertEquals(1.0f, telemetry.quotaUsageFraction, 0.01f)
    }
}
