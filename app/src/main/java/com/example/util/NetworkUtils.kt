package com.example.util

import com.example.data.model.DeviceType
import com.example.data.model.WifiBand
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

object NetworkUtils {

    fun frequencyToChannel(freqMhz: Int): Int {
        return when {
            freqMhz == 2484 -> 14
            freqMhz in 2412..2472 -> (freqMhz - 2412) / 5 + 1
            freqMhz in 5170..5825 -> (freqMhz - 5000) / 5
            freqMhz in 5925..7125 -> (freqMhz - 5940) / 5
            else -> 0
        }
    }

    fun frequencyToBand(freqMhz: Int): WifiBand {
        return when {
            freqMhz in 2400..2499 -> WifiBand.BAND_2_4_GHZ
            freqMhz in 4900..5899 -> WifiBand.BAND_5_GHZ
            freqMhz >= 5900 -> WifiBand.BAND_6_GHZ
            else -> WifiBand.BAND_2_4_GHZ
        }
    }

    /**
     * Estimates distance using Free Space Path Loss model.
     * FSPL: RSSI = 27.55 - 20*log10(freqMhz) + 20*log10(distance) -> distance = 10^((27.55 - 20*log10(f) + |RSSI|)/20)
     */
    fun calculateDistance(rssi: Int, freqMhz: Int): Double {
        if (freqMhz <= 0 || rssi >= 0) return 0.0
        val exp = (27.55 - (20 * log10(freqMhz.toDouble())) + kotlin.math.abs(rssi)) / 20.0
        val dist = 10.0.pow(exp)
        return (dist * 10.0).roundToInt() / 10.0
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
    }

    fun formatSpeed(bytesPerSec: Long): String {
        val bitsPerSec = bytesPerSec * 8
        return when {
            bytesPerSec < 1024 -> "$bytesPerSec B/s"
            bytesPerSec < 1024 * 1024 -> String.format(Locale.US, "%.1f KB/s", bytesPerSec / 1024.0)
            else -> String.format(Locale.US, "%.2f MB/s", bytesPerSec / (1024.0 * 1024.0))
        }
    }

    fun formatSpeedBits(bytesPerSec: Long): String {
        val bits = bytesPerSec * 8
        return when {
            bits < 1_000_000 -> String.format(Locale.US, "%.1f Kbps", bits / 1000.0)
            else -> String.format(Locale.US, "%.2f Mbps", bits / 1_000_000.0)
        }
    }

    fun formatIpAddress(ipInt: Int): String {
        return String.format(
            Locale.US,
            "%d.%d.%d.%d",
            ipInt and 0xff,
            ipInt shr 8 and 0xff,
            ipInt shr 16 and 0xff,
            ipInt shr 24 and 0xff
        )
    }

    fun guessDeviceType(hostname: String, vendor: String): DeviceType {
        val lower = "${hostname.lowercase(Locale.ROOT)} ${vendor.lowercase(Locale.ROOT)}"
        return when {
            lower.contains("iphone") || lower.contains("pixel") || lower.contains("galaxy") ||
                    lower.contains("android") || lower.contains("mobile") || lower.contains("phone") ||
                    lower.contains("oneplus") || lower.contains("xiaomi") -> DeviceType.PHONE

            lower.contains("macbook") || lower.contains("laptop") || lower.contains("pc") ||
                    lower.contains("desktop") || lower.contains("windows") || lower.contains("dell") ||
                    lower.contains("thinkpad") || lower.contains("hp") || lower.contains("lenovo") -> DeviceType.LAPTOP

            lower.contains("ipad") || lower.contains("tablet") || lower.contains("tab") -> DeviceType.TABLET

            lower.contains("tv") || lower.contains("roku") || lower.contains("firestick") ||
                    lower.contains("chromecast") || lower.contains("bravia") || lower.contains("cast") -> DeviceType.SMART_TV

            lower.contains("playstation") || lower.contains("xbox") || lower.contains("nintendo") ||
                    lower.contains("switch") || lower.contains("ps5") || lower.contains("ps4") -> DeviceType.CONSOLE

            lower.contains("esp") || lower.contains("arduino") || lower.contains("nest") ||
                    lower.contains("sonos") || lower.contains("echo") || lower.contains("alexa") ||
                    lower.contains("bulb") || lower.contains("cam") || lower.contains("shelly") -> DeviceType.IOT

            else -> DeviceType.UNKNOWN
        }
    }

    fun lookupVendor(macAddress: String): String {
        val cleanMac = macAddress.uppercase(Locale.ROOT).replace("-", ":")
        if (cleanMac.length < 8) return "Generic Device"
        val prefix = cleanMac.take(8)
        return when {
            prefix.startsWith("00:1A:11") || prefix.startsWith("F4:F5:D8") || prefix.startsWith("E4:E7:49") -> "Google"
            prefix.startsWith("00:03:93") || prefix.startsWith("AC:BC:32") || prefix.startsWith("3C:22:FB") ||
                    prefix.startsWith("F0:18:98") || prefix.startsWith("A4:83:E7") -> "Apple"
            prefix.startsWith("A4:C3:F0") || prefix.startsWith("BC:D0:74") || prefix.startsWith("50:85:69") -> "Samsung"
            prefix.startsWith("00:26:B9") || prefix.startsWith("18:66:DA") -> "Dell"
            prefix.startsWith("EC:F4:51") || prefix.startsWith("50:C7:BF") || prefix.startsWith("30:DE:4B") -> "TP-Link"
            prefix.startsWith("C0:25:E9") || prefix.startsWith("10:DA:43") -> "Netgear"
            prefix.startsWith("D8:32:14") || prefix.startsWith("70:69:79") -> "Intel"
            prefix.startsWith("60:01:94") || prefix.startsWith("84:F3:EB") -> "Espressif (IoT)"
            prefix.startsWith("B8:27:EB") || prefix.startsWith("DC:A6:32") -> "Raspberry Pi"
            else -> "Network Device"
        }
    }

    /**
     * Reads /proc/net/arp to find connected devices on local network/hotspot interface.
     */
    fun readArpTable(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        try {
            val br = BufferedReader(FileReader("/proc/net/arp"))
            var line: String?
            // Skip header
            br.readLine()
            while (br.readLine().also { line = it } != null) {
                val tokens = line!!.split("\\s+".toRegex())
                if (tokens.size >= 4) {
                    val ip = tokens[0]
                    val flags = tokens[2]
                    val mac = tokens[3]
                    // flag 0x2 is complete entry, mac != 00:00:00:00:00:00
                    if (flags != "0x0" && mac != "00:00:00:00:00:00" && mac.length == 17) {
                        result.add(Pair(ip, mac))
                    }
                }
            }
            br.close()
        } catch (_: Exception) {
            // Permission or missing file on non-root Android
        }
        return result
    }

    fun parseChannelWidth(channelWidth: Int): Int {
        return when (channelWidth) {
            1 -> 40
            2 -> 80
            3 -> 160
            4 -> 160
            else -> 20
        }
    }

    fun getSampleNetworks(): List<com.example.data.model.WifiNetworkInfo> {
        return listOf(
            com.example.data.model.WifiNetworkInfo(
                bssid = "00:1A:11:7C:4B:90",
                ssid = "HomeMesh_Pro_5G",
                rssi = -54,
                frequency = 5240,
                channel = 48,
                band = WifiBand.BAND_5_GHZ,
                channelWidthMhz = 80,
                securityType = "WPA3-Personal",
                distanceMeters = 2.4,
                vendor = "Google",
                isCurrentConnection = true
            ),
            com.example.data.model.WifiNetworkInfo(
                bssid = "00:1A:11:7C:4B:91",
                ssid = "HomeMesh_Pro_2.4G",
                rssi = -48,
                frequency = 2437,
                channel = 6,
                band = WifiBand.BAND_2_4_GHZ,
                channelWidthMhz = 40,
                securityType = "WPA2-PSK",
                distanceMeters = 2.1,
                vendor = "Google",
                isCurrentConnection = false
            ),
            com.example.data.model.WifiNetworkInfo(
                bssid = "EC:F4:51:8A:12:34",
                ssid = "TP-Link_Archer_AX50",
                rssi = -68,
                frequency = 2412,
                channel = 1,
                band = WifiBand.BAND_2_4_GHZ,
                channelWidthMhz = 20,
                securityType = "WPA2-PSK",
                distanceMeters = 5.2,
                vendor = "TP-Link",
                isCurrentConnection = false
            ),
            com.example.data.model.WifiNetworkInfo(
                bssid = "C0:25:E9:99:A1:B2",
                ssid = "NETGEAR-NightHawk",
                rssi = -72,
                frequency = 2462,
                channel = 11,
                band = WifiBand.BAND_2_4_GHZ,
                channelWidthMhz = 20,
                securityType = "WPA2/WPA3",
                distanceMeters = 7.1,
                vendor = "Netgear",
                isCurrentConnection = false
            ),
            com.example.data.model.WifiNetworkInfo(
                bssid = "A4:C3:F0:4D:E5:67",
                ssid = "Office_Guests_Fast",
                rssi = -62,
                frequency = 5180,
                channel = 36,
                band = WifiBand.BAND_5_GHZ,
                channelWidthMhz = 80,
                securityType = "WPA2-PSK",
                distanceMeters = 4.0,
                vendor = "Samsung",
                isCurrentConnection = false
            ),
            com.example.data.model.WifiNetworkInfo(
                bssid = "D8:32:14:1A:99:43",
                ssid = "Starlink_WiFI_Direct",
                rssi = -79,
                frequency = 5745,
                channel = 149,
                band = WifiBand.BAND_5_GHZ,
                channelWidthMhz = 80,
                securityType = "WPA2-PSK",
                distanceMeters = 9.8,
                vendor = "Intel",
                isCurrentConnection = false
            )
        )
    }
}
