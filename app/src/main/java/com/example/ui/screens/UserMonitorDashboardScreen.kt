package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DestinationEntity
import com.example.data.TripRequestEntity
import com.example.data.TripStatus
import com.example.data.UserRole
import com.example.data.VehicleEntity
import com.example.ui.FleetDashboardUiState
import com.example.ui.components.TripStatusBadge
import com.example.ui.components.VintageNotificationBanner
import com.example.ui.components.VintageOrnamentalDivider
import com.example.ui.components.VintageRealTimeMapPanel
import com.example.ui.components.VintageTopBar
import com.example.ui.components.vehicleIconFor
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepInkBrown
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RichLeatherBrown
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftMochaText
import com.example.ui.theme.VintageAmberBg
import com.example.ui.theme.VintageAmberPending
import com.example.ui.theme.VintageCardCream
import com.example.ui.theme.VintageCreamBg
import com.example.ui.theme.VintageGreenBg
import com.example.ui.theme.VintageGreenSuccess
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder

@Composable
fun UserMonitorDashboardScreen(
    uiState: FleetDashboardUiState,
    onOpenTripForm: (String?) -> Unit,
    onSelectTripOnMap: (Int?) -> Unit,
    onCompleteTrip: (TripRequestEntity) -> Unit,
    onAdvanceManualStep: () -> Unit,
    onLocationPermissionResult: (android.content.Context, Boolean) -> Unit,
    onQuickSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Rencana & Armada, 1 = Pantau Peta Live, 2 = Riwayat Perjalanan
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VintageCreamBg,
        topBar = {
            VintageTopBar(
                title = "Pengguna & Pemantau SR",
                subtitle = "${uiState.session.loggedInUserName} • ${uiState.session.loggedInUserDivision}",
                currentRole = UserRole.USER_MONITOR,
                pendingCountForBadge = uiState.pendingTrips.size,
                onQuickSwitchRole = onQuickSwitchRole,
                onLogout = onLogout
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = EspressoBrown,
                contentColor = SoftGoldHighlight,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Rencana & Armada"
                        )
                    },
                    label = { Text("Rencana & Armada") },
                    colors = vintageNavBarColors(),
                    modifier = Modifier.testTag("user_tab_fleet")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.activeTrips.isNotEmpty()) {
                                    Badge(
                                        containerColor = MetallicGold,
                                        contentColor = DeepInkBrown
                                    ) {
                                        Text("${uiState.activeTrips.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Pantau Peta Live"
                            )
                        }
                    },
                    label = { Text("Pantau Peta Live") },
                    colors = vintageNavBarColors(),
                    modifier = Modifier.testTag("user_tab_live_map")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Riwayat"
                        )
                    },
                    label = { Text("Riwayat Log") },
                    colors = vintageNavBarColors(),
                    modifier = Modifier.testTag("user_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.session.bannerMessage != null) {
                VintageNotificationBanner(
                    message = uiState.session.bannerMessage,
                    onDismiss = onDismissBanner
                )
            }

            when (selectedTab) {
                0 -> UserFleetAndPlanTab(
                    vehicles = uiState.vehicles,
                    allTrips = uiState.allTrips,
                    pendingTrips = uiState.pendingTrips,
                    activeTrips = uiState.activeTrips,
                    onOpenTripForm = onOpenTripForm,
                    onOpenLiveMapForTrip = { tripId ->
                        onSelectTripOnMap(tripId)
                        selectedTab = 1
                    },
                    onCompleteTrip = onCompleteTrip,
                    onQuickSwitchToSecurity = onQuickSwitchRole
                )

                1 -> VintageRealTimeMapPanel(
                    activeTrips = uiState.activeTrips,
                    allDestinations = uiState.destinations,
                    focusedTripId = uiState.session.focusedMapTripId,
                    isGpsEnabled = uiState.session.isGpsPermissionGranted,
                    onSelectTrip = onSelectTripOnMap,
                    onCompleteTrip = onCompleteTrip,
                    onAdvanceManualStep = onAdvanceManualStep,
                    onLocationPermissionResult = onLocationPermissionResult
                )

                2 -> TripHistoryListTab(
                    trips = uiState.allTrips,
                    onViewOnMap = { tripId ->
                        onSelectTripOnMap(tripId)
                        selectedTab = 1
                    }
                )
            }
        }
    }
}

@Composable
private fun UserFleetAndPlanTab(
    vehicles: List<VehicleEntity>,
    allTrips: List<TripRequestEntity>,
    pendingTrips: List<TripRequestEntity>,
    activeTrips: List<TripRequestEntity>,
    onOpenTripForm: (String?) -> Unit,
    onOpenLiveMapForTrip: (Int) -> Unit,
    onCompleteTrip: (TripRequestEntity) -> Unit,
    onQuickSwitchToSecurity: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Prominent CTA Card: Tombol Formulir Isi Rencana Bawa Mobil
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, MetallicGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(EspressoBrown, RichLeatherBrown)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftGoldHighlight.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, MetallicGold)
                                ) {
                                    Text(
                                        text = "PENGAJUAN IZIN KELUAR ARMADA",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftGoldHighlight,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = MetallicGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Rencana Bawa Mobil SR",
                                style = MaterialTheme.typography.headlineMedium,
                                color = SoftGoldHighlight
                            )
                            Text(
                                text = "Pilih unit (Gran Max, N-Max, atau Avanza Veloz), pilih/tambah tujuan bepergian, lalu kirim ke Pos Keamanan untuk disetujui.",
                                style = MaterialTheme.typography.bodySmall,
                                color = VintageCreamBg.copy(alpha = 0.9f),
                                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                            )

                            Button(
                                onClick = { onOpenTripForm(null) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MetallicGold,
                                    contentColor = DeepInkBrown
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_trip_form_cta_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = DeepInkBrown
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Formulir Isi Rencana Bawa Mobil",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepInkBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Status Ketersediaan 3 Unit Kendaraan (Gran Max, N-Max, Avanza Veloz)
            item {
                VintageOrnamentalDivider(label = "STATUS 3 UNIT ARMADA SAAT INI")
            }

            items(vehicles, key = { it.id }) { vehicle ->
                val activeTrip = activeTrips.firstOrNull { it.vehicleId == vehicle.id }
                val pendingTrip = pendingTrips.firstOrNull { it.vehicleId == vehicle.id }

                VehicleStatusOverviewCard(
                    vehicle = vehicle,
                    activeTrip = activeTrip,
                    pendingTrip = pendingTrip,
                    onSelectForPlan = { onOpenTripForm(vehicle.id) },
                    onTrackOnMap = { tripId -> onOpenLiveMapForTrip(tripId) }
                )
            }

            // Active Trips in Transit Section
            if (activeTrips.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    VintageOrnamentalDivider(label = "SEDANG DALAM PERJALANAN (${activeTrips.size})")
                }

                items(activeTrips, key = { "active_${it.id}" }) { trip ->
                    ActiveOrPendingTripCard(
                        trip = trip,
                        onViewMap = { onOpenLiveMapForTrip(trip.id) },
                        onCompleteTrip = { onCompleteTrip(trip) },
                        onSwitchToSecurity = onQuickSwitchToSecurity
                    )
                }
            }

            // Pending Approval Section
            if (pendingTrips.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    VintageOrnamentalDivider(label = "MENUNGGU PERSETUJUAN KEAMANAN (${pendingTrips.size})")
                }

                items(pendingTrips, key = { "pending_${it.id}" }) { trip ->
                    ActiveOrPendingTripCard(
                        trip = trip,
                        onViewMap = {},
                        onCompleteTrip = {},
                        onSwitchToSecurity = onQuickSwitchToSecurity
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleStatusOverviewCard(
    vehicle: VehicleEntity,
    activeTrip: TripRequestEntity?,
    pendingTrip: TripRequestEntity?,
    onSelectForPlan: () -> Unit,
    onTrackOnMap: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fleet_unit_card_${vehicle.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VintageCardCream),
        border = BorderStroke(1.5.dp, AntiqueGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EspressoBrown),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vehicleIconFor(vehicle.id),
                            contentDescription = vehicle.name,
                            tint = MetallicGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vehicle.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepInkBrown
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = VintageParchmentSurface,
                                border = BorderStroke(1.dp, AntiqueGold)
                            ) {
                                Text(
                                    text = vehicle.plateNumber,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoBrown,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${vehicle.category} • BBM ${vehicle.fuelLevelPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )
                    }
                }

                // Availability Badge
                when {
                    activeTrip != null -> TripStatusBadge(status = TripStatus.IN_TRANSIT)
                    pendingTrip != null -> TripStatusBadge(status = TripStatus.PENDING_APPROVAL)
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = VintageGreenBg,
                            border = BorderStroke(1.dp, VintageGreenSuccess)
                        ) {
                            Text(
                                text = "Tersedia di Pos",
                                style = MaterialTheme.typography.labelSmall,
                                color = VintageGreenSuccess,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                activeTrip != null -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VintageParchmentSurface,
                        border = BorderStroke(1.dp, VintageWarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dibawa oleh: ${activeTrip.driverName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DeepInkBrown,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tujuan: ${activeTrip.parsedDestinations.joinToString(" → ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftMochaText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onTrackOnMap(activeTrip.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EspressoBrown,
                                    contentColor = SoftGoldHighlight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("track_unit_btn_${vehicle.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Pantau",
                                    tint = MetallicGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lihat Peta", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                pendingTrip != null -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VintageAmberBg,
                        border = BorderStroke(1.dp, AntiqueGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Diajukan oleh: ${pendingTrip.driverName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DeepInkBrown,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Menunggu verifikasi & persetujuan Pos Keamanan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VintageAmberPending
                                )
                            }
                        }
                    }
                }

                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = vehicle.capacityInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )
                        OutlinedButton(
                            onClick = onSelectForPlan,
                            border = BorderStroke(1.2.dp, EspressoBrown),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("book_unit_btn_${vehicle.id}")
                        ) {
                            Text(
                                text = "Pakai ${vehicle.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = EspressoBrown
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveOrPendingTripCard(
    trip: TripRequestEntity,
    onViewMap: () -> Unit,
    onCompleteTrip: () -> Unit,
    onSwitchToSecurity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_card_${trip.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VintageCardCream),
        border = BorderStroke(1.5.dp, AntiqueGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = vehicleIconFor(trip.vehicleId),
                        contentDescription = trip.vehicleName,
                        tint = EspressoBrown,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${trip.vehicleName} (${trip.vehiclePlate})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepInkBrown
                    )
                }
                TripStatusBadge(status = trip.tripStatusEnum)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SoftMochaText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${trip.driverName} • ${trip.driverDivision}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepInkBrown,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Keperluan: ${trip.purpose}",
                style = MaterialTheme.typography.bodySmall,
                color = SoftMochaText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = VintageParchmentSurface,
                border = BorderStroke(1.dp, VintageWarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "RUTE TUJUAN BEPERGIAN:",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    trip.parsedDestinations.forEachIndexed { idx, destName ->
                        Text(
                            text = "${idx + 1}. $destName",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepInkBrown,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estimasi Waktu: ${trip.departureEstimate} s/d ${trip.returnEstimate} • Odo Awal: ${trip.startOdometerKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftMochaText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (trip.tripStatusEnum == TripStatus.IN_TRANSIT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onViewMap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EspressoBrown,
                            contentColor = SoftGoldHighlight
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("view_realtime_map_btn_${trip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MetallicGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pantau di Peta Live", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onCompleteTrip,
                        border = BorderStroke(1.5.dp, VintageGreenSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("finish_trip_btn_${trip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VintageGreenSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Selesai",
                            style = MaterialTheme.typography.labelMedium,
                            color = VintageGreenSuccess
                        )
                    }
                }
            } else if (trip.tripStatusEnum == TripStatus.PENDING_APPROVAL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Data telah dikirim ke Pos Keamanan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VintageAmberPending,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onSwitchToSecurity,
                        border = BorderStroke(1.dp, AntiqueGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EspressoBrown,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Buka Keamanan",
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoBrown
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripHistoryListTab(
    trips: List<TripRequestEntity>,
    onViewOnMap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                VintageOrnamentalDivider(label = "DAFTAR SELURUH LOG PERJALANAN (${trips.size})")
            }

            items(trips, key = { it.id }) { trip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                    border = BorderStroke(1.dp, VintageWarmBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${trip.vehicleName} • ${trip.vehiclePlate}",
                                style = MaterialTheme.typography.titleMedium,
                                color = EspressoBrown
                            )
                            TripStatusBadge(status = trip.tripStatusEnum)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pembawa: ${trip.driverName} (${trip.driverDivision})",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepInkBrown,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tujuan: ${trip.parsedDestinations.joinToString(" → ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )
                        Text(
                            text = "Keperluan: ${trip.purpose}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )
                        if (trip.securityNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VintageParchmentSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Catatan Keamanan (${trip.approvedByOfficer.ifBlank { "Pos SR" }}): ${trip.securityNotes}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoBrown,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        if (trip.tripStatusEnum == TripStatus.IN_TRANSIT) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onViewOnMap(trip.id) },
                                border = BorderStroke(1.dp, EspressoBrown),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = EspressoBrown,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lihat Posisi di Peta Real-Time", color = EspressoBrown)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun vintageNavBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = DeepInkBrown,
    selectedTextColor = SoftGoldHighlight,
    indicatorColor = MetallicGold,
    unselectedIconColor = VintageCreamBg.copy(alpha = 0.7f),
    unselectedTextColor = VintageCreamBg.copy(alpha = 0.7f)
)
