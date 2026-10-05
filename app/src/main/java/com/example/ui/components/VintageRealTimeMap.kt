package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DestinationEntity
import com.example.data.FleetRepository
import com.example.data.TripRequestEntity
import com.example.data.TripStatus
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Almost Full-Screen Popup Modal Dialog (96% width x 94% height) for live OpenStreetMap monitoring.
 * Gives maximum viewport space to the real street map while keeping floating controls & compact telemetry.
 */
@Composable
fun VintageNearFullScreenMapPopup(
    activeTrips: List<TripRequestEntity>,
    allDestinations: List<DestinationEntity>,
    focusedTripId: Int?,
    isGpsEnabled: Boolean,
    onSelectTrip: (Int?) -> Unit,
    onCompleteTrip: (TripRequestEntity) -> Unit,
    onAdvanceManualStep: () -> Unit,
    onLocationPermissionResult: (android.content.Context, Boolean) -> Unit,
    onDismiss: () -> Unit,
    allTrips: List<TripRequestEntity> = activeTrips
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .fillMaxHeight(0.95f)
                    .testTag("near_fullscreen_map_popup"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCreamBg),
                border = BorderStroke(2.5.dp, MetallicGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Compact Vintage Popup Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(EspressoBrown, RichLeatherBrown)
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = MetallicGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Layar Penuh Pantauan Peta Jalan Asli",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftGoldHighlight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "OpenStreetMap Live • Cubit 2 jari atau ketuk 2x untuk perbesar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VintageCreamBg.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(50),
                            color = SoftGoldHighlight,
                            border = BorderStroke(1.dp, MetallicGold),
                            modifier = Modifier.testTag("close_fullscreen_map_popup_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup Pop Up Peta",
                                    tint = DeepInkBrown,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tutup",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeepInkBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Full-bleed Map Content inside Popup
                    VintageRealTimeMapPanel(
                        activeTrips = activeTrips,
                        allDestinations = allDestinations,
                        focusedTripId = focusedTripId,
                        isGpsEnabled = isGpsEnabled,
                        onSelectTrip = onSelectTrip,
                        onCompleteTrip = onCompleteTrip,
                        onAdvanceManualStep = onAdvanceManualStep,
                        onLocationPermissionResult = onLocationPermissionResult,
                        isInsidePopup = true,
                        onOpenFullPopup = onDismiss,
                        allTrips = allTrips,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

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
    isInsidePopup: Boolean = false,
    onOpenFullPopup: (() -> Unit)? = null,
    allTrips: List<TripRequestEntity> = activeTrips,
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

    // Include completed trips with route trails if focused from Rekap Perjalanan
    val displayTrips = remember(activeTrips, allTrips, focusedTripId) {
        val focusedFromAll = allTrips.firstOrNull { it.id == focusedTripId }
        if (focusedFromAll != null && activeTrips.none { it.id == focusedFromAll.id }) {
            activeTrips + focusedFromAll
        } else if (activeTrips.isEmpty() && allTrips.isNotEmpty()) {
            allTrips.filter { it.tripStatusEnum == TripStatus.COMPLETED }.take(2)
        } else {
            activeTrips
        }
    }

    val selectedTrip = remember(displayTrips, focusedTripId) {
        displayTrips.firstOrNull { it.id == focusedTripId } ?: displayTrips.firstOrNull()
    }

    // Continuous floating-point zoom (10.5f .. 17.5f) for butter-smooth pinch & button zoom
    var targetZoom by remember { mutableFloatStateOf(if (isInsidePopup) 14.0f else 13.5f) }
    val smoothZoom by animateFloatAsState(
        targetValue = targetZoom,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "smooth_osm_zoom"
    )

    var centerLat by remember {
        mutableDoubleStateOf(selectedTrip?.currentLat ?: FleetRepository.BASE_LAT)
    }
    var centerLng by remember {
        mutableDoubleStateOf(selectedTrip?.currentLng ?: FleetRepository.BASE_LNG)
    }
    var followSelectedVehicle by remember { mutableStateOf(true) }
    var vintageTintOverlay by remember { mutableStateOf(true) }
    // Allow collapsing the bottom telemetry card so the map can occupy 100% of the canvas
    var isTelemetryExpanded by remember { mutableStateOf(!isInsidePopup) }

    // Smoothly interpolate camera & vehicle positions so markers glide at 60fps
    val animatedCenterLat by animateFloatAsState(
        targetValue = centerLat.toFloat(),
        animationSpec = tween(
            durationMillis = if (followSelectedVehicle) 1800 else 45,
            easing = LinearEasing
        ),
        label = "camera_lat"
    )
    val animatedCenterLng by animateFloatAsState(
        targetValue = centerLng.toFloat(),
        animationSpec = tween(
            durationMillis = if (followSelectedVehicle) 1800 else 45,
            easing = LinearEasing
        ),
        label = "camera_lng"
    )

    LaunchedEffect(selectedTrip?.id, selectedTrip?.currentLat, selectedTrip?.currentLng, followSelectedVehicle) {
        if (followSelectedVehicle && selectedTrip != null) {
            centerLat = selectedTrip.currentLat
            centerLng = selectedTrip.currentLng
        }
    }

    val discreteZoom = smoothZoom.toInt().coerceIn(10, 17)
    LaunchedEffect(discreteZoom, (centerLat * 100).toInt(), (centerLng * 100).toInt()) {
        val cx = floor(WebMercator.lonToTileX(centerLng, discreteZoom)).toInt()
        val cy = floor(WebMercator.latToTileY(centerLat, discreteZoom)).toInt()
        OsmTileStore.prefetchRegion(context, discreteZoom, cx, cy, radius = 2)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "osm_radar_pulse")
    val pulseRadiusMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 2.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val animatedVehiclePositions = displayTrips.associate { trip ->
        val animLat by animateFloatAsState(
            targetValue = trip.currentLat.toFloat(),
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing),
            label = "veh_lat_${trip.id}"
        )
        val animLng by animateFloatAsState(
            targetValue = trip.currentLng.toFloat(),
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing),
            label = "veh_lng_${trip.id}"
        )
        trip.id to Pair(animLat.toDouble(), animLng.toDouble())
    }

    val textMeasurer = rememberTextMeasurer()
    val tileRevisionCount = OsmTileStore.tileRevision.intValue

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(if (isInsidePopup) 6.dp else 8.dp)
    ) {
        // Full-Screen / Near-Full-Screen Interactive Map Canvas
        Card(
            modifier = Modifier
                .fillMaxSize()
                .testTag("digital_realtime_map_canvas"),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, AntiqueGold),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            // Focal-point (centroid) 2-finger pinch-to-zoom & smooth panning like Google Maps
                            detectTransformGestures(panZoomLock = false) { centroid, pan, zoomChange, _ ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val oldZoom = targetZoom
                                val oldZInt = oldZoom.toInt().coerceIn(10, 17)
                                val oldScale = 2.0.pow((oldZoom - oldZInt).toDouble())
                                val oldEffectiveTileSize = WebMercator.TILE_SIZE * oldScale

                                val cTileX = WebMercator.lonToTileX(centerLng, oldZInt)
                                val cTileY = WebMercator.latToTileY(centerLat, oldZInt)

                                if (zoomChange != 1f || pan != Offset.Zero) {
                                    followSelectedVehicle = false
                                }

                                // 1. Apply pan first in current tile space
                                val pannedCenterTileX = cTileX - (pan.x / oldEffectiveTileSize)
                                val pannedCenterTileY = cTileY - (pan.y / oldEffectiveTileSize)

                                if (zoomChange != 1f && w > 0f && h > 0f) {
                                    // 2. Keep the geographic coordinate under the 2-finger centroid anchored while zooming
                                    val dxPx = (centroid.x - w / 2f).toDouble()
                                    val dyPx = (centroid.y - h / 2f).toDouble()
                                    val focalTileX = pannedCenterTileX + dxPx / oldEffectiveTileSize
                                    val focalTileY = pannedCenterTileY + dyPx / oldEffectiveTileSize
                                    val focalLon = WebMercator.tileXToLon(focalTileX, oldZInt)
                                    val focalLat = WebMercator.tileYToLat(focalTileY, oldZInt)

                                    val zoomDelta = (ln(zoomChange.toDouble()) / ln(2.0)).toFloat()
                                    val newZoom = (oldZoom + zoomDelta).coerceIn(10.5f, 17.5f)
                                    targetZoom = newZoom

                                    val newZInt = newZoom.toInt().coerceIn(10, 17)
                                    val newScale = 2.0.pow((newZoom - newZInt).toDouble())
                                    val newEffectiveTileSize = WebMercator.TILE_SIZE * newScale

                                    val focalNewTileX = WebMercator.lonToTileX(focalLon, newZInt)
                                    val focalNewTileY = WebMercator.latToTileY(focalLat, newZInt)

                                    val anchoredCenterTileX = focalNewTileX - dxPx / newEffectiveTileSize
                                    val anchoredCenterTileY = focalNewTileY - dyPx / newEffectiveTileSize

                                    centerLng = WebMercator.tileXToLon(anchoredCenterTileX, newZInt).coerceIn(-179.9, 179.9)
                                    centerLat = WebMercator.tileYToLat(anchoredCenterTileY, newZInt).coerceIn(-80.0, 80.0)
                                } else if (pan != Offset.Zero) {
                                    centerLng = WebMercator.tileXToLon(pannedCenterTileX, oldZInt).coerceIn(-179.9, 179.9)
                                    centerLat = WebMercator.tileYToLat(pannedCenterTileY, oldZInt).coerceIn(-80.0, 80.0)
                                }
                            }
                        }
                        .pointerInput(displayTrips, smoothZoom, centerLat, centerLng) {
                            detectTapGestures(
                                onDoubleTap = { tapOffset ->
                                    followSelectedVehicle = false
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val zInt = targetZoom.toInt().coerceIn(10, 17)
                                    val scaleFactor = 2.0.pow((targetZoom - zInt).toDouble())
                                    val effectiveTileSize = WebMercator.TILE_SIZE * scaleFactor
                                    val cTileX = WebMercator.lonToTileX(centerLng, zInt)
                                    val cTileY = WebMercator.latToTileY(centerLat, zInt)
                                    val tappedTileX = cTileX + (tapOffset.x - w / 2f) / effectiveTileSize * 0.5
                                    val tappedTileY = cTileY + (tapOffset.y - h / 2f) / effectiveTileSize * 0.5
                                    centerLng = WebMercator.tileXToLon(tappedTileX, zInt).coerceIn(-179.9, 179.9)
                                    centerLat = WebMercator.tileYToLat(tappedTileY, zInt).coerceIn(-80.0, 80.0)
                                    targetZoom = (targetZoom + 1.0f).coerceAtMost(17.5f)
                                },
                                onTap = { tapOffset ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val zInt = smoothZoom.toInt().coerceIn(10, 17)
                                    val scaleFactor = 2f.pow(smoothZoom - zInt)
                                    val effectiveTileSize = WebMercator.TILE_SIZE.toFloat() * scaleFactor
                                    val cTileX = WebMercator.lonToTileX(centerLng, zInt)
                                    val cTileY = WebMercator.latToTileY(centerLat, zInt)

                                    fun project(lat: Double, lng: Double): Offset {
                                        val tx = WebMercator.lonToTileX(lng, zInt)
                                        val ty = WebMercator.latToTileY(lat, zInt)
                                        return Offset(
                                            x = (w / 2f + (tx - cTileX) * effectiveTileSize).toFloat(),
                                            y = (h / 2f + (ty - cTileY) * effectiveTileSize).toFloat()
                                        )
                                    }

                                    val hitTrip = displayTrips.minByOrNull { trip ->
                                        val pt = project(trip.currentLat, trip.currentLng)
                                        (pt - tapOffset).getDistance()
                                    }
                                    if (hitTrip != null) {
                                        val pt = project(hitTrip.currentLat, hitTrip.currentLng)
                                        if ((pt - tapOffset).getDistance() < 95f) {
                                            onSelectTrip(hitTrip.id)
                                            followSelectedVehicle = true
                                            centerLat = hitTrip.currentLat
                                            centerLng = hitTrip.currentLng
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    val _rev = tileRevisionCount

                    val w = size.width
                    val h = size.height

                    val zInt = smoothZoom.toInt().coerceIn(10, 17)
                    val fractionalScale = 2f.pow(smoothZoom - zInt)
                    val tileSizePx = WebMercator.TILE_SIZE.toFloat() * fractionalScale

                    val renderCenterLat = if (followSelectedVehicle) animatedCenterLat.toDouble() else centerLat
                    val renderCenterLng = if (followSelectedVehicle) animatedCenterLng.toDouble() else centerLng

                    val centerTileX = WebMercator.lonToTileX(renderCenterLng, zInt)
                    val centerTileY = WebMercator.latToTileY(renderCenterLat, zInt)

                    fun project(lat: Double, lng: Double): Offset {
                        val tx = WebMercator.lonToTileX(lng, zInt)
                        val ty = WebMercator.latToTileY(lat, zInt)
                        return Offset(
                            x = (w / 2f + (tx - centerTileX) * tileSizePx).toFloat(),
                            y = (h / 2f + (ty - centerTileY) * tileSizePx).toFloat()
                        )
                    }

                    // 1. Render Real OpenStreetMap Raster Tiles with Parent-Tile Fallback
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

                            val tileSpec = OsmTileStore.getTileOrFallback(context, zInt, tx, ty)
                            if (tileSpec != null) {
                                drawImage(
                                    image = tileSpec.imageBitmap,
                                    srcOffset = IntOffset(tileSpec.srcLeft, tileSpec.srcTop),
                                    srcSize = IntSize(tileSpec.srcWidth, tileSpec.srcHeight),
                                    dstOffset = IntOffset(drawLeft, drawTop),
                                    dstSize = IntSize(tileW, tileH),
                                    filterQuality = FilterQuality.Medium
                                )
                            } else {
                                drawRect(
                                    color = Color(0xFFEDE1CB),
                                    topLeft = Offset(drawLeft.toFloat(), drawTop.toFloat()),
                                    size = Size(tileW.toFloat(), tileH.toFloat())
                                )
                                drawRect(
                                    color = AntiqueGold.copy(alpha = 0.22f),
                                    topLeft = Offset(drawLeft.toFloat(), drawTop.toFloat()),
                                    size = Size(tileW.toFloat(), tileH.toFloat()),
                                    style = Stroke(width = 1f)
                                )
                            }
                        }
                    }

                    if (vintageTintOverlay) {
                        drawRect(
                            color = Color(0xFFD4AF37).copy(alpha = 0.08f),
                            topLeft = Offset.Zero,
                            size = size
                        )
                    }

                    // 2. Registered Destination Pins
                    val basePos = project(FleetRepository.BASE_LAT, FleetRepository.BASE_LNG)
                    allDestinations.forEach { dest ->
                        val pt = project(dest.latitude, dest.longitude)
                        if (pt.x in -80f..(w + 80f) && pt.y in -80f..(h + 80f)) {
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

                    // 3. Planned Round-Trip Route (Dashed Line: Pos SR -> Destinations -> Back to Pos SR)
                    //    + Solid Recorded Journey Trail (Garis Perjalanan Nyata dari Keberangkatan sampai Kembali ke SR)
                    displayTrips.forEach { trip ->
                        val isFocused = selectedTrip?.id == trip.id
                        val coords = trip.parsedCoordinates
                        val names = trip.parsedDestinations

                        // 3A. Planned Round-Trip Loop (Pos Utama SR -> Tujuan 1..N -> Kembali ke Pos Utama SR)
                        val plannedWaypoints = buildList {
                            add(Pair(FleetRepository.BASE_LAT, FleetRepository.BASE_LNG))
                            addAll(coords)
                            add(Pair(FleetRepository.BASE_LAT, FleetRepository.BASE_LNG))
                        }

                        for (i in 0 until plannedWaypoints.size - 1) {
                            val pStart = project(plannedWaypoints[i].first, plannedWaypoints[i].second)
                            val pEnd = project(plannedWaypoints[i + 1].first, plannedWaypoints[i + 1].second)

                            drawLine(
                                color = if (isFocused) VintageCardCream.copy(alpha = 0.72f) else VintageCardCream.copy(alpha = 0.45f),
                                start = pStart,
                                end = pEnd,
                                strokeWidth = if (isFocused) 10f else 6f,
                                cap = StrokeCap.Round
                            )

                            drawLine(
                                color = if (isFocused) RichLeatherBrown.copy(alpha = 0.65f) else RichLeatherBrown.copy(alpha = 0.40f),
                                start = pStart,
                                end = pEnd,
                                strokeWidth = if (isFocused) 4.5f else 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f),
                                cap = StrokeCap.Round
                            )
                        }

                        // 3B. SOLID RECORDED JOURNEY TRAIL (Garis Jejak Perjalanan yang Sudah Ditempuh Mobil)
                        val smoothVehPos = animatedVehiclePositions[trip.id]
                        val recordedTrail = buildList {
                            val rawTrail = trip.parsedTrailCoordinates
                            if (rawTrail.isEmpty()) {
                                add(Pair(FleetRepository.BASE_LAT, FleetRepository.BASE_LNG))
                            } else {
                                addAll(rawTrail)
                            }
                            if (smoothVehPos != null && trip.tripStatusEnum == TripStatus.IN_TRANSIT) {
                                add(smoothVehPos)
                            }
                        }

                        if (recordedTrail.size >= 2) {
                            val trailPath = Path()
                            val projectedTrail = recordedTrail.map { project(it.first, it.second) }
                            projectedTrail.forEachIndexed { index, pt ->
                                if (index == 0) {
                                    trailPath.moveTo(pt.x, pt.y)
                                } else {
                                    trailPath.lineTo(pt.x, pt.y)
                                }
                            }

                            // Outer Vintage Gold Halo for the traveled trail
                            drawPath(
                                path = trailPath,
                                color = if (isFocused) MetallicGold.copy(alpha = 0.92f) else AntiqueGold.copy(alpha = 0.65f),
                                style = Stroke(
                                    width = if (isFocused) 15f else 10f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Inner Emerald/Espresso Traveled Line
                            drawPath(
                                path = trailPath,
                                color = if (trip.isArrivedBackAtSrGate || trip.tripStatusEnum == TripStatus.COMPLETED) {
                                    VintageGreenSuccess
                                } else if (isFocused) {
                                    EspressoBrown
                                } else {
                                    RichLeatherBrown
                                },
                                style = Stroke(
                                    width = if (isFocused) 8.5f else 5.5f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Breadcrumb Dots & Directional Chevrons along the recorded trail
                            for (i in 0 until projectedTrail.size - 1) {
                                val p1 = projectedTrail[i]
                                val p2 = projectedTrail[i + 1]
                                drawCircle(
                                    color = SoftGoldHighlight,
                                    radius = if (isFocused) 3.2f else 2.2f,
                                    center = p1
                                )
                                val segLen = (p2 - p1).getDistance()
                                if (isFocused && segLen > 28f) {
                                    val mid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                                    val angle = atan2(p2.y - p1.y, p2.x - p1.x)
                                    val arrowLen = 7.5f
                                    val wing1 = Offset(
                                        x = mid.x - arrowLen * cos(angle - 0.55f),
                                        y = mid.y - arrowLen * sin(angle - 0.55f)
                                    )
                                    val wing2 = Offset(
                                        x = mid.x - arrowLen * cos(angle + 0.55f),
                                        y = mid.y - arrowLen * sin(angle + 0.55f)
                                    )
                                    drawLine(
                                        color = SoftGoldHighlight,
                                        start = wing1,
                                        end = mid,
                                        strokeWidth = 2.4f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = SoftGoldHighlight,
                                        start = wing2,
                                        end = mid,
                                        strokeWidth = 2.4f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        // 3C. Numbered Destination Pins
                        coords.forEachIndexed { idx, pair ->
                            val destPt = project(pair.first, pair.second)
                            if (destPt.x in -120f..(w + 120f) && destPt.y in -120f..(h + 120f)) {
                                val isVisited = trip.progressPercent >= ((idx + 1).toFloat() / (coords.size + 1).toFloat()) ||
                                    trip.tripStatusEnum == TripStatus.COMPLETED
                                drawCircle(
                                    color = if (isVisited) VintageGreenSuccess else EspressoBrown,
                                    radius = if (isFocused) 16f else 12f,
                                    center = destPt
                                )
                                drawCircle(
                                    color = if (isVisited) SoftGoldHighlight else MetallicGold,
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
                                        color = if (isVisited) VintageGreenSuccess else EspressoBrown,
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
                    }

                    // 4. Base Camp Marker (Pos Utama Keamanan SR)
                    if (basePos.x in -120f..(w + 120f) && basePos.y in -120f..(h + 120f)) {
                        drawCircle(
                            color = EspressoBrown,
                            radius = 18f,
                            center = basePos
                        )
                        drawCircle(
                            color = MetallicGold,
                            radius = 13f,
                            center = basePos
                        )
                        drawCircle(
                            color = EspressoBrown,
                            radius = 5.5f,
                            center = basePos
                        )
                        val baseLabel = textMeasurer.measure(
                            text = "POS KEAMANAN SR (START / KEMBALI)",
                            style = TextStyle(
                                color = SoftGoldHighlight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        val baseTagW = baseLabel.size.width + 14f
                        val baseTagH = baseLabel.size.height + 6f
                        val baseTagOffset = Offset(basePos.x - baseTagW / 2f, basePos.y - 36f)
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
                    }

                    // 5. Smoothly Gliding Live Vehicle Markers
                    displayTrips.forEach { trip ->
                        val isFocused = selectedTrip?.id == trip.id
                        val smoothCoords = animatedVehiclePositions[trip.id]
                        val vLat = smoothCoords?.first ?: trip.currentLat
                        val vLng = smoothCoords?.second ?: trip.currentLng
                        val vPos = project(vLat, vLng)

                        if (vPos.x in -150f..(w + 150f) && vPos.y in -150f..(h + 150f)) {
                            drawCircle(
                                color = if (trip.isArrivedBackAtSrGate || trip.tripStatusEnum == TripStatus.COMPLETED) {
                                    VintageGreenSuccess.copy(alpha = pulseAlpha * 0.7f)
                                } else if (isFocused) {
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
                                color = if (trip.isArrivedBackAtSrGate || trip.tripStatusEnum == TripStatus.COMPLETED) {
                                    VintageGreenSuccess
                                } else if (isFocused) {
                                    EspressoBrown
                                } else {
                                    RichLeatherBrown
                                },
                                radius = if (isFocused) 18f else 14f,
                                center = vPos
                            )
                            drawCircle(
                                color = SoftGoldHighlight,
                                radius = 5f,
                                center = vPos
                            )

                            val statusShort = when {
                                trip.tripStatusEnum == TripStatus.COMPLETED ->
                                    String.format(java.util.Locale.US, "Selesai • %.2f km", trip.totalDistanceTraveledKm)
                                trip.isArrivedBackAtSrGate ->
                                    String.format(java.util.Locale.US, "Tiba di Pos SR • %.2f km", trip.totalDistanceTraveledKm)
                                else ->
                                    String.format(java.util.Locale.US, "%d km/j • %.2f km", trip.currentSpeedKmh, trip.totalDistanceTraveledKm)
                            }
                            val calloutText = "${trip.vehicleName} • $statusShort"
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

                    // 6. Google Maps Style Dynamic Distance Scale Bar (Top-Left under chips)
                    val metersPerPixel = (156543.03392 * cos(Math.toRadians(renderCenterLat))) / 2.0.pow(smoothZoom.toDouble())
                    val targetBarPx = 95.0
                    val rawMeters = metersPerPixel * targetBarPx
                    val niceMeters = when {
                        rawMeters >= 5000 -> 5000
                        rawMeters >= 2000 -> 2000
                        rawMeters >= 1000 -> 1000
                        rawMeters >= 500 -> 500
                        rawMeters >= 200 -> 200
                        else -> 100
                    }
                    val barWidthPx = (niceMeters / metersPerPixel).toFloat().coerceIn(48f, 140f)
                    val scaleLabel = if (niceMeters >= 1000) "${niceMeters / 1000} km" else "$niceMeters m"
                    val scaleMeas = textMeasurer.measure(
                        text = "Skala $scaleLabel",
                        style = TextStyle(
                            color = DeepInkBrown,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    val scaleBoxTopLeft = Offset(14f, h - (if (selectedTrip != null) 215f else 48f))
                    if (scaleBoxTopLeft.y > 90f) {
                        drawRoundRect(
                            color = VintageCardCream.copy(alpha = 0.90f),
                            topLeft = scaleBoxTopLeft,
                            size = Size(barWidthPx + 20f, 26f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawLine(
                            color = EspressoBrown,
                            start = Offset(scaleBoxTopLeft.x + 10f, scaleBoxTopLeft.y + 20f),
                            end = Offset(scaleBoxTopLeft.x + 10f + barWidthPx, scaleBoxTopLeft.y + 20f),
                            strokeWidth = 2.5f
                        )
                        drawLine(
                            color = EspressoBrown,
                            start = Offset(scaleBoxTopLeft.x + 10f, scaleBoxTopLeft.y + 15f),
                            end = Offset(scaleBoxTopLeft.x + 10f, scaleBoxTopLeft.y + 22f),
                            strokeWidth = 2.5f
                        )
                        drawLine(
                            color = EspressoBrown,
                            start = Offset(scaleBoxTopLeft.x + 10f + barWidthPx, scaleBoxTopLeft.y + 15f),
                            end = Offset(scaleBoxTopLeft.x + 10f + barWidthPx, scaleBoxTopLeft.y + 22f),
                            strokeWidth = 2.5f
                        )
                        drawText(
                            textLayoutResult = scaleMeas,
                            topLeft = Offset(scaleBoxTopLeft.x + 12f, scaleBoxTopLeft.y + 3f)
                        )
                    }
                }

                // Floating Top Overlay Bar: Active Vehicle Chips + GPS & Popup Fullscreen Button
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Compact Live OSM Status Pill
                        Surface(
                            shape = RoundedCornerShape(50),
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = String.format(java.util.Locale.US, "OSM LIVE Z%.1f • 2-Jari Zoom", smoothZoom),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // GPS Sync Pill
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
                                color = if (isGpsEnabled) VintageGreenBg.copy(alpha = 0.95f) else SoftGoldHighlight.copy(alpha = 0.95f),
                                border = BorderStroke(1.dp, if (isGpsEnabled) VintageGreenSuccess else AntiqueGold),
                                shadowElevation = 3.dp,
                                modifier = Modifier.testTag("gps_sync_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = "Sinkron GPS HP",
                                        tint = if (isGpsEnabled) VintageGreenSuccess else EspressoBrown,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isGpsEnabled) "GPS Aktif" else "GPS HP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGpsEnabled) VintageGreenSuccess else DeepInkBrown,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Button to Launch Almost Full-Screen Map Popup (when viewing inside tab)
                            if (!isInsidePopup && onOpenFullPopup != null) {
                                Surface(
                                    onClick = onOpenFullPopup,
                                    shape = RoundedCornerShape(50),
                                    color = EspressoBrown,
                                    border = BorderStroke(1.2.dp, MetallicGold),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.testTag("open_fullscreen_map_popup_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fullscreen,
                                            contentDescription = "Pop Up Layar Penuh",
                                            tint = MetallicGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Pop Up Penuh",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SoftGoldHighlight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating Vehicle Selector Chips right over the top of the map
                    if (displayTrips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(displayTrips, key = { it.id }) { trip ->
                                val isSelected = selectedTrip?.id == trip.id
                                Surface(
                                    onClick = {
                                        onSelectTrip(trip.id)
                                        followSelectedVehicle = true
                                        centerLat = trip.currentLat
                                        centerLng = trip.currentLng
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) EspressoBrown else VintageCardCream.copy(alpha = 0.94f),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MetallicGold else AntiqueGold
                                    ),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.testTag("map_vehicle_chip_${trip.vehicleId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = vehicleIconFor(trip.vehicleId),
                                            contentDescription = trip.vehicleName,
                                            tint = if (isSelected) MetallicGold else EspressoBrown,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = String.format(
                                                java.util.Locale.US,
                                                "%s • %.2f km",
                                                trip.vehicleName,
                                                trip.totalDistanceTraveledKm
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) SoftGoldHighlight else DeepInkBrown,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right-Side Floating Map Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MapControlButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Perbesar Peta Jalan (Zoom In)",
                        onClick = { targetZoom = (targetZoom + 0.8f).coerceAtMost(17.5f) }
                    )
                    MapControlButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Perkecil Peta Jalan (Zoom Out)",
                        onClick = { targetZoom = (targetZoom - 0.8f).coerceAtLeast(10.5f) }
                    )
                    MapControlButton(
                        icon = Icons.Default.MyLocation,
                        contentDescription = "Fokus ke Kendaraan / Pos Utama",
                        onClick = {
                            followSelectedVehicle = true
                            centerLat = selectedTrip?.currentLat ?: FleetRepository.BASE_LAT
                            centerLng = selectedTrip?.currentLng ?: FleetRepository.BASE_LNG
                            targetZoom = 14.2f
                        }
                    )
                    MapControlButton(
                        icon = Icons.Default.Layers,
                        contentDescription = "Ganti Filter Warna Peta (Vintage / Asli)",
                        onClick = { vintageTintOverlay = !vintageTintOverlay }
                    )
                    MapControlButton(
                        icon = Icons.Default.FastForward,
                        contentDescription = "Percepat Pergerakan & Garis Rute Mobil",
                        onClick = onAdvanceManualStep
                    )
                }

                // Bottom Compact Collapsible Telemetry & Route Recap Overlay Card on top of Map
                if (selectedTrip != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(10.dp)
                            .testTag("selected_trip_telemetry_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = VintageCardCream.copy(alpha = 0.97f)
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = if (selectedTrip.isArrivedBackAtSrGate) VintageGreenSuccess else AntiqueGold
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            // Always-visible compact summary bar with Live Distance & Trail points
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isTelemetryExpanded = !isTelemetryExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selectedTrip.isArrivedBackAtSrGate || selectedTrip.tripStatusEnum == TripStatus.COMPLETED) {
                                                    VintageGreenSuccess
                                                } else {
                                                    EspressoBrown
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = vehicleIconFor(selectedTrip.vehicleId),
                                            contentDescription = selectedTrip.vehicleName,
                                            tint = MetallicGold,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = String.format(
                                                java.util.Locale.US,
                                                "%s (%s) • Jarak: %.2f km",
                                                selectedTrip.vehicleName,
                                                selectedTrip.vehiclePlate,
                                                selectedTrip.totalDistanceTraveledKm
                                            ),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = DeepInkBrown,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when {
                                                selectedTrip.tripStatusEnum == TripStatus.COMPLETED ->
                                                    "Rekap Selesai • Diakhiri Keamanan (${selectedTrip.completedByOfficer.ifBlank { "Pos SR" }})"
                                                selectedTrip.isArrivedBackAtSrGate ->
                                                    "Sudah Kembali di Gerbang SR • Menunggu Diakhiri Keamanan"
                                                else ->
                                                    "Pembawa: ${selectedTrip.driverName} • ${selectedTrip.currentSpeedKmh} km/j • ${selectedTrip.parsedTrailCoordinates.size} titik rute"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selectedTrip.isArrivedBackAtSrGate || selectedTrip.tripStatusEnum == TripStatus.COMPLETED) {
                                                VintageGreenSuccess
                                            } else {
                                                SoftMochaText
                                            },
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        onClick = { isTelemetryExpanded = !isTelemetryExpanded },
                                        shape = RoundedCornerShape(50),
                                        color = VintageParchmentSurface,
                                        border = BorderStroke(1.dp, VintageWarmBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isTelemetryExpanded) "Ringkas" else "Rekap Rute",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EspressoBrown,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = if (isTelemetryExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                                contentDescription = "Buka/Tutup Detail",
                                                tint = EspressoBrown,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(visible = isTelemetryExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Full Round-Trip Route Summary Pill
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = VintageParchmentSurface,
                                        border = BorderStroke(1.dp, VintageWarmBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Route,
                                                        contentDescription = null,
                                                        tint = EspressoBrown,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "REKAP RUTE & GARIS PERJALANAN:",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = EspressoBrown,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = "${(selectedTrip.progressPercent * 100).toInt().coerceIn(0, 100)}% Rute PP",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VintageGreenSuccess,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = selectedTrip.fullRouteSummaryText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DeepInkBrown,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = String.format(
                                                        java.util.Locale.US,
                                                        "Jarak Tempuh: %.2f km",
                                                        selectedTrip.totalDistanceTraveledKm
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EspressoBrown,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Odo: ${selectedTrip.startOdometerKm} ➔ ${selectedTrip.computedEndOdometerKm} km",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SoftMochaText,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { selectedTrip.progressPercent.coerceIn(0.05f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = if (selectedTrip.isArrivedBackAtSrGate || selectedTrip.tripStatusEnum == TripStatus.COMPLETED) {
                                            VintageGreenSuccess
                                        } else {
                                            AntiqueGold
                                        },
                                        trackColor = VintageParchmentSurface
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Navigation,
                                                    contentDescription = "Rute Eksternal",
                                                    tint = EspressoBrown,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Navigasi",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EspressoBrown
                                                )
                                            }

                                            if (selectedTrip.tripStatusEnum == TripStatus.IN_TRANSIT && !selectedTrip.isArrivedBackAtSrGate) {
                                                OutlinedButton(
                                                    onClick = onAdvanceManualStep,
                                                    border = BorderStroke(1.dp, EspressoBrown),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                                    modifier = Modifier.testTag("advance_route_step_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.FastForward,
                                                        contentDescription = "Lanjut Rute",
                                                        tint = EspressoBrown,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Maju Rute",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = EspressoBrown
                                                    )
                                                }
                                            }
                                        }

                                        if (selectedTrip.tripStatusEnum == TripStatus.IN_TRANSIT) {
                                            Button(
                                                onClick = { onCompleteTrip(selectedTrip) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (selectedTrip.isArrivedBackAtSrGate) VintageGreenSuccess else EspressoBrown,
                                                    contentColor = SoftGoldHighlight
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("complete_trip_map_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = "Akhiri oleh Keamanan",
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = if (selectedTrip.isArrivedBackAtSrGate) {
                                                        "Akhiri Kembali di SR (Keamanan)"
                                                    } else {
                                                        "Akhiri Kembali di SR"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
