package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DestinationEntity
import com.example.data.FleetRepository
import com.example.data.TripRequestEntity
import com.example.data.TripStatus
import com.example.data.UserRole
import com.example.data.VehicleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AppSessionState(
    val currentRole: UserRole = UserRole.NONE,
    val loggedInUserName: String = "Bapak Andi Pratama",
    val loggedInUserDivision: String = "Divisi Operasional & Logistik",
    val loggedInSecurityOfficer: String = "Komandan Pos Suryo",
    val isTripFormOpen: Boolean = false,
    val isMapPopupOpen: Boolean = false,
    val preselectedVehicleId: String? = null,
    val focusedMapTripId: Int? = null,
    val isGpsPermissionGranted: Boolean = false,
    val lastDeviceLat: Double? = null,
    val lastDeviceLng: Double? = null,
    val bannerMessage: String? = null
)

data class FleetDashboardUiState(
    val session: AppSessionState = AppSessionState(),
    val vehicles: List<VehicleEntity> = emptyList(),
    val destinations: List<DestinationEntity> = emptyList(),
    val allTrips: List<TripRequestEntity> = emptyList(),
    val pendingTrips: List<TripRequestEntity> = emptyList(),
    val activeTrips: List<TripRequestEntity> = emptyList(),
    val historyTrips: List<TripRequestEntity> = emptyList()
)

class FleetViewModel(
    private val repository: FleetRepository
) : ViewModel() {

    private val _session = MutableStateFlow(AppSessionState())
    val session: StateFlow<AppSessionState> = _session.asStateFlow()

    private var telemetryLoopJob: Job? = null
    private var locationManager: LocationManager? = null
    private var activeLocationListener: LocationListener? = null

    val uiState: StateFlow<FleetDashboardUiState> = combine(
        _session,
        repository.vehicles,
        repository.destinations,
        repository.tripRequests
    ) { sessionState, vehicles, destinations, trips ->
        val pending = trips.filter { it.tripStatusEnum == TripStatus.PENDING_APPROVAL }
        val active = trips.filter { it.tripStatusEnum == TripStatus.IN_TRANSIT }
        val history = trips.filter {
            it.tripStatusEnum == TripStatus.COMPLETED || it.tripStatusEnum == TripStatus.REJECTED
        }
        FleetDashboardUiState(
            session = sessionState,
            vehicles = vehicles,
            destinations = destinations,
            allTrips = trips,
            pendingTrips = pending,
            activeTrips = active,
            historyTrips = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetDashboardUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeedData()
        }
        startLiveMapTelemetryLoop()
    }

    private fun startLiveMapTelemetryLoop() {
        telemetryLoopJob?.cancel()
        telemetryLoopJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2400L)
                repository.advanceActiveTripsTelemetry(1f)
            }
        }
    }

    fun loginAsUserMonitor(userName: String, division: String) {
        val cleanName = userName.trim().ifEmpty { "Pengguna Mobil SR" }
        val cleanDiv = division.trim().ifEmpty { "Operasional Umum" }
        _session.update {
            it.copy(
                currentRole = UserRole.USER_MONITOR,
                loggedInUserName = cleanName,
                loggedInUserDivision = cleanDiv,
                isTripFormOpen = false,
                bannerMessage = "Selamat datang, $cleanName! Anda masuk sebagai Pengguna & Pemantau."
            )
        }
    }

    fun loginAsSecurity(officerName: String) {
        val cleanOfficer = officerName.trim().ifEmpty { "Komandan Pos Suryo" }
        _session.update {
            it.copy(
                currentRole = UserRole.SECURITY,
                loggedInSecurityOfficer = cleanOfficer,
                isTripFormOpen = false,
                bannerMessage = "Pos Keamanan Aktif — Petugas: $cleanOfficer"
            )
        }
    }

    fun quickSwitchRole() {
        _session.update { current ->
            val nextRole = when (current.currentRole) {
                UserRole.USER_MONITOR -> UserRole.SECURITY
                UserRole.SECURITY -> UserRole.USER_MONITOR
                UserRole.NONE -> UserRole.USER_MONITOR
            }
            val msg = if (nextRole == UserRole.SECURITY) {
                "Beralih ke Halaman Keamanan (${current.loggedInSecurityOfficer})"
            } else {
                "Beralih ke Halaman Pengguna & Pemantau (${current.loggedInUserName})"
            }
            current.copy(
                currentRole = nextRole,
                isTripFormOpen = false,
                bannerMessage = msg
            )
        }
    }

    fun logout() {
        _session.update {
            it.copy(
                currentRole = UserRole.NONE,
                isTripFormOpen = false,
                bannerMessage = null
            )
        }
    }

    fun openTripPlanForm(vehicleId: String? = null) {
        _session.update {
            it.copy(
                isTripFormOpen = true,
                preselectedVehicleId = vehicleId
            )
        }
    }

    fun closeTripPlanForm() {
        _session.update {
            it.copy(
                isTripFormOpen = false,
                preselectedVehicleId = null
            )
        }
    }

    fun focusTripOnMap(tripId: Int?) {
        _session.update {
            it.copy(focusedMapTripId = tripId)
        }
    }

    fun openMapPopup(tripId: Int? = null) {
        _session.update {
            it.copy(
                isMapPopupOpen = true,
                focusedMapTripId = tripId ?: it.focusedMapTripId
            )
        }
    }

    fun closeMapPopup() {
        _session.update {
            it.copy(isMapPopupOpen = false)
        }
    }

    fun clearBannerMessage() {
        _session.update { it.copy(bannerMessage = null) }
    }

    fun addCustomDestination(
        name: String,
        addressCategory: String,
        latitude: Double,
        longitude: Double,
        onCreated: (DestinationEntity) -> Unit = {}
    ) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val created = repository.addCustomDestination(
                name = name,
                addressCategory = addressCategory,
                latitude = latitude,
                longitude = longitude
            )
            launch(Dispatchers.Main) {
                onCreated(created)
                _session.update {
                    it.copy(bannerMessage = "Tujuan baru '${created.name}' berhasil ditambahkan di peta OpenStreetMap.")
                }
            }
        }
    }

    fun submitTripPlan(
        vehicle: VehicleEntity,
        driverName: String,
        driverDivision: String,
        purpose: String,
        selectedDestinations: List<DestinationEntity>,
        departureEstimate: String,
        returnEstimate: String,
        startOdometerKm: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitTripPlan(
                vehicle = vehicle,
                driverName = driverName,
                driverDivision = driverDivision,
                purpose = purpose,
                selectedDestinations = selectedDestinations,
                departureEstimate = departureEstimate,
                returnEstimate = returnEstimate,
                startOdometerKm = startOdometerKm
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        isTripFormOpen = false,
                        preselectedVehicleId = null,
                        bannerMessage = "Rencana bawa ${vehicle.name} terkirim ke Pos Keamanan! Menunggu persetujuan."
                    )
                }
                onSuccess()
            }
        }
    }

    fun approveTripRequest(tripId: Int, vehicleName: String, notes: String) {
        val officer = _session.value.loggedInSecurityOfficer
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveTrip(tripId = tripId, officerName = officer, notes = notes)
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        focusedMapTripId = tripId,
                        bannerMessage = "Disetujui! Status $vehicleName kini 'Dalam Perjalanan' & dipantau di Peta Real-Time."
                    )
                }
            }
        }
    }

    fun rejectTripRequest(tripId: Int, vehicleName: String, reason: String) {
        val officer = _session.value.loggedInSecurityOfficer
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectTrip(tripId = tripId, officerName = officer, reason = reason)
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        bannerMessage = "Pengajuan rencana bawa $vehicleName telah ditolak."
                    )
                }
            }
        }
    }

    fun completeActiveTrip(tripId: Int, vehicleName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeTrip(
                tripId = tripId,
                completionNote = "Kendaraan telah kembali ke Pos Utama SR."
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        bannerMessage = "Perjalanan $vehicleName selesai. Unit kini kembali Tersedia di Garasi."
                    )
                }
            }
        }
    }

    fun advanceSingleStepManual() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.advanceActiveTripsTelemetry(2.2f)
        }
    }

    @SuppressLint("MissingPermission")
    fun onLocationPermissionResult(context: Context, granted: Boolean) {
        _session.update { it.copy(isGpsPermissionGranted = granted) }
        if (!granted) return

        try {
            val mgr = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
            locationManager = mgr

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val lat = location.latitude
                    val lng = location.longitude
                    val speedKmh = ((location.speed * 3.6f).toInt()).coerceAtLeast(15)
                    _session.update {
                        it.copy(
                            lastDeviceLat = lat,
                            lastDeviceLng = lng
                        )
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.updateLiveDeviceGpsForDriver(
                            driverName = _session.value.loggedInUserName,
                            lat = lat,
                            lng = lng,
                            speedKmh = speedKmh
                        )
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            activeLocationListener = listener

            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            for (provider in providers) {
                if (mgr.isProviderEnabled(provider)) {
                    mgr.requestLocationUpdates(provider, 4000L, 5f, listener)
                    val lastKnown = mgr.getLastKnownLocation(provider)
                    if (lastKnown != null) {
                        _session.update {
                            it.copy(
                                lastDeviceLat = lastKnown.latitude,
                                lastDeviceLng = lastKnown.longitude
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore in environments without physical GPS hardware
        }
    }

    override fun onCleared() {
        super.onCleared()
        telemetryLoopJob?.cancel()
        try {
            activeLocationListener?.let { locationManager?.removeUpdates(it) }
        } catch (_: Exception) {
        }
    }

    class Factory(private val repository: FleetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FleetViewModel(repository) as T
        }
    }
}
