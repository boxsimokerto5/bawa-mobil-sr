package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DestinationEntity
import com.example.data.FleetRepository
import com.example.data.TripRequestEntity
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepInkBrown
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RichLeatherBrown
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftMochaText
import com.example.ui.theme.VintageCardCream
import com.example.ui.theme.VintageCreamBg
import com.example.ui.theme.VintageGreenBg
import com.example.ui.theme.VintageGreenSuccess
import com.example.ui.theme.VintageMapRoad
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun VintageRealTimeMapPanel(
    activeTrips: List<TripRequestEntity>,
    allDestinations: List<DestinationEntity>,
    focusedTripId: Int?,
    isGpsEnabled: Boolean,
    onSelectTrip: (Int?) -> Unit,
    onCompleteTrip: (TripRequestEntity) -> Unit,
    onAdvanceManualStep: () -> Unit,
    onLocationPermissionResult: (android.content.Context, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        onLocationPermissionResult(context, granted)
    }

    val selectedTrip = remember(activeTrips, focusedTripId) {
        activeTrips.firstOrNull { it.id == focusedTripId } ?: activeTrips.firstOrNull()
    }

    // Real OpenStreetMap slippy zoom level (11..17) & center lat/lng
    var osmZoom by remember { mutableIntStateOf(13) }
    var centerLat by remember {
        mutableDoubleStateOf(selectedTrip?.currentLat ?: FleetRepository.BASE_LAT)
    }
    var centerLng by remember {
        mutableDoubleStateOf(selectedTrip?.currentLng ?: FleetRepository.BASE_LNG)
    }
    var followSelectedVehicle by remember { mutableStateOf(true) }
    var vintageTintOverlay by remember { mutableStateOf(true) }

    // Automatically follow the focused vehicle as its GPS/telemetry updates if follow mode is active
    LaunchedEffect(selectedTrip?.id, selectedTrip?.currentLat, selectedTrip?.currentLng, followSelectedVehicle) {
        if (followSelectedVehicle && selectedTrip != null) {
            centerLat = selectedTrip.currentLat
            centerLng = selectedTrip.currentLng
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "osm_radar_pulse")
    val pulseRadiusMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Top Header Info Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Peta Jalan Asli (OpenStreetMap)",
                    style = MaterialTheme.typography.titleLarge,
                    color = EspressoBrown
                )
                Text(
                    text = if (activeTrips.isEmpty()) {
                        "Semua unit parkir di Pos Utama SR • Peta Jalan Live"
                    } else {
                        "${activeTrips.size} Unit Dalam Perjalanan • Pantauan Peta Jalan Asli"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftMochaText
                )
            }

            // GPS Sensor Link Button
            Surface(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                shape = RoundedCornerShape(50),
                color = if (isGpsEnabled) VintageGreenBg else SoftGoldHighlight,
                border = BorderStroke(1.dp, if (isGpsEnabled) VintageGreenSuccess else AntiqueGold),
                modifier = Modifier.testTag("gps_sync_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Aktifkan GPS Perangkat",
                        tint = if (isGpsEnabled) VintageGreenSuccess else EspressoBrown,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGpsEnabled) "GPS HP Aktif" else "Sinkron GPS HP",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGpsEnabled) VintageGreenSuccess else DeepInkBrown,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips for Active Vehicles on Map
        if (activeTrips.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activeTrips, key = { it.id }) { trip ->
                    val isSelected = selectedTrip?.id == trip.id
                    Surface(
                        onClick = {
                            onSelectTrip(trip.id)
                            followSelectedVehicle = true
                            centerLat = trip.currentLat
                            centerLng = trip.currentLng
                        },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) EspressoBrown else VintageCardCream,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MetallicGold else VintageWarmBorder
                        ),
                        modifier = Modifier.testTag("map_vehicle_chip_${trip.vehicleId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = vehicleIconFor(trip.vehicleId),
                                contentDescription = trip.vehicleName,
                                tint = if (isSelected) MetallicGold else EspressoBrown,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${trip.vehicleName} (${trip.driverName.split(" ").firstOrNull() ?: ""})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) SoftGoldHighlight else DeepInkBrown,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${trip.currentSpeedKmh} km/j",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) VintageCreamBg else VintageGreenSuccess
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Real OpenStreetMap Interactive Map Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("digital_realtime_map_canvas"),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, AntiqueGold),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E9D8))
                        .pointerInput(osmZoom) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                followSelectedVehicle = false
                                val centerTileX = WebMercator.lonToTileX(centerLng, osmZoom)
                                val centerTileY = WebMercator.latToTileY(centerLat, osmZoom)
                                val newTileX = centerTileX - (dragAmount.x / WebMercator.TILE_SIZE)
                                val newTileY = centerTileY - (dragAmount.y / WebMercator.TILE_SIZE)
                                centerLng = WebMercator.tileXToLon(newTileX, osmZoom).coerceIn(-179.9, 179.9)
                                centerLat = WebMercator.tileYToLat(newTileY, osmZoom).coerceIn(-80.0, 80.0)
                            }
                        }
                        .pointerInput(activeTrips, osmZoom, centerLat, centerLng) {
                            detectTapGestures { tapOffset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val cTileX = WebMercator.lonToTileX(centerLng, osmZoom)
                                val cTileY = WebMercator.latToTileY(centerLat, osmZoom)

                                fun project(lat: Double, lng: Double): Offset {
                                    val tx = WebMercator.lonToTileX(lng, osmZoom)
                                    val ty = WebMercator.latToTileY(lat, osmZoom)
                                    return Offset(
                                        x = (w / 2f + (tx - cTileX) * WebMercator.TILE_SIZE).toFloat(),
                                        y = (h / 2f + (ty - cTileY) * WebMercator.TILE_SIZE).toFloat()
                                    )
                                }

                                val hitTrip = activeTrips.minByOrNull { trip ->
                                    val pt = project(trip.currentLat, trip.currentLng)
                                    (pt - tapOffset).getDistance()
                                }
                                if (hitTrip != null) {
                                    val pt = project(hitTrip.currentLat, hitTrip.currentLng)
                                    if ((pt - tapOffset).getDistance() < 90f) {
                                        onSelectTrip(hitTrip.id)
                                        followSelectedVehicle = true
                                        centerLat = hitTrip.currentLat
                                        centerLng = hitTrip.currentLng
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val tileSizePx = WebMercator.TILE_SIZE.toFloat()

                    val centerTileX = WebMercator.lonToTileX(centerLng, osmZoom)
                    val centerTileY = WebMercator.latToTileY(centerLat, osmZoom)

                    fun project(lat: Double, lng: Double): Offset {
                        val tx = WebMercator.lonToTileX(lng, osmZoom)
                        val ty = WebMercator.latToTileY(lat, osmZoom)
                        return Offset(
                            x = (w / 2f + (tx - centerTileX) * tileSizePx).toFloat(),
                            y = (h / 2f + (ty - centerTileY) * tileSizePx).toFloat()
                        )
                    }

                    // 1. Render Real OpenStreetMap Raster Tiles
                    val halfTilesX = (w / (2f * tileSizePx)).toInt() + 2
                    val halfTilesY = (h / (2f * tileSizePx)).toInt() + 2
                    val baseTileX = floor(centerTileX).toInt()
                    val baseTileY = floor(centerTileY).toInt()

                    for (tx in (baseTileX - halfTilesX)..(baseTileX + halfTilesX)) {
                        for (ty in (baseTileY - halfTilesY)..(baseTileY + halfTilesY)) {
                            val drawLeft = (w / 2f + (tx - centerTileX) * tileSizePx).roundToInt()
                            val drawTop = (h / 2f + (ty - centerTileY) * tileSizePx).roundToInt()
                            val drawRight = (w / 2f + (tx + 1 - centerTileX) * tileSizePx).roundToInt()
                            val drawBottom = (h / 2f + (ty + 1 - centerTileY) * tileSizePx).roundToInt()

                            val tileW = (drawRight - drawLeft).coerceAtLeast(1)
                            val tileH = (drawBottom - drawTop).coerceAtLeast(1)

                            val bmp = OsmTileStore.getOrLoadTile(context, osmZoom, tx, ty)
                            if (bmp != null) {
                                drawImage(
                                    image = bmp.asImageBitmap(),
                                    dstOffset = IntOffset(drawLeft, drawTop),
                                    dstSize = IntSize(tileW, tileH)
                                )
                            } else {
                                // Subtle placeholder grid while real OSM tile downloads
                                drawRect(
                                    color = Color(0xFFEDE1CB),
                                    topLeft = Offset(drawLeft.toFloat(), drawTop.toFloat()),
                                    size = Size(tileW.toFloat(), tileH.toFloat())
                                )
                                drawRect(
                                    color = AntiqueGold.copy(alpha = 0.25f),
                                    topLeft = Offset(drawLeft.toFloat(), drawTop.toFloat()),
                                    size = Size(tileW.toFloat(), tileH.toFloat()),
                                    style = Stroke(width = 1f)
                                )
                            }
                        }
                    }

                    // Optional Warm Vintage Sepia Glaze over OpenStreetMap so it blends with Brown & Gold theme
                    if (vintageTintOverlay) {
                        drawRect(
                            color = Color(0xFFD4AF37).copy(alpha = 0.10f),
                            topLeft = Offset.Zero,
                            size = size
                        )
                    }

                    // 2. Draw All Registered Destinations as Waypoint Pins on Real Street Map
                    val basePos = project(FleetRepository.BASE_LAT, FleetRepository.BASE_LNG)
                    allDestinations.forEach { dest ->
                        val pt = project(dest.latitude, dest.longitude)
                        if (pt.x in -100f..(w + 100f) && pt.y in -100f..(h + 100f)) {
                            drawCircle(
                                color = EspressoBrown,
                                radius = 7f,
                                center = pt
                            )
                            drawCircle(
                                color = MetallicGold,
                                radius = 4f,
                                center = pt
                            )
                        }
                    }

                    // 3. Draw Active Trip Route Polylines & Numbered Stops on Real Street Map
                    activeTrips.forEach { trip ->
                        val isFocused = selectedTrip?.id == trip.id
                        val coords = trip.parsedCoordinates
                        val names = trip.parsedDestinations
                        val waypoints = buildList {
                            add(Pair(FleetRepository.BASE_LAT, FleetRepository.BASE_LNG))
                            addAll(coords)
                        }

                        for (i in 0 until waypoints.size - 1) {
                            val pStart = project(waypoints[i].first, waypoints[i].second)
                            val pEnd = project(waypoints[i + 1].first, waypoints[i + 1].second)

                            // Casing outline so route stands out clearly over OpenStreetMap roads
                            drawLine(
                                color = if (isFocused) MetallicGold.copy(alpha = 0.75f) else VintageCardCream.copy(alpha = 0.7f),
                                start = pStart,
                                end = pEnd,
                                strokeWidth = if (isFocused) 13f else 8f,
                                cap = StrokeCap.Round
                            )

                            drawLine(
                                color = if (isFocused) EspressoBrown else RichLeatherBrown.copy(alpha = 0.75f),
                                start = pStart,
                                end = pEnd,
                                strokeWidth = if (isFocused) 6.5f else 4f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f),
                                cap = StrokeCap.Round
                            )
                        }

                        // Numbered Destination Pins
                        coords.forEachIndexed { idx, pair ->
                            val destPt = project(pair.first, pair.second)
                            drawCircle(
                                color = EspressoBrown,
                                radius = if (isFocused) 16f else 12f,
                                center = destPt
                            )
                            drawCircle(
                                color = MetallicGold,
                                radius = if (isFocused) 13f else 9f,
                                center = destPt
                            )
                            val numLayout = textMeasurer.measure(
                                text = "${idx + 1}",
                                style = TextStyle(
                                    color = DeepInkBrown,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = numLayout,
                                topLeft = Offset(
                                    destPt.x - numLayout.size.width / 2f,
                                    destPt.y - numLayout.size.height / 2f
                                )
                            )

                            if (isFocused) {
                                val destTitle = names.getOrNull(idx) ?: "Tujuan ${idx + 1}"
                                val labelResult = textMeasurer.measure(
                                    text = destTitle,
                                    style = TextStyle(
                                        color = DeepInkBrown,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                val padH = 7f
                                val padV = 4f
                                val boxW = labelResult.size.width + padH * 2
                                val boxH = labelResult.size.height + padV * 2
                                val boxTopLeft = Offset(destPt.x - boxW / 2f, destPt.y + 18f)
                                drawRoundRect(
                                    color = VintageCardCream.copy(alpha = 0.95f),
                                    topLeft = boxTopLeft,
                                    size = Size(boxW, boxH),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                                drawRoundRect(
                                    color = EspressoBrown,
                                    topLeft = boxTopLeft,
                                    size = Size(boxW, boxH),
                                    cornerRadius = CornerRadius(6f, 6f),
                                    style = Stroke(width = 1.5f)
                                )
                                drawText(
                                    textLayoutResult = labelResult,
                                    topLeft = Offset(boxTopLeft.x + padH, boxTopLeft.y + padV)
                                )
                            }
                        }
                    }

                    // 4. Draw Base Camp Marker (Pos Utama Keamanan SR)
                    drawCircle(
                        color = EspressoBrown,
                        radius = 17f,
                        center = basePos
                    )
                    drawCircle(
                        color = MetallicGold,
                        radius = 12f,
                        center = basePos
                    )
                    drawCircle(
                        color = EspressoBrown,
                        radius = 5f,
                        center = basePos
                    )
                    val baseLabel = textMeasurer.measure(
                        text = "POS UTAMA SR",
                        style = TextStyle(
                            color = SoftGoldHighlight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    val baseTagW = baseLabel.size.width + 14f
                    val baseTagH = baseLabel.size.height + 6f
                    val baseTagOffset = Offset(basePos.x - baseTagW / 2f, basePos.y - 35f)
                    drawRoundRect(
                        color = EspressoBrown,
                        topLeft = baseTagOffset,
                        size = Size(baseTagW, baseTagH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = MetallicGold,
                        topLeft = baseTagOffset,
                        size = Size(baseTagW, baseTagH),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1.2f)
                    )
                    drawText(
                        textLayoutResult = baseLabel,
                        topLeft = Offset(baseTagOffset.x + 7f, baseTagOffset.y + 3f)
                    )

                    // 5. Draw Live Moving Vehicle Markers on Real OpenStreetMap
                    activeTrips.forEach { trip ->
                        val isFocused = selectedTrip?.id == trip.id
                        val vPos = project(trip.currentLat, trip.currentLng)

                        drawCircle(
                            color = if (isFocused) {
                                EspressoBrown.copy(alpha = pulseAlpha * 0.65f)
                            } else {
                                VintageGreenSuccess.copy(alpha = pulseAlpha * 0.65f)
                            },
                            radius = (if (isFocused) 28f else 22f) * pulseRadiusMultiplier,
                            center = vPos
                        )

                        drawCircle(
                            color = if (isFocused) MetallicGold else AntiqueGold,
                            radius = if (isFocused) 23f else 18f,
                            center = vPos
                        )
                        drawCircle(
                            color = if (isFocused) EspressoBrown else RichLeatherBrown,
                            radius = if (isFocused) 18f else 14f,
                            center = vPos
                        )
                        drawCircle(
                            color = SoftGoldHighlight,
                            radius = 5f,
                            center = vPos
                        )

                        val calloutText = "${trip.vehicleName} • ${trip.currentSpeedKmh} km/j"
                        val measuredCallout = textMeasurer.measure(
                            text = calloutText,
                            style = TextStyle(
                                color = SoftGoldHighlight,
                                fontSize = if (isFocused) 11.sp else 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        val tagW = measuredCallout.size.width + 18f
                        val tagH = measuredCallout.size.height + 10f
                        val tagTopLeft = Offset(vPos.x - tagW / 2f, vPos.y - 48f)

                        drawRoundRect(
                            color = EspressoBrown.copy(alpha = 0.96f),
                            topLeft = tagTopLeft,
                            size = Size(tagW, tagH),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = MetallicGold,
                            topLeft = tagTopLeft,
                            size = Size(tagW, tagH),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = if (isFocused) 2.5f else 1.5f)
                        )
                        drawText(
                            textLayoutResult = measuredCallout,
                            topLeft = Offset(tagTopLeft.x + 9f, tagTopLeft.y + 5f)
                        )
                    }
                }

                // Top-Left Badge: OpenStreetMap Live Status
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = VintageCardCream.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, AntiqueGold),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "OpenStreetMap",
                            tint = EspressoBrown,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "OPENSTREETMAP LIVE (Z$osmZoom)",
                                style = MaterialTheme.typography.labelSmall,
                                color = EspressoBrown,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (followSelectedVehicle) "Mengikuti posisi mobil otomatis" else "Mode geser bebas (Ketuk Pusat untuk fokus)",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftMochaText,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Bottom-Left Mandatory OpenStreetMap Attribution Pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = VintageCardCream.copy(alpha = 0.88f),
                    border = BorderStroke(0.5.dp, VintageWarmBorder)
                ) {
                    Text(
                        text = "© OpenStreetMap contributors",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepInkBrown,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Top-Right Map Controls: Zoom In, Zoom Out, Center on Vehicle/Base, Toggle Vintage Filter, Step Forward
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MapControlButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Perbesar Peta Jalan (Zoom In)",
                        onClick = { osmZoom = (osmZoom + 1).coerceAtMost(17) }
                    )
                    MapControlButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Perkecil Peta Jalan (Zoom Out)",
                        onClick = { osmZoom = (osmZoom - 1).coerceAtLeast(10) }
                    )
                    MapControlButton(
                        icon = Icons.Default.MyLocation,
                        contentDescription = "Fokus ke Kendaraan / Pos Utama",
                        onClick = {
                            followSelectedVehicle = true
                            centerLat = selectedTrip?.currentLat ?: FleetRepository.BASE_LAT
                            centerLng = selectedTrip?.currentLng ?: FleetRepository.BASE_LNG
                            osmZoom = 14
                        }
                    )
                    MapControlButton(
                        icon = Icons.Default.Layers,
                        contentDescription = "Ganti Filter Warna Peta (Vintage / Asli)",
                        onClick = { vintageTintOverlay = !vintageTintOverlay }
                    )
                    MapControlButton(
                        icon = Icons.Default.FastForward,
                        contentDescription = "Percepat Laju Posisi",
                        onClick = onAdvanceManualStep
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Live Telemetry Detail Card for Selected Active Vehicle
        if (selectedTrip != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_trip_telemetry_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                border = BorderStroke(1.5.dp, AntiqueGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EspressoBrown),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vehicleIconFor(selectedTrip.vehicleId),
                                    contentDescription = selectedTrip.vehicleName,
                                    tint = MetallicGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${selectedTrip.vehicleName} • ${selectedTrip.vehiclePlate}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepInkBrown
                                )
                                Text(
                                    text = "Pembawa: ${selectedTrip.driverName} (${selectedTrip.driverDivision})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftMochaText
                                )
                            }
                        }

                        TripStatusBadge(status = selectedTrip.tripStatusEnum)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Telemetry Coordinates & Speed Row
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VintageParchmentSurface,
                        border = BorderStroke(1.dp, VintageWarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Koordinat",
                                    tint = EspressoBrown,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(
                                        java.util.Locale.US,
                                        "OSM %.4f, %.4f",
                                        selectedTrip.currentLat,
                                        selectedTrip.currentLng
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DeepInkBrown
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Kecepatan",
                                    tint = VintageGreenSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${selectedTrip.currentSpeedKmh} km/jam",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VintageGreenSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Rute Tujuan: Pos Utama SR → ${selectedTrip.parsedDestinations.joinToString(" → ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepInkBrown,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { selectedTrip.progressPercent.coerceIn(0.05f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = AntiqueGold,
                        trackColor = VintageParchmentSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val uri = Uri.parse(
                                        "geo:${selectedTrip.currentLat},${selectedTrip.currentLng}?q=${selectedTrip.currentLat},${selectedTrip.currentLng}(${Uri.encode(selectedTrip.vehicleName)})"
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            },
                            border = BorderStroke(1.dp, AntiqueGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Buka Navigasi Eksternal",
                                tint = EspressoBrown,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rute Eksternal",
                                style = MaterialTheme.typography.labelSmall,
                                color = EspressoBrown
                            )
                        }

                        Button(
                            onClick = { onCompleteTrip(selectedTrip) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EspressoBrown,
                                contentColor = SoftGoldHighlight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("complete_trip_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selesai",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Selesai Perjalanan",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = VintageCardCream.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, AntiqueGold),
        shadowElevation = 3.dp,
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = EspressoBrown,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
