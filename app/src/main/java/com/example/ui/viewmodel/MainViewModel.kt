package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChannelRating
import com.example.data.model.ConnectedClient
import com.example.data.model.CurrentWifiConnection
import com.example.data.model.DailyUsageSummary
import com.example.data.model.DeviceType
import com.example.data.model.HotspotTelemetry
import com.example.data.model.PingResult
import com.example.data.model.SpeedDataPoint
import com.example.data.model.SubnetHost
import com.example.data.model.WifiBand
import com.example.data.model.WifiNetworkInfo
import com.example.data.model.WifiScanState
import com.example.data.repository.NetworkScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NetworkSortOrder {
    SIGNAL_STRENGTH,
    SSID,
    CHANNEL
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NetworkScannerRepository(application, viewModelScope)

    val currentConnection: StateFlow<CurrentWifiConnection> = repository.currentConnection
    val scanState: StateFlow<WifiScanState> = repository.scanState
    val channelRatings24: StateFlow<List<ChannelRating>> = repository.channelRatings24
    val channelRatings5: StateFlow<List<ChannelRating>> = repository.channelRatings5
    val hotspotTelemetry: StateFlow<HotspotTelemetry> = repository.hotspotTelemetry
    val connectedClients: StateFlow<List<ConnectedClient>> = repository.connectedClients
    val speedHistory: StateFlow<List<SpeedDataPoint>> = repository.speedHistory
    val dailyUsageHistory: StateFlow<List<DailyUsageSummary>> = repository.dailyUsageHistory

    // Filter and Sort states for WiFi Scanner
    private val _selectedBandFilter = MutableStateFlow<WifiBand?>(null) // null = ALL
    val selectedBandFilter: StateFlow<WifiBand?> = _selectedBandFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(NetworkSortOrder.SIGNAL_STRENGTH)
    val sortOrder: StateFlow<NetworkSortOrder> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered & sorted scan list
    val filteredNetworks: StateFlow<List<WifiNetworkInfo>> = combine(
        repository.scannedNetworks,
        _selectedBandFilter,
        _sortOrder,
        _searchQuery
    ) { networks, bandFilter, sort, query ->
        var result = networks
        if (bandFilter != null) {
            result = result.filter { it.band == bandFilter }
        }
        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            result = result.filter { it.ssid.lowercase().contains(q) || it.bssid.lowercase().contains(q) }
        }
        when (sort) {
            NetworkSortOrder.SIGNAL_STRENGTH -> result.sortedByDescending { it.rssi }
            NetworkSortOrder.SSID -> result.sortedBy { it.ssid.lowercase() }
            NetworkSortOrder.CHANNEL -> result.sortedBy { it.channel }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ping diagnostic test state
    private val _pingResult = MutableStateFlow<PingResult?>(null)
    val pingResult: StateFlow<PingResult?> = _pingResult.asStateFlow()

    private val _isPinging = MutableStateFlow(false)
    val isPinging: StateFlow<Boolean> = _isPinging.asStateFlow()

    // Subnet scan state
    private val _subnetHosts = MutableStateFlow<List<SubnetHost>>(emptyList())
    val subnetHosts: StateFlow<List<SubnetHost>> = _subnetHosts.asStateFlow()

    private val _isSubnetScanning = MutableStateFlow(false)
    val isSubnetScanning: StateFlow<Boolean> = _isSubnetScanning.asStateFlow()

    // Dialog state for editing client
    private val _clientToEdit = MutableStateFlow<ConnectedClient?>(null)
    val clientToEdit: StateFlow<ConnectedClient?> = _clientToEdit.asStateFlow()

    // Dialog state for setting hotspot quota
    private val _showQuotaDialog = MutableStateFlow(false)
    val showQuotaDialog: StateFlow<Boolean> = _showQuotaDialog.asStateFlow()

    fun setBandFilter(band: WifiBand?) {
        _selectedBandFilter.value = band
    }

    fun setSortOrder(order: NetworkSortOrder) {
        _sortOrder.value = order
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun triggerScan() {
        repository.triggerScan()
    }

    fun openEditClient(client: ConnectedClient) {
        _clientToEdit.value = client
    }

    fun closeEditClient() {
        _clientToEdit.value = null
    }

    fun saveClientDetails(
        macAddress: String,
        alias: String,
        deviceType: DeviceType,
        quotaGb: Double?,
        isBlocked: Boolean
    ) {
        viewModelScope.launch {
            val quotaBytes = quotaGb?.let { (it * 1024.0 * 1024.0 * 1024.0).toLong() }
            repository.saveClientSetting(macAddress, alias, deviceType, quotaBytes, isBlocked)
            closeEditClient()
        }
    }

    fun setHotspotDataLimit(limitGb: Double, isEnabled: Boolean) {
        viewModelScope.launch {
            val limitBytes = (limitGb * 1024.0 * 1024.0 * 1024.0).toLong()
            repository.setHotspotDataLimit(limitBytes, isEnabled)
            _showQuotaDialog.value = false
        }
    }

    fun openQuotaDialog() {
        _showQuotaDialog.value = true
    }

    fun closeQuotaDialog() {
        _showQuotaDialog.value = false
    }

    fun resetSessionUsage() {
        viewModelScope.launch {
            repository.resetSessionUsage()
        }
    }

    fun runPing(targetHost: String = "8.8.8.8") {
        viewModelScope.launch {
            _isPinging.value = true
            _pingResult.value = repository.runPingTest(targetHost)
            _isPinging.value = false
        }
    }

    fun scanSubnet() {
        viewModelScope.launch {
            _isSubnetScanning.value = true
            _subnetHosts.value = repository.scanSubnet()
            _isSubnetScanning.value = false
        }
    }

    private val _isScanningClients = MutableStateFlow(false)
    val isScanningClients: StateFlow<Boolean> = _isScanningClients.asStateFlow()

    fun refreshClients() {
        viewModelScope.launch {
            _isScanningClients.value = true
            repository.scanAndDiscoverClients()
            _isScanningClients.value = false
        }
    }
}
