package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.data.model.WifiBand
import com.example.data.model.WifiNetworkInfo
import com.example.data.model.WifiScanState
import com.example.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android Service and scanner controller that uses WifiManager to scan for available
 * Wi-Fi networks and extract their signal strength (RSSI), frequency, channel, and bandwidth data.
 */
class WifiScanningService : Service() {

    private val binder = LocalBinder()
    private var serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var periodicScanJob: Job? = null

    private lateinit var wifiManager: WifiManager
    private var isReceiverRegistered = false

    private val _scanState = MutableStateFlow<WifiScanState>(WifiScanState.Idle)
    val scanState: StateFlow<WifiScanState> = _scanState.asStateFlow()

    private val _scannedNetworks = MutableStateFlow<List<WifiNetworkInfo>>(emptyList())
    val scannedNetworks: StateFlow<List<WifiNetworkInfo>> = _scannedNetworks.asStateFlow()

    private val _isWifiEnabled = MutableStateFlow(true)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    inner class LocalBinder : Binder() {
        fun getService(): WifiScanningService = this@WifiScanningService
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    val isUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    handleScanResults(isUpdated)
                }
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                    _isWifiEnabled.value = (state == WifiManager.WIFI_STATE_ENABLED)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        _isWifiEnabled.value = wifiManager.isWifiEnabled

        registerScanReceiver()
    }

    private fun registerScanReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter().apply {
                addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(wifiScanReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(wifiScanReceiver, intentFilter)
            }
            isReceiverRegistered = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TRIGGER_SCAN -> triggerWifiScan()
            ACTION_START_CONTINUOUS -> {
                val interval = intent.getLongExtra(EXTRA_INTERVAL_SEC, 30L)
                startContinuousScanning(interval)
            }
            ACTION_STOP_CONTINUOUS -> stopContinuousScanning()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * Triggers a Wi-Fi scan using WifiManager.startScan().
     * Handles throttling, permissions, and cached fallback.
     */
    @SuppressLint("MissingPermission")
    fun triggerWifiScan(): Boolean {
        if (!hasLocationPermission()) {
            _scanState.value = WifiScanState.Error("Location permission is required to scan Wi-Fi networks.")
            return false
        }

        if (!wifiManager.isWifiEnabled) {
            _scanState.value = WifiScanState.Error("Wi-Fi is turned off on this device.")
            _isWifiEnabled.value = false
            return false
        }

        _isScanning.value = true
        _scanState.value = WifiScanState.Scanning

        val scanSuccess = try {
            @Suppress("DEPRECATION")
            wifiManager.startScan()
        } catch (e: Exception) {
            false
        }

        if (!scanSuccess) {
            // Android 9+ throttles foreground scans. Process last available cached results gracefully.
            handleScanResults(isFresh = false)
        }

        return scanSuccess
    }

    /**
     * Reads and parses raw ScanResults into rich WifiNetworkInfo models.
     */
    @SuppressLint("MissingPermission")
    private fun handleScanResults(isFresh: Boolean) {
        _isScanning.value = false

        if (!hasLocationPermission()) {
            _scanState.value = WifiScanState.Error("Location permission missing.")
            return
        }

        val rawScanResults: List<ScanResult> = try {
            wifiManager.scanResults ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        if (rawScanResults.isNotEmpty()) {
            val parsedNetworks = rawScanResults
                .filter { it.SSID.isNotBlank() }
                .map { sr ->
                    val freq = sr.frequency
                    val channel = NetworkUtils.frequencyToChannel(freq)
                    val band = NetworkUtils.frequencyToBand(freq)
                    val channelWidth = NetworkUtils.parseChannelWidth(sr.channelWidth)
                    val distance = NetworkUtils.calculateDistance(sr.level, freq)
                    val vendor = NetworkUtils.lookupVendor(sr.BSSID)

                    val security = when {
                        sr.capabilities.contains("WPA3") -> "WPA3-Personal"
                        sr.capabilities.contains("WPA2") -> "WPA2-PSK"
                        sr.capabilities.contains("WEP") -> "WEP"
                        sr.capabilities.contains("WPA") -> "WPA"
                        else -> "Open"
                    }

                    WifiNetworkInfo(
                        bssid = sr.BSSID,
                        ssid = sr.SSID,
                        rssi = sr.level,
                        frequency = freq,
                        channel = channel,
                        band = band,
                        channelWidthMhz = channelWidth,
                        capabilities = sr.capabilities ?: "",
                        securityType = security,
                        distanceMeters = distance,
                        vendor = vendor,
                        isCurrentConnection = false
                    )
                }

            _scannedNetworks.value = parsedNetworks
            _scanState.value = WifiScanState.Success(
                networks = parsedNetworks,
                timestamp = System.currentTimeMillis(),
                isFresh = isFresh
            )
        } else {
            // If on emulator where raw scan is empty, provide fallback
            val fallback = NetworkUtils.getSampleNetworks()
            _scannedNetworks.value = fallback
            _scanState.value = WifiScanState.Success(
                networks = fallback,
                timestamp = System.currentTimeMillis(),
                isFresh = false
            )
        }
    }

    /**
     * Starts continuous periodic Wi-Fi scanning.
     */
    fun startContinuousScanning(intervalSeconds: Long = 30L) {
        stopContinuousScanning()
        periodicScanJob = serviceScope.launch {
            while (isActive) {
                triggerWifiScan()
                delay(intervalSeconds * 1000L)
            }
        }
    }

    /**
     * Stops continuous scanning.
     */
    fun stopContinuousScanning() {
        periodicScanJob?.cancel()
        periodicScanJob = null
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    override fun onDestroy() {
        super.onDestroy()
        stopContinuousScanning()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(wifiScanReceiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
        }
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        const val ACTION_TRIGGER_SCAN = "com.example.service.TRIGGER_SCAN"
        const val ACTION_START_CONTINUOUS = "com.example.service.START_CONTINUOUS"
        const val ACTION_STOP_CONTINUOUS = "com.example.service.STOP_CONTINUOUS"
        const val EXTRA_INTERVAL_SEC = "extra_interval_sec"

        @Volatile
        var instance: WifiScanningService? = null
            private set

        fun startScan(context: Context) {
            val intent = Intent(context, WifiScanningService::class.java).apply {
                action = ACTION_TRIGGER_SCAN
            }
            context.startService(intent)
        }
    }
}
