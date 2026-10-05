package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TripRequestEntity
import com.example.data.UserRole
import com.example.ui.FleetDashboardUiState
import com.example.ui.components.TripStatusBadge
import com.example.ui.components.VintageAboutAndPrivacyDialog
import com.example.ui.components.VintageAboutPrivacyFooterCard
import com.example.ui.components.VintageNearFullScreenMapPopup
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
import com.example.ui.theme.VintageCardCream
import com.example.ui.theme.VintageCreamBg
import com.example.ui.theme.VintageCrimsonBg
import com.example.ui.theme.VintageCrimsonReject
import com.example.ui.theme.VintageGreenBg
import com.example.ui.theme.VintageGreenSuccess
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder

@Composable
fun SecurityDashboardScreen(
    uiState: FleetDashboardUiState,
    onApproveTrip: (tripId: Int, vehicleName: String, notes: String) -> Unit,
    onRejectTrip: (tripId: Int, vehicleName: String, reason: String) -> Unit,
    onCompleteTrip: (TripRequestEntity) -> Unit,
    onSelectTripOnMap: (Int?) -> Unit,
    onOpenMapPopup: (Int?) -> Unit,
    onCloseMapPopup: () -> Unit,
    onAdvanceManualStep: () -> Unit,
    onLocationPermissionResult: (android.content.Context, Boolean) -> Unit,
    onQuickSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Persetujuan Izin Masuk, 1 = Pantau Peta Real-Time, 2 = Log Pos Keamanan
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    if (uiState.session.isMapPopupOpen) {
        VintageNearFullScreenMapPopup(
            activeTrips = uiState.activeTrips,
            allDestinations = uiState.destinations,
            focusedTripId = uiState.session.focusedMapTripId,
            isGpsEnabled = uiState.session.isGpsPermissionGranted,
            deviceLat = uiState.session.lastDeviceLat,
            deviceLng = uiState.session.lastDeviceLng,
            onSelectTrip = onSelectTripOnMap,
            onCompleteTrip = { trip ->
                onCompleteTrip(trip)
                onCloseMapPopup()
            },
            onAdvanceManualStep = onAdvanceManualStep,
            onLocationPermissionResult = onLocationPermissionResult,
            onDismiss = onCloseMapPopup,
            allTrips = uiState.allTrips
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VintageCreamBg,
        topBar = {
            VintageTopBar(
                title = "Pos Keamanan SR",
                subtitle = "Piket: ${uiState.session.loggedInSecurityOfficer}",
                currentRole = UserRole.SECURITY,
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
                        BadgedBox(
                            badge = {
                                if (uiState.pendingTrips.isNotEmpty()) {
                                    Badge(
                                        containerColor = MetallicGold,
                                        contentColor = DeepInkBrown
                                    ) {
                                        Text("${uiState.pendingTrips.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = "Verifikasi Izin"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Verifikasi Izin",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = vintageNavBarColors(),
                    modifier = Modifier.testTag("security_tab_approvals")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.activeTrips.isNotEmpty()) {
                                    Badge(
                                        containerColor = VintageGreenSuccess,
                                        contentColor = VintageCreamBg
                                    ) {
                                        Text("${uiState.activeTrips.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Peta Live"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Peta Live",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = vintageNavBarColors(),
                    modifier = Modifier.testTag("security_tab_map")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Rekap & Log"
                        )
                    },
                    label = {
                        Text(
                            text = "Rekap & Log",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = vintageNavBarColors(),
                    modifier = Modifier.testTag("security_tab_logs")
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
                0 -> SecurityApprovalQueueTab(
                    pendingTrips = uiState.pendingTrips,
                    activeTrips = uiState.activeTrips,
                    onApproveTrip = { tripId, vehicleName, notes ->
                        onApproveTrip(tripId, vehicleName, notes)
                    },
                    onRejectTrip = onRejectTrip,
                    onViewTripOnMap = { tripId ->
                        onOpenMapPopup(tripId)
                    },
                    onCompleteTrip = onCompleteTrip
                )

                1 -> VintageRealTimeMapPanel(
                    activeTrips = uiState.activeTrips,
                    allDestinations = uiState.destinations,
                    focusedTripId = uiState.session.focusedMapTripId,
                    isGpsEnabled = uiState.session.isGpsPermissionGranted,
                    deviceLat = uiState.session.lastDeviceLat,
                    deviceLng = uiState.session.lastDeviceLng,
                    onSelectTrip = onSelectTripOnMap,
                    onCompleteTrip = onCompleteTrip,
                    onAdvanceManualStep = onAdvanceManualStep,
                    onLocationPermissionResult = onLocationPermissionResult,
                    isInsidePopup = false,
                    onOpenFullPopup = { onOpenMapPopup(uiState.session.focusedMapTripId) },
                    allTrips = uiState.allTrips
                )

                2 -> TripHistoryListTab(
                    trips = uiState.allTrips,
                    onViewOnMap = { tripId ->
                        onOpenMapPopup(tripId)
                    }
                )
            }
        }
    }
}

@Composable
private fun SecurityApprovalQueueTab(
    pendingTrips: List<TripRequestEntity>,
    activeTrips: List<TripRequestEntity>,
    onApproveTrip: (Int, String, String) -> Unit,
    onRejectTrip: (Int, String, String) -> Unit,
    onViewTripOnMap: (Int) -> Unit,
    onCompleteTrip: (TripRequestEntity) -> Unit
) {
    var rejectingTrip by remember { mutableStateOf<TripRequestEntity?>(null) }
    var rejectReasonInput by remember { mutableStateOf("") }

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
            // Security Summary Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, MetallicGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(EspressoBrown, RichLeatherBrown)
                                )
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Meja Verifikasi Izin Keluar Mobil",
                                style = MaterialTheme.typography.titleLarge,
                                color = SoftGoldHighlight
                            )
                            Text(
                                text = "Periksa kelengkapan rencana tujuan pengguna. Jika disetujui, status otomatis 'Dalam Perjalanan' & dipantau di Peta Digital.",
                                style = MaterialTheme.typography.bodySmall,
                                color = VintageCreamBg.copy(alpha = 0.88f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SoftGoldHighlight,
                            border = BorderStroke(1.dp, MetallicGold)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${pendingTrips.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = DeepInkBrown
                                )
                                Text(
                                    text = "Antrean",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                VintageOrnamentalDivider(
                    label = "PERMOHONAN MENUNGGU PERSETUJUAN (${pendingTrips.size})"
                )
            }

            if (pendingTrips.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                        border = BorderStroke(1.dp, VintageWarmBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = AntiqueGold,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tidak Ada Permohonan Tertunda",
                                style = MaterialTheme.typography.titleMedium,
                                color = EspressoBrown
                            )
                            Text(
                                text = "Semua rencana bawa mobil telah diverifikasi. Silakan pantau posisi unit yang sedang berjalan di tab Peta Real-Time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftMochaText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(pendingTrips, key = { "sec_pending_${it.id}" }) { trip ->
                    SecurityApprovalCard(
                        trip = trip,
                        onApprove = { notes ->
                            onApproveTrip(trip.id, trip.vehicleName, notes)
                        },
                        onRejectClick = {
                            rejectReasonInput = "Jadwal operasional padat / Mohon konfirmasi ulang ke bagian GA."
                            rejectingTrip = trip
                        }
                    )
                }
            }

            // Active Vehicles Currently Monitored by Security
            item {
                Spacer(modifier = Modifier.height(6.dp))
                VintageOrnamentalDivider(
                    label = "ARMADA DALAM PERJALANAN (${activeTrips.size})"
                )
            }

            items(activeTrips, key = { "sec_active_${it.id}" }) { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sec_active_trip_card_${trip.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                    border = BorderStroke(1.5.dp, VintageGreenSuccess)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = vehicleIconFor(trip.vehicleId),
                                    contentDescription = trip.vehicleName,
                                    tint = EspressoBrown,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${trip.vehicleName} (${trip.vehiclePlate})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepInkBrown,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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
                            text = "Rute PP: ${trip.fullRouteSummaryText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (trip.isArrivedBackAtSrGate) VintageGreenBg else VintageParchmentSurface,
                            border = BorderStroke(
                                1.dp,
                                if (trip.isArrivedBackAtSrGate) VintageGreenSuccess else VintageWarmBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (trip.isArrivedBackAtSrGate) {
                                        "STATUS GERBANG: Mobil telah kembali di Pos Utama SR • Siap diakhiri Keamanan"
                                    } else {
                                        String.format(
                                            java.util.Locale.US,
                                            "Posisi Live: %.4f, %.4f • Kecepatan: %d km/jam",
                                            trip.currentLat,
                                            trip.currentLng,
                                            trip.currentSpeedKmh
                                        )
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VintageGreenSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = String.format(
                                        java.util.Locale.US,
                                        "Jarak Tempuh Tercatat: %.2f km • Odometer: %d ➔ %d km (%d titik garis rute)",
                                        trip.totalDistanceTraveledKm,
                                        trip.startOdometerKm,
                                        trip.computedEndOdometerKm,
                                        trip.parsedTrailCoordinates.size
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepInkBrown,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onViewTripOnMap(trip.id) },
                                border = BorderStroke(1.2.dp, EspressoBrown),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sec_open_map_btn_${trip.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = EspressoBrown,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Garis Rute Peta", color = EspressoBrown, style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { onCompleteTrip(trip) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (trip.isArrivedBackAtSrGate) VintageGreenSuccess else EspressoBrown,
                                    contentColor = SoftGoldHighlight
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.25f)
                                    .testTag("sec_complete_trip_btn_${trip.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MetallicGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Akhiri Kembali di SR",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Tentang Kami & Privacy Policy Section on Security Dashboard
            item {
                var showDialog by rememberSaveable { mutableStateOf(false) }
                var dialogTab by rememberSaveable { mutableIntStateOf(0) }

                if (showDialog) {
                    VintageAboutAndPrivacyDialog(
                        initialTab = dialogTab,
                        onDismiss = { showDialog = false }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                VintageOrnamentalDivider(label = "TENTANG KAMI & PRIVACY POLICY")
                Spacer(modifier = Modifier.height(10.dp))
                VintageAboutPrivacyFooterCard(
                    onOpenAboutUs = {
                        dialogTab = 0
                        showDialog = true
                    },
                    onOpenPrivacyPolicy = {
                        dialogTab = 1
                        showDialog = true
                    }
                )
            }
        }
    }

    if (rejectingTrip != null) {
        val targetTrip = rejectingTrip!!
        AlertDialog(
            onDismissRequest = { rejectingTrip = null },
            containerColor = VintageCardCream,
            titleContentColor = VintageCrimsonReject,
            title = {
                Text(
                    text = "Tolak Rencana Bawa ${targetTrip.vehicleName}?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pemohon: ${targetTrip.driverName}\nBerikan catatan alasan penolakan dari Pos Keamanan:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        label = { Text("Alasan Penolakan Keamanan") },
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reject_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectTrip(targetTrip.id, targetTrip.vehicleName, rejectReasonInput)
                        rejectingTrip = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VintageCrimsonReject,
                        contentColor = VintageCreamBg
                    ),
                    modifier = Modifier.testTag("confirm_reject_button")
                ) {
                    Text("Tolak Permohonan")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { rejectingTrip = null },
                    border = BorderStroke(1.dp, AntiqueGold)
                ) {
                    Text("Batal", color = EspressoBrown)
                }
            }
        )
    }
}

@Composable
private fun SecurityApprovalCard(
    trip: TripRequestEntity,
    onApprove: (String) -> Unit,
    onRejectClick: () -> Unit
) {
    var officerNotes by remember {
        mutableStateOf("Surat jalan & tujuan terverifikasi. Disetujui keluar Pos Utama SR.")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("security_pending_card_${trip.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = VintageCardCream),
        border = BorderStroke(2.dp, AntiqueGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EspressoBrown),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vehicleIconFor(trip.vehicleId),
                            contentDescription = trip.vehicleName,
                            tint = MetallicGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${trip.vehicleName} • ${trip.vehiclePlate}",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepInkBrown,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Odometer Awal: ${trip.startOdometerKm} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftMochaText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                TripStatusBadge(status = trip.tripStatusEnum)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Applicant Details
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = VintageParchmentSurface,
                border = BorderStroke(1.dp, VintageWarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = EspressoBrown,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pemohon: ${trip.driverName} (${trip.driverDivision})",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepInkBrown
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keperluan: ${trip.purpose}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepInkBrown
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SoftMochaText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rencana Jam: ${trip.departureEstimate} s/d ${trip.returnEstimate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftMochaText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DAFTAR TUJUAN BEPERGIAN (${trip.parsedDestinations.size} LOKASI):",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    trip.parsedDestinations.forEachIndexed { idx, dest ->
                        Text(
                            text = "  ${idx + 1}. $dest",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepInkBrown,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = officerNotes,
                onValueChange = { officerNotes = it },
                label = { Text("Catatan Verifikasi Pos Keamanan") },
                singleLine = true,
                colors = vintageTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_notes_input_${trip.id}")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRejectClick,
                    border = BorderStroke(1.5.dp, VintageCrimsonReject),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VintageCrimsonReject
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reject_trip_button_${trip.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Tolak",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tolak Izin",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = { onApprove(officerNotes) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VintageGreenSuccess,
                        contentColor = VintageCreamBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("approve_trip_button_${trip.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Setujui",
                        tint = SoftGoldHighlight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Setujui (Jalan)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
