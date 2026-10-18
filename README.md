# WiFiPulse - Android WiFi Analyzer & Hotspot Tracker

**WiFiPulse** is a comprehensive Android application engineered for advanced wireless network analysis, real-time spectrum monitoring, and local Wi-Fi hotspot client bandwidth usage tracking. Built natively with Kotlin and Jetpack Compose, WiFiPulse provides granular telemetry, visual throughput graphs, and robust network diagnostic tools.

## Features

### 1. WiFi Analyzer & Spectrum
- **Current Connection Hub:** Displays live SSID, BSSID, IP address, Gateway, DNS, link speed, frequency band, and security standards.
- **Signal Strength Gauge:** Animated circular meter plotting RSSI (-100 to -30 dBm) with color-coded quality tiers (Excellent, Good, Fair, Weak).
- **Parabolic Spectrum Graph:** Custom Canvas rendering signal curves for 2.4 GHz, 5 GHz, and 6 GHz bands to visualize channel overlap and bandwidth spread.
- **Optimal Channel Ratings:** 5-star scoring algorithm identifying the least-congested channels with a "Best Pick" recommendation badge.

### 2. Hotspot & Connected Client Tracker
- **Hotspot Session Overview:** Monitors active network interfaces (ap0/wlan0), gateway IP, and total session download/upload bytes.
- **Data Quota Management:** Configurable session data cap with animated progress bars and 85%/100% threshold warning alerts.
- **Connected Client Monitor:** Discovers connected devices via ARP table parsing and active subnet scanning, identifying hostnames, IPs, MAC addresses, and OUI vendors.
- **Per-Device Data & Speed Tracking:** Tracks live download/upload speeds (KB/s, MB/s) and cumulative data consumption per client.
- **Quota & Alias Management:** Assign custom device aliases, set individual data caps with violation badges, or toggle access restriction.

### 3. Data Visualizations & Analytics
- **Client Distribution Donut Chart:** Visualizes each connected device's percentage share of total hotspot bandwidth.
- **Live Throughput Line Chart:** Real-time 30-second scrolling graph plotting download and upload speeds with cubic bezier curves.
- **Weekly Consumption Bar Chart:** 7-day historical breakdown of download versus upload data volumes.

### 4. Network Diagnostics & Tools
- **Ping & Latency Tester:** Measures latency, packet loss percentage, and hop round-trip times to Gateway, Google DNS (8.8.8.8), or Cloudflare (1.1.1.1).
- **LAN Subnet Scanner:** Probes active devices on the local network segment.
- **Local Persistence:** Room Database implementation storing client aliases, device types, and quota preferences locally.

## Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose & Material 3
- **Architecture:** MVVM (Model-View-ViewModel) with Kotlin Coroutines & Flow
- **Local Storage:** Room Persistence Library
- **Networking & Scanning:** WifiManager, ConnectivityManager, Kernel ARP Parsing

## Getting Started
1. Clone the repository: `git clone https://github.com/lutfurenvoy-cpu/wifi-analyzer-hotspot-tracker.git`
2. Open the project in Android Studio (Jellyfish or newer recommended).
3. Sync Gradle dependencies and build the project.
4. Deploy to a physical Android device to utilize live Wi-Fi scanning and hotspot tracking capabilities.
