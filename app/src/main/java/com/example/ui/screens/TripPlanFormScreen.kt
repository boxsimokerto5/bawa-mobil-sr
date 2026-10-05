package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.data.DestinationEntity
import com.example.data.FleetRepository
import com.example.data.TripRequestEntity
import com.example.data.TripStatus
import com.example.data.VehicleEntity
import com.example.ui.components.OsmPlaceSearchService
import com.example.ui.components.OsmPlaceSuggestion
import com.example.ui.components.OsmTileStore
import com.example.ui.components.VintageOrnamentalDivider
import com.example.ui.components.WebMercator
import com.example.ui.components.vehicleIconFor
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepInkBrown
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.MetallicGold
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripPlanFormScreen(
    vehicles: List<VehicleEntity>,
    destinations: List<DestinationEntity>,
    allTrips: List<TripRequestEntity>,
    preselectedVehicleId: String?,
    defaultDriverName: String,
    defaultDriverDivision: String,
    onAddCustomDestination: (String, String, Double, Double, (DestinationEntity) -> Unit) -> Unit,
    onSubmitPlan: (
        vehicle: VehicleEntity,
        driverName: String,
        driverDivision: String,
        purpose: String,
        selectedDestinations: List<DestinationEntity>,
        departureEstimate: String,
        returnEstimate: String,
        startOdometerKm: Int
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onCancel()
    }

    val initialVehicle = remember(vehicles, preselectedVehicleId) {
        vehicles.firstOrNull { it.id == preselectedVehicleId }
            ?: vehicles.firstOrNull { v ->
                allTrips.none {
                    it.vehicleId == v.id && (it.tripStatusEnum == TripStatus.IN_TRANSIT || it.tripStatusEnum == TripStatus.PENDING_APPROVAL)
                }
            }
            ?: vehicles.firstOrNull()
    }

    var selectedVehicle by remember(initialVehicle) { mutableStateOf(initialVehicle) }
    var driverName by remember { mutableStateOf(defaultDriverName) }
    var driverDivision by remember { mutableStateOf(defaultDriverDivision) }
    var purpose by remember { mutableStateOf("Operasional Pengiriman & Kunjungan Kerja") }
    var departureEstimate by remember { mutableStateOf("09:00 WIB") }
    var returnEstimate by remember { mutableStateOf("15:00 WIB") }
    var startOdometer by remember(selectedVehicle) {
        val defaultOdo = when (selectedVehicle?.id) {
            "VH_GRANMAX" -> "68210"
            "VH_NMAX" -> "15435"
            else -> "42850"
        }
        mutableStateOf(defaultOdo)
    }

    val selectedDestinations = remember {
        mutableStateListOf<DestinationEntity>().apply {
            destinations.firstOrNull()?.let { add(it) }
        }
    }

    var showAddDestinationDialog by remember { mutableStateOf(false) }
    var newDestName by remember { mutableStateOf("") }
    var newDestAddress by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VintageCreamBg)
    ) {
        // Form Top Header Bar
        Surface(
            color = EspressoBrown,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("close_trip_form_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = SoftGoldHighlight
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Formulir Isi Rencana Bawa Mobil",
                        style = MaterialTheme.typography.titleLarge,
                        color = SoftGoldHighlight
                    )
                    Text(
                        text = "Lengkapi data & tujuan untuk dikirim ke Pos Keamanan",
                        style = MaterialTheme.typography.bodySmall,
                        color = VintageCreamBg.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                // Step 1: Pilih Unit Armada (Gran Max, N-Max, Avanza Veloz)
                VintageOrnamentalDivider(label = "1. PILIH UNIT KENDARAAN SR")
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    vehicles.forEach { vehicle ->
                        val activeTripForVehicle = allTrips.firstOrNull {
                            it.vehicleId == vehicle.id &&
                                (it.tripStatusEnum == TripStatus.IN_TRANSIT || it.tripStatusEnum == TripStatus.PENDING_APPROVAL)
                        }
                        val isChosen = selectedVehicle?.id == vehicle.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVehicle = vehicle }
                                .testTag("select_vehicle_${vehicle.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChosen) EspressoBrown else VintageCardCream
                            ),
                            border = BorderStroke(
                                width = if (isChosen) 2.dp else 1.dp,
                                color = if (isChosen) MetallicGold else VintageWarmBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isChosen) MetallicGold else VintageParchmentSurface
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = vehicleIconFor(vehicle.id),
                                            contentDescription = vehicle.name,
                                            tint = if (isChosen) DeepInkBrown else EspressoBrown,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = vehicle.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (isChosen) SoftGoldHighlight else DeepInkBrown
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isChosen) SoftGoldHighlight.copy(alpha = 0.2f) else VintageParchmentSurface,
                                                border = BorderStroke(1.dp, AntiqueGold)
                                            ) {
                                                Text(
                                                    text = vehicle.plateNumber,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isChosen) SoftGoldHighlight else EspressoBrown,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = vehicle.capacityInfo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isChosen) VintageCreamBg.copy(alpha = 0.85f) else SoftMochaText
                                        )
                                        if (activeTripForVehicle != null) {
                                            Text(
                                                text = "Catatan: Saat ini sedang ${if (activeTripForVehicle.tripStatusEnum == TripStatus.IN_TRANSIT) "dibawa ${activeTripForVehicle.driverName}" else "diajukan ${activeTripForVehicle.driverName}"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isChosen) SoftGoldHighlight else AntiqueGold
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (activeTripForVehicle == null) VintageGreenBg else VintageParchmentSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (activeTripForVehicle == null) VintageGreenSuccess else AntiqueGold
                                    )
                                ) {
                                    Text(
                                        text = if (activeTripForVehicle == null) "Siap Pakai" else "Terjadwal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (activeTripForVehicle == null) VintageGreenSuccess else EspressoBrown,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step 2: Pilih & Tambah Tujuan Bepergian (Multi-Stop Route)
                VintageOrnamentalDivider(label = "2. PILIH / TAMBAH TUJUAN BEPERGIAN")
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                    border = BorderStroke(1.5.dp, AntiqueGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daftar Tujuan Perjalanan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = EspressoBrown
                                )
                                Text(
                                    text = "Boleh pilih lebih dari 1 tujuan berurutan atau tambah tujuan baru.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftMochaText
                                )
                            }

                            OutlinedButton(
                                onClick = { showAddDestinationDialog = true },
                                border = BorderStroke(1.5.dp, EspressoBrown),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("add_custom_destination_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddLocationAlt,
                                    contentDescription = "Tambah Tujuan",
                                    tint = EspressoBrown,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+ Tambah Tujuan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EspressoBrown
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected Ordered Route Preview
                        if (selectedDestinations.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftGoldHighlight.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, AntiqueGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "URUTAN RUTE TERPILIH (${selectedDestinations.size} TITIK):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EspressoBrown,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    selectedDestinations.forEachIndexed { idx, dest ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(EspressoBrown),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${idx + 1}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = SoftGoldHighlight,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = dest.name,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = DeepInkBrown
                                                    )
                                                    Text(
                                                        text = dest.addressCategory,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = SoftMochaText,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { selectedDestinations.remove(dest) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hapus tujuan",
                                                    tint = VintageCrimsonReject,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = "KETUK UNTUK MEMILIH / MENAMBAHKAN KE RUTE:",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftMochaText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            destinations.forEach { dest ->
                                val orderIndex = selectedDestinations.indexOfFirst { it.id == dest.id }
                                val isSelected = orderIndex >= 0
                                Surface(
                                    onClick = {
                                        if (isSelected) {
                                            selectedDestinations.removeAll { it.id == dest.id }
                                        } else {
                                            selectedDestinations.add(dest)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) EspressoBrown else VintageParchmentSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MetallicGold else VintageWarmBorder
                                    ),
                                    modifier = Modifier.testTag("dest_chip_${dest.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                            contentDescription = dest.name,
                                            tint = if (isSelected) MetallicGold else EspressoBrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isSelected) "${orderIndex + 1}. ${dest.name}" else dest.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) SoftGoldHighlight else DeepInkBrown
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step 3: Detail Pembawa & Jadwal Keperluan
                VintageOrnamentalDivider(label = "3. DATA PEMBAWA & KEPERLUAN")
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                    border = BorderStroke(1.5.dp, AntiqueGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Nama Pembawa Mobil / Penanggung Jawab") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = EspressoBrown)
                            },
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_driver_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = driverDivision,
                            onValueChange = { driverDivision = it },
                            label = { Text("Bagian / Divisi") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = EspressoBrown)
                            },
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_driver_division_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = purpose,
                            onValueChange = { purpose = it },
                            label = { Text("Keperluan / Rencana Kegiatan") },
                            leadingIcon = {
                                Icon(Icons.Default.Assignment, contentDescription = null, tint = EspressoBrown)
                            },
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_purpose_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = departureEstimate,
                                onValueChange = { departureEstimate = it },
                                label = { Text("Jam Berangkat") },
                                leadingIcon = {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = EspressoBrown)
                                },
                                singleLine = true,
                                colors = vintageTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = returnEstimate,
                                onValueChange = { returnEstimate = it },
                                label = { Text("Estimasi Kembali") },
                                leadingIcon = {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = EspressoBrown)
                                },
                                singleLine = true,
                                colors = vintageTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = startOdometer,
                            onValueChange = { startOdometer = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Kilometer Awal (Odometer KM)") },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = EspressoBrown)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VintageCrimsonBg,
                        border = BorderStroke(1.dp, VintageCrimsonReject),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = VintageCrimsonReject,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit to Security Button
                Button(
                    onClick = {
                        val chosenVehicle = selectedVehicle
                        when {
                            chosenVehicle == null -> {
                                validationError = "Pilih salah satu unit kendaraan terlebih dahulu."
                            }
                            selectedDestinations.isEmpty() -> {
                                validationError = "Pilih minimal 1 tujuan bepergian atau tambahkan tujuan baru."
                            }
                            driverName.isBlank() || purpose.isBlank() -> {
                                validationError = "Nama pembawa mobil dan keperluan wajib diisi lengkap."
                            }
                            else -> {
                                validationError = null
                                onSubmitPlan(
                                    chosenVehicle,
                                    driverName,
                                    driverDivision,
                                    purpose,
                                    selectedDestinations.toList(),
                                    departureEstimate,
                                    returnEstimate,
                                    startOdometer.toIntOrNull() ?: 40000
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EspressoBrown,
                        contentColor = SoftGoldHighlight
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_trip_plan_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = MetallicGold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Kirim Rencana ke Halaman Keamanan",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Modal Dialog to Add Custom Destination with Real OpenStreetMap Search Auto-Suggest & Pin Picker
    if (showAddDestinationDialog) {
        val context = LocalContext.current
        var pickedLat by remember { mutableDoubleStateOf(-7.2819) }
        var pickedLng by remember { mutableDoubleStateOf(112.7382) }
        var pickerZoom by remember { mutableFloatStateOf(14.5f) }
        val smoothPickerZoom by animateFloatAsState(
            targetValue = pickerZoom,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            label = "picker_zoom"
        )
        val animatedPickedLat by animateFloatAsState(
            targetValue = pickedLat.toFloat(),
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            label = "picker_lat"
        )
        val animatedPickedLng by animateFloatAsState(
            targetValue = pickedLng.toFloat(),
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            label = "picker_lng"
        )

        var isPinnedFromSuggestion by remember { mutableStateOf(false) }
        var isSearchingPlaces by remember { mutableStateOf(false) }
        var showSuggestionsDropdown by remember { mutableStateOf(true) }
        var suggestions by remember {
            mutableStateOf(OsmPlaceSearchService.getInstantLocalSuggestions(""))
        }

        // Live auto-suggest as the user types in Nama Tempat / Tujuan Baru
        LaunchedEffect(newDestName) {
            val query = newDestName.trim()
            suggestions = OsmPlaceSearchService.getInstantLocalSuggestions(query)
            if (query.length >= 2) {
                isSearchingPlaces = true
                delay(260L)
                val merged = OsmPlaceSearchService.searchPlacesWithNominatim(query)
                suggestions = merged
                isSearchingPlaces = false
            } else {
                isSearchingPlaces = false
            }
        }

        // Prefetch OSM tiles around the currently pinned coordinate
        val discretePickerZoom = smoothPickerZoom.toInt().coerceIn(10, 17)
        LaunchedEffect(discretePickerZoom, (pickedLat * 100).toInt(), (pickedLng * 100).toInt()) {
            val cx = floor(WebMercator.lonToTileX(pickedLng, discretePickerZoom)).toInt()
            val cy = floor(WebMercator.latToTileY(pickedLat, discretePickerZoom)).toInt()
            OsmTileStore.prefetchRegion(context, discretePickerZoom, cx, cy, radius = 2)
        }

        val textMeasurer = rememberTextMeasurer()
        val dialogScrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { showAddDestinationDialog = false },
            containerColor = VintageCardCream,
            titleContentColor = EspressoBrown,
            textContentColor = DeepInkBrown,
            title = {
                Text(
                    text = "Tambah Tujuan Baru (OpenStreetMap)",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(dialogScrollState)
                ) {
                    Text(
                        text = "Ketik nama tujuan (misal: Rumah Sakit SLG / RS SLG) untuk melihat usulan otomatis, atau geser Peta Jalan Asli (OpenStreetMap) di bawah:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Interactive OpenStreetMap Picker with Marked Destination Pin & Callout Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(195.dp)
                            .testTag("custom_destination_osm_picker_map"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = 1.8.dp,
                            color = if (isPinnedFromSuggestion) VintageGreenSuccess else AntiqueGold
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF2E9D8))
                                    .pointerInput(Unit) {
                                        detectTransformGestures(panZoomLock = false) { centroid, pan, zoomChange, _ ->
                                            val w = size.width.toFloat()
                                            val h = size.height.toFloat()
                                            val oldZ = pickerZoom
                                            val oldZInt = oldZ.toInt().coerceIn(10, 17)
                                            val oldScale = 2.0.pow((oldZ - oldZInt).toDouble())
                                            val oldTileSize = WebMercator.TILE_SIZE * oldScale

                                            val cTx = WebMercator.lonToTileX(pickedLng, oldZInt)
                                            val cTy = WebMercator.latToTileY(pickedLat, oldZInt)

                                            val pannedTx = cTx - (pan.x / oldTileSize)
                                            val pannedTy = cTy - (pan.y / oldTileSize)

                                            if (zoomChange != 1f && w > 0f && h > 0f) {
                                                val dxPx = (centroid.x - w / 2f).toDouble()
                                                val dyPx = (centroid.y - h / 2f).toDouble()
                                                val focalTx = pannedTx + dxPx / oldTileSize
                                                val focalTy = pannedTy + dyPx / oldTileSize
                                                val focalLon = WebMercator.tileXToLon(focalTx, oldZInt)
                                                val focalLat = WebMercator.tileYToLat(focalTy, oldZInt)

                                                val zoomDelta = (ln(zoomChange.toDouble()) / ln(2.0)).toFloat()
                                                val newZ = (oldZ + zoomDelta).coerceIn(10.5f, 17.5f)
                                                pickerZoom = newZ

                                                val newZInt = newZ.toInt().coerceIn(10, 17)
                                                val newScale = 2.0.pow((newZ - newZInt).toDouble())
                                                val newTileSize = WebMercator.TILE_SIZE * newScale

                                                val focalNewTx = WebMercator.lonToTileX(focalLon, newZInt)
                                                val focalNewTy = WebMercator.latToTileY(focalLat, newZInt)

                                                pickedLng = WebMercator.tileXToLon(
                                                    focalNewTx - dxPx / newTileSize,
                                                    newZInt
                                                ).coerceIn(-179.9, 179.9)
                                                pickedLat = WebMercator.tileYToLat(
                                                    focalNewTy - dyPx / newTileSize,
                                                    newZInt
                                                ).coerceIn(-80.0, 80.0)
                                            } else if (pan != Offset.Zero) {
                                                pickedLng = WebMercator.tileXToLon(pannedTx, oldZInt).coerceIn(-179.9, 179.9)
                                                pickedLat = WebMercator.tileYToLat(pannedTy, oldZInt).coerceIn(-80.0, 80.0)
                                            }
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                pickerZoom = (pickerZoom + 1.0f).coerceAtMost(17.5f)
                                            },
                                            onTap = { tapOffset ->
                                                val w = size.width.toFloat()
                                                val h = size.height.toFloat()
                                                val zInt = smoothPickerZoom.toInt().coerceIn(10, 17)
                                                val scale = 2.0.pow((smoothPickerZoom - zInt).toDouble())
                                                val effTileSize = WebMercator.TILE_SIZE * scale
                                                val cTx = WebMercator.lonToTileX(pickedLng, zInt)
                                                val cTy = WebMercator.latToTileY(pickedLat, zInt)
                                                val tappedTx = cTx + (tapOffset.x - w / 2f) / effTileSize
                                                val tappedTy = cTy + (tapOffset.y - h / 2f) / effTileSize
                                                pickedLng = WebMercator.tileXToLon(tappedTx, zInt)
                                                pickedLat = WebMercator.tileYToLat(tappedTy, zInt)
                                                isPinnedFromSuggestion = true
                                            }
                                        )
                                    }
                            ) {
                                val _rev = OsmTileStore.tileRevision.intValue
                                val w = size.width
                                val h = size.height
                                val zInt = smoothPickerZoom.toInt().coerceIn(10, 17)
                                val scale = 2f.pow(smoothPickerZoom - zInt)
                                val tileSizePx = WebMercator.TILE_SIZE.toFloat() * scale

                                val renderLat = animatedPickedLat.toDouble()
                                val renderLng = animatedPickedLng.toDouble()
                                val cTx = WebMercator.lonToTileX(renderLng, zInt)
                                val cTy = WebMercator.latToTileY(renderLat, zInt)
                                val baseTx = floor(cTx).toInt()
                                val baseTy = floor(cTy).toInt()

                                for (tx in (baseTx - 2)..(baseTx + 2)) {
                                    for (ty in (baseTy - 2)..(baseTy + 2)) {
                                        val left = (w / 2f + (tx - cTx) * tileSizePx).roundToInt()
                                        val top = (h / 2f + (ty - cTy) * tileSizePx).roundToInt()
                                        val right = (w / 2f + (tx + 1 - cTx) * tileSizePx).roundToInt()
                                        val bottom = (h / 2f + (ty + 1 - cTy) * tileSizePx).roundToInt()
                                        val tileSpec = OsmTileStore.getTileOrFallback(context, zInt, tx, ty)
                                        if (tileSpec != null) {
                                            drawImage(
                                                image = tileSpec.imageBitmap,
                                                srcOffset = IntOffset(tileSpec.srcLeft, tileSpec.srcTop),
                                                srcSize = IntSize(tileSpec.srcWidth, tileSpec.srcHeight),
                                                dstOffset = IntOffset(left, top),
                                                dstSize = IntSize(
                                                    (right - left).coerceAtLeast(1),
                                                    (bottom - top).coerceAtLeast(1)
                                                ),
                                                filterQuality = FilterQuality.Medium
                                            )
                                        }
                                    }
                                }

                                // Draw Prominent Destination Pin Marker + Label Callout on Map
                                val centerPt = Offset(w / 2f, h / 2f)
                                val pinColor = if (isPinnedFromSuggestion) VintageGreenSuccess else EspressoBrown

                                // Ground shadow & radar halo
                                drawCircle(
                                    color = pinColor.copy(alpha = 0.22f),
                                    radius = 28f,
                                    center = centerPt
                                )
                                // Pin teardrop pointer
                                val pinPath = Path().apply {
                                    moveTo(centerPt.x, centerPt.y)
                                    lineTo(centerPt.x - 13f, centerPt.y - 20f)
                                    lineTo(centerPt.x + 13f, centerPt.y - 20f)
                                    close()
                                }
                                drawPath(path = pinPath, color = pinColor)
                                drawCircle(
                                    color = pinColor,
                                    radius = 15f,
                                    center = Offset(centerPt.x, centerPt.y - 24f)
                                )
                                drawCircle(
                                    color = MetallicGold,
                                    radius = 10f,
                                    center = Offset(centerPt.x, centerPt.y - 24f)
                                )
                                drawCircle(
                                    color = DeepInkBrown,
                                    radius = 4.5f,
                                    center = Offset(centerPt.x, centerPt.y - 24f)
                                )

                                // Draw Destination Title Tag right above the pin when named
                                val labelText = newDestName.trim().ifEmpty { "Titik Tujuan Baru" }
                                val shortLabel = if (labelText.length > 28) labelText.take(26) + "…" else labelText
                                val measuredLabel = textMeasurer.measure(
                                    text = "📍 $shortLabel",
                                    style = TextStyle(
                                        color = SoftGoldHighlight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                val tagW = measuredLabel.size.width + 16f
                                val tagH = measuredLabel.size.height + 8f
                                val tagTopLeft = Offset(
                                    x = (centerPt.x - tagW / 2f).coerceIn(6f, (w - tagW - 6f).coerceAtLeast(6f)),
                                    y = (centerPt.y - 46f - tagH).coerceAtLeast(6f)
                                )
                                drawRoundRect(
                                    color = pinColor.copy(alpha = 0.95f),
                                    topLeft = tagTopLeft,
                                    size = Size(tagW, tagH),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                                drawRoundRect(
                                    color = MetallicGold,
                                    topLeft = tagTopLeft,
                                    size = Size(tagW, tagH),
                                    cornerRadius = CornerRadius(8f, 8f),
                                    style = Stroke(width = 1.5f)
                                )
                                drawText(
                                    textLayoutResult = measuredLabel,
                                    topLeft = Offset(tagTopLeft.x + 8f, tagTopLeft.y + 4f)
                                )
                            }

                            // Mini Zoom Controls on Right Side of Picker Map
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    onClick = { pickerZoom = (pickerZoom + 0.8f).coerceAtMost(17.5f) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = VintageCardCream.copy(alpha = 0.92f),
                                    border = BorderStroke(1.dp, AntiqueGold),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Perbesar",
                                            tint = EspressoBrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Surface(
                                    onClick = { pickerZoom = (pickerZoom - 0.8f).coerceAtLeast(10.5f) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = VintageCardCream.copy(alpha = 0.92f),
                                    border = BorderStroke(1.dp, AntiqueGold),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Perkecil",
                                            tint = EspressoBrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(6.dp),
                                shape = RoundedCornerShape(50),
                                color = if (isPinnedFromSuggestion) VintageGreenSuccess.copy(alpha = 0.94f) else EspressoBrown.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, MetallicGold)
                            ) {
                                Text(
                                    text = String.format(
                                        java.util.Locale.US,
                                        "%s: %.4f, %.4f",
                                        if (isPinnedFromSuggestion) "Ditandai sebagai Tujuan" else "Titik OSM",
                                        pickedLat,
                                        pickedLng
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftGoldHighlight,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search / Place Name Input with Auto-Suggest Trigger
                    OutlinedTextField(
                        value = newDestName,
                        onValueChange = {
                            newDestName = it
                            showSuggestionsDropdown = true
                        },
                        label = { Text("Nama Tempat / Tujuan Baru (Ketik untuk Usulan)") },
                        placeholder = { Text("Contoh: Rumah Sakit SLG / RS SLG / Bandara") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = EspressoBrown
                            )
                        },
                        trailingIcon = {
                            if (isSearchingPlaces) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = EspressoBrown
                                )
                            } else if (newDestName.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        newDestName = ""
                                        newDestAddress = ""
                                        isPinnedFromSuggestion = false
                                        showSuggestionsDropdown = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Bersihkan",
                                        tint = SoftMochaText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_destination_name_input")
                    )

                    // Live Auto-Suggest Dropdown List (Usulan Lokasi OpenStreetMap)
                    AnimatedVisibility(visible = showSuggestionsDropdown && suggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .testTag("osm_place_suggestions_list"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
                            border = BorderStroke(1.2.dp, AntiqueGold)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (newDestName.isBlank()) {
                                            "USULAN LOKASI POPULER (KETUK UNTUK TANDAI DI PETA):"
                                        } else {
                                            "USULAN SESUAI PENCARIAN (${suggestions.size} LOKASI):"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EspressoBrown,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tutup",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftMochaText,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { showSuggestionsDropdown = false }
                                    )
                                }

                                suggestions.take(5).forEachIndexed { idx, item ->
                                    Surface(
                                        onClick = {
                                            newDestName = item.title
                                            newDestAddress = item.addressSubtitle
                                            pickedLat = item.latitude
                                            pickedLng = item.longitude
                                            pickerZoom = 15.6f
                                            isPinnedFromSuggestion = true
                                            showSuggestionsDropdown = false
                                        },
                                        color = Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("osm_suggestion_item_$idx")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(EspressoBrown),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = MetallicGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = item.title,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = DeepInkBrown,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = SoftGoldHighlight.copy(alpha = 0.65f),
                                                        border = BorderStroke(0.8.dp, AntiqueGold)
                                                    ) {
                                                        Text(
                                                            text = item.categoryBadge,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = EspressoBrown,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = item.addressSubtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SoftMochaText,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDestAddress,
                        onValueChange = { newDestAddress = it },
                        label = { Text("Alamat / Patokan Area (Otomatis / Opsional)") },
                        placeholder = { Text("Contoh: Jl. Galuh Candrakirana, Ngasem, Kediri") },
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_destination_address_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDestName.isNotBlank()) {
                            val nameToCreate = newDestName
                            val addrToCreate = newDestAddress
                            val finalLat = pickedLat
                            val finalLng = pickedLng
                            newDestName = ""
                            newDestAddress = ""
                            showAddDestinationDialog = false
                            onAddCustomDestination(
                                nameToCreate,
                                addrToCreate,
                                finalLat,
                                finalLng
                            ) { createdEntity ->
                                if (selectedDestinations.none { it.id == createdEntity.id }) {
                                    selectedDestinations.add(createdEntity)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EspressoBrown,
                        contentColor = SoftGoldHighlight
                    ),
                    modifier = Modifier.testTag("confirm_add_destination_button")
                ) {
                    Text("Simpan & Pilih Tujuan")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddDestinationDialog = false },
                    border = BorderStroke(1.dp, AntiqueGold)
                ) {
                    Text("Batal", color = EspressoBrown)
                }
            }
        )
    }
}
