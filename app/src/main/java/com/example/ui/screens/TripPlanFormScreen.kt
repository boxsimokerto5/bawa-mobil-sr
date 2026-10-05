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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.data.DestinationEntity
import com.example.data.FleetRepository
import com.example.data.TripRequestEntity
import com.example.data.TripStatus
import com.example.data.VehicleEntity
import com.example.ui.components.OsmTileStore
import com.example.ui.components.VintageOrnamentalDivider
import com.example.ui.components.WebMercator
import com.example.ui.components.vehicleIconFor
import kotlin.math.floor
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

    // Modal Dialog to Add Custom Destination with Real OpenStreetMap Picker
    if (showAddDestinationDialog) {
        val context = LocalContext.current
        var pickedLat by remember { mutableDoubleStateOf(-7.2819) }
        var pickedLng by remember { mutableDoubleStateOf(112.7382) }
        val pickerZoom = 13

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
                Column {
                    Text(
                        text = "Ketuk atau geser Peta Jalan Asli (OpenStreetMap) di bawah untuk menentukan titik koordinat tujuan baru:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Interactive Mini OpenStreetMap Picker
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, AntiqueGold)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF2E9D8))
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val cTx = WebMercator.lonToTileX(pickedLng, pickerZoom)
                                            val cTy = WebMercator.latToTileY(pickedLat, pickerZoom)
                                            pickedLng = WebMercator.tileXToLon(
                                                cTx - dragAmount.x / WebMercator.TILE_SIZE,
                                                pickerZoom
                                            )
                                            pickedLat = WebMercator.tileYToLat(
                                                cTy - dragAmount.y / WebMercator.TILE_SIZE,
                                                pickerZoom
                                            )
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures { tapOffset ->
                                            val w = size.width.toFloat()
                                            val h = size.height.toFloat()
                                            val cTx = WebMercator.lonToTileX(pickedLng, pickerZoom)
                                            val cTy = WebMercator.latToTileY(pickedLat, pickerZoom)
                                            val tappedTx = cTx + (tapOffset.x - w / 2f) / WebMercator.TILE_SIZE
                                            val tappedTy = cTy + (tapOffset.y - h / 2f) / WebMercator.TILE_SIZE
                                            pickedLng = WebMercator.tileXToLon(tappedTx, pickerZoom)
                                            pickedLat = WebMercator.tileYToLat(tappedTy, pickerZoom)
                                        }
                                    }
                            ) {
                                val _rev = OsmTileStore.tileRevision.intValue
                                val w = size.width
                                val h = size.height
                                val tileSizePx = WebMercator.TILE_SIZE.toFloat()
                                val cTx = WebMercator.lonToTileX(pickedLng, pickerZoom)
                                val cTy = WebMercator.latToTileY(pickedLat, pickerZoom)
                                val baseTx = floor(cTx).toInt()
                                val baseTy = floor(cTy).toInt()

                                for (tx in (baseTx - 2)..(baseTx + 2)) {
                                    for (ty in (baseTy - 2)..(baseTy + 2)) {
                                        val left = (w / 2f + (tx - cTx) * tileSizePx).roundToInt()
                                        val top = (h / 2f + (ty - cTy) * tileSizePx).roundToInt()
                                        val right = (w / 2f + (tx + 1 - cTx) * tileSizePx).roundToInt()
                                        val bottom = (h / 2f + (ty + 1 - cTy) * tileSizePx).roundToInt()
                                        val tileSpec = OsmTileStore.getTileOrFallback(context, pickerZoom, tx, ty)
                                        if (tileSpec != null) {
                                            drawImage(
                                                image = tileSpec.imageBitmap,
                                                srcOffset = IntOffset(tileSpec.srcLeft, tileSpec.srcTop),
                                                srcSize = IntSize(tileSpec.srcWidth, tileSpec.srcHeight),
                                                dstOffset = IntOffset(left, top),
                                                dstSize = IntSize(
                                                    (right - left).coerceAtLeast(1),
                                                    (bottom - top).coerceAtLeast(1)
                                                )
                                            )
                                        }
                                    }
                                }

                                // Draw Center Target Pin on Map
                                val centerPt = Offset(w / 2f, h / 2f)
                                drawCircle(
                                    color = EspressoBrown.copy(alpha = 0.25f),
                                    radius = 24f,
                                    center = centerPt
                                )
                                drawCircle(
                                    color = MetallicGold,
                                    radius = 12f,
                                    center = centerPt
                                )
                                drawCircle(
                                    color = EspressoBrown,
                                    radius = 7f,
                                    center = centerPt
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(6.dp),
                                shape = RoundedCornerShape(50),
                                color = EspressoBrown.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, MetallicGold)
                            ) {
                                Text(
                                    text = String.format(
                                        java.util.Locale.US,
                                        "Titik OSM: %.4f, %.4f",
                                        pickedLat,
                                        pickedLng
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftGoldHighlight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newDestName,
                        onValueChange = { newDestName = it },
                        label = { Text("Nama Tempat / Tujuan Baru") },
                        placeholder = { Text("Contoh: Pabrik Cabang Sidoarjo") },
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_destination_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDestAddress,
                        onValueChange = { newDestAddress = it },
                        label = { Text("Alamat / Patokan Area (Opsional)") },
                        placeholder = { Text("Contoh: Jl. Raya Waru KM 15") },
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
