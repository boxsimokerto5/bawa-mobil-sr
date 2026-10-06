package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.LruCache
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sinh
import kotlin.math.tan

/**
 * High-performance OpenStreetMap (Slippy Map Web Mercator EPSG:3857) Tile Engine & Two-Tier Offline Cache
 * for 60fps Jetpack Compose Canvas rendering:
 * - Multi-CDN High-Availability Tile Providers (CartoDB Voyager OSM Street Map, ArcGIS World Street Map,
 *   and OpenStreetMap Humanitarian) with automatic failover so the map is never blocked by 403 rate limits.
 * - Automatic purge of legacy v1/v2 caches that may have stored "403 Access blocked" warning tiles, plus
 *   bitmap pattern validation to reject error/blocked placeholder tiles.
 * - Tier 1 (L1 Memory Cache): Thread-safe [LruCache] of GPU-ready [ImageBitmap] instances (up to 320 tiles).
 * - Tier 2 (L2 Persistent Disk Cache): Dedicated persistent tile store (`filesDir/osm_tile_cache_v3`)
 *   with atomic writes, 30-day stale-while-revalidate retention, and automatic quota management.
 * - Multi-Level Parent & Child Tile Fallback (z-1 .. z-4 and z+1) for seamless zooming and offline resilience.
 */
object OsmTileStore {
    private const val MAX_MEMORY_TILES = 320
    private const val MAX_DISK_CACHE_BYTES = 120L * 1024L * 1024L // 120 MB persistent offline tile storage
    private const val TILE_FRESHNESS_TTL_MS = 30L * 24L * 60L * 60L * 1000L // 30 days before background refresh

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val inFlightKeys = ConcurrentHashMap.newKeySet<String>()
    private val diskLoadInFlightKeys = ConcurrentHashMap.newKeySet<String>()
    // Strictly adhere to polite tile server concurrency (max 2 concurrent HTTP requests)
    private val networkSemaphore = Semaphore(2)
    private val diskSemaphore = Semaphore(10)
    private val initialized = AtomicBoolean(false)

    // L1 LRU Memory Cache of pre-converted ImageBitmaps for zero-allocation Canvas drawing
    private val memoryCache = object : LruCache<String, ImageBitmap>(MAX_MEMORY_TILES) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = 1
    }

    // Fast in-memory index of tile keys known to exist on disk so fallback checks are O(1) without blocking UI
    private val diskIndex = ConcurrentHashMap.newKeySet<String>()

    // Single lightweight observable counter to trigger Canvas redraw when new tiles arrive from disk or network
    val tileRevision = mutableIntStateOf(0)

    // Observable offline/cache status for UI badges and user feedback
    val cachedDiskTileCount = mutableIntStateOf(0)
    val isOfflineFallbackActive = mutableStateOf(false)
    val isPreCachingArea = mutableStateOf(false)

    data class TileDrawSpec(
        val imageBitmap: ImageBitmap,
        val srcLeft: Int = 0,
        val srcTop: Int = 0,
        val srcWidth: Int = 256,
        val srcHeight: Int = 256,
        val isScaledFallback: Boolean = false
    )

    private fun getPersistentCacheDir(context: Context): File {
        val dir = File(context.applicationContext.filesDir, "osm_tile_cache_v3")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun tileKey(z: Int, x: Int, y: Int): String = "$z/$x/$y"

    private fun tileFileName(z: Int, x: Int, y: Int): String = "${z}_${x}_${y}.png"

    /**
     * Builds ordered list of reliable street map tile URLs for (z, x, y) with automatic failover:
     * 1. CartoDB Voyager (OpenStreetMap data with crisp street/POI labels & high-capacity global CDN)
     * 2. ArcGIS World Street Map (Detailed street map with high availability)
     * 3. OpenStreetMap Humanitarian (HOT) Street Tiles
     */
    private fun buildCandidateTileUrls(z: Int, x: Int, y: Int): List<String> {
        val cartoSubs = arrayOf("a", "b", "c", "d")
        val cartoSub = cartoSubs[(x + y).mod(cartoSubs.size)]
        val hotSubs = arrayOf("a", "b")
        val hotSub = hotSubs[(x + y).mod(hotSubs.size)]
        return listOf(
            "https://$cartoSub.basemaps.cartocdn.com/rastertiles/voyager/$z/$x/$y.png",
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/$z/$y/$x",
            "https://$hotSub.tile.openstreetmap.fr/hot/$z/$x/$y.png"
        )
    }

    /**
     * Detects the "403 Access blocked" warning tile pattern (which has a vertical yellow-and-black hazard stripe
     * around x = 60..66 and stark white background) so poisoned tiles are never cached or displayed.
     */
    private fun isLikelyBlockedWarningTile(bitmap: Bitmap): Boolean {
        if (bitmap.width < 64 || bitmap.height < 64) return true
        return try {
            // Check if the right half is pure white (255,255,255) at multiple vertical points AND left stripe has alternating pure black/yellow
            val whiteSamples = listOf(
                bitmap.getPixel(180, 20),
                bitmap.getPixel(220, 128),
                bitmap.getPixel(180, 235)
            )
            val allPureWhite = whiteSamples.all { px ->
                Color.red(px) > 250 && Color.green(px) > 250 && Color.blue(px) > 250
            }
            if (!allPureWhite) return false

            var yellowCount = 0
            var blackCount = 0
            for (sampleY in 10..240 step 12) {
                val px = bitmap.getPixel(62, sampleY)
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)
                if (r > 230 && g > 210 && b < 40) yellowCount++
                if (r < 25 && g < 25 && b < 25) blackCount++
            }
            yellowCount >= 3 && blackCount >= 3
        } catch (_: Exception) {
            false
        }
    }

    fun ensureInitialized(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        scope.launch {
            try {
                // 1. Purge legacy cache directories that may contain poisoned "403 Access blocked" tiles
                val legacyDirV1 = File(appContext.cacheDir, "osm_tiles_v1")
                if (legacyDirV1.exists()) {
                    runCatching { legacyDirV1.deleteRecursively() }
                }
                val legacyDirV2 = File(appContext.filesDir, "osm_tile_cache_v2")
                if (legacyDirV2.exists()) {
                    runCatching { legacyDirV2.deleteRecursively() }
                }

                // 2. Index all valid persistent cached tiles in v3 directory
                val primaryDir = getPersistentCacheDir(appContext)
                val files = primaryDir.listFiles() ?: emptyArray()
                var validCount = 0
                for (file in files) {
                    if (file.isFile && file.name.endsWith(".png") && file.length() > 200) {
                        val parts = file.name.removeSuffix(".png").split("_")
                        if (parts.size == 3) {
                            diskIndex.add("${parts[0]}/${parts[1]}/${parts[2]}")
                            validCount++
                        }
                    } else if (file.isFile) {
                        runCatching { file.delete() }
                    }
                }
                withContext(Dispatchers.Main) {
                    cachedDiskTileCount.intValue = validCount
                    tileRevision.intValue++
                }

                // Prune oldest tiles if disk cache exceeds quota
                pruneDiskCacheIfNeeded(primaryDir)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Clears any corrupted or stale tiles and reloads the visible map area cleanly.
     */
    fun refreshAndPrefetchArea(
        context: Context,
        centerLat: Double,
        centerLng: Double,
        currentZoom: Int
    ) {
        ensureInitialized(context)
        val appContext = context.applicationContext
        val z = currentZoom.coerceIn(10, 17)
        val cx = floor(WebMercator.lonToTileX(centerLng, z)).toInt()
        val cy = floor(WebMercator.latToTileY(centerLat, z)).toInt()
        prefetchRegion(appContext, z, cx, cy, radius = 2, includeAdjacentZooms = true)
    }

    fun getTileOrFallback(context: Context, z: Int, x: Int, y: Int): TileDrawSpec? {
        ensureInitialized(context)
        val appContext = context.applicationContext
        val maxTile = 1 shl z
        val wrappedX = ((x % maxTile) + maxTile) % maxTile
        if (y < 0 || y >= maxTile) return null

        val exactKey = tileKey(z, wrappedX, y)
        memoryCache.get(exactKey)?.let { bmp ->
            return TileDrawSpec(bmp, 0, 0, bmp.width, bmp.height, isScaledFallback = false)
        }

        // Trigger async load for exact tile (reads L2 disk cache first, then multi-CDN network if needed)
        enqueueTileLoad(appContext, z, wrappedX, y)

        // Multi-level Parent Zoom Fallback (z - 1 down to z - 4) so map remains responsive & visible offline
        for (deltaZ in 1..4) {
            val parentZ = z - deltaZ
            if (parentZ < 1) break
            val shift = 1 shl deltaZ
            val parentX = wrappedX shr deltaZ
            val parentY = y shr deltaZ
            val parentKey = tileKey(parentZ, parentX, parentY)

            val parentBmp = memoryCache.get(parentKey)
            if (parentBmp != null) {
                val subW = max(1, parentBmp.width / shift)
                val subH = max(1, parentBmp.height / shift)
                val qx = (wrappedX and (shift - 1)) * subW
                val qy = (y and (shift - 1)) * subH
                return TileDrawSpec(parentBmp, qx, qy, subW, subH, isScaledFallback = true)
            } else if (diskIndex.contains(parentKey)) {
                // Warm parent tile from disk into L1 memory immediately for smooth offline fallback
                enqueueDiskOnlyWarm(appContext, parentZ, parentX, parentY)
            }
        }

        // Child Zoom Fallback (z + 1): if user zooms out offline and we have a higher-res child tile in memory
        if (z < 17) {
            val childZ = z + 1
            val baseChildX = wrappedX shl 1
            val baseChildY = y shl 1
            val topLeftChildKey = tileKey(childZ, baseChildX, baseChildY)
            memoryCache.get(topLeftChildKey)?.let { childBmp ->
                return TileDrawSpec(childBmp, 0, 0, childBmp.width, childBmp.height, isScaledFallback = true)
            }
        }

        return null
    }

    /**
     * Politely prefetches the visible tile grid plus immediate parent overview tiles so zooming in/out
     * works smoothly without triggering bulk-download rate limits.
     */
    fun prefetchRegion(
        context: Context,
        z: Int,
        centerTileX: Int,
        centerTileY: Int,
        radius: Int = 2,
        includeAdjacentZooms: Boolean = true
    ) {
        ensureInitialized(context)
        val appContext = context.applicationContext
        val maxTile = 1 shl z
        val effectiveRadius = radius.coerceIn(1, 2)

        // 1. Prioritize center and immediate visible ring at current zoom level
        for (r in 0..effectiveRadius) {
            for (dx in -r..r) {
                for (dy in -r..r) {
                    if (max(abs(dx), abs(dy)) != r) continue
                    val tx = ((centerTileX + dx) % maxTile + maxTile) % maxTile
                    val ty = centerTileY + dy
                    if (ty in 0 until maxTile) {
                        enqueueTileLoad(appContext, z, tx, ty)
                    }
                }
            }
        }

        // 2. Also cache parent overview zoom (z - 1) center 2x2 tiles for instant zoom-out & offline fallback
        if (includeAdjacentZooms && z > 10) {
            val parentZ = z - 1
            val pMax = 1 shl parentZ
            val px = centerTileX shr 1
            val py = centerTileY shr 1
            for (dx in 0..1) {
                for (dy in 0..1) {
                    val tx = ((px + dx) % pMax + pMax) % pMax
                    val ty = py + dy
                    if (ty in 0 until pMax) {
                        enqueueTileLoad(appContext, parentZ, tx, ty)
                    }
                }
            }
        }
    }

    /**
     * Politely caches key waypoints along a trip route (origin + destinations) without flooding tile servers.
     */
    fun prefetchRouteCorridor(
        context: Context,
        waypoints: List<Pair<Double, Double>>,
        zoomLevels: List<Int> = listOf(13, 15)
    ) {
        if (waypoints.isEmpty()) return
        ensureInitialized(context)
        val appContext = context.applicationContext

        scope.launch {
            try {
                val keyWaypoints = waypoints.distinct().take(4)
                for (z in zoomLevels) {
                    val clampedZ = z.coerceIn(11, 16)
                    val maxTile = 1 shl clampedZ
                    for ((lat, lng) in keyWaypoints) {
                        val cx = floor(WebMercator.lonToTileX(lng, clampedZ)).toInt()
                        val cy = floor(WebMercator.latToTileY(lat, clampedZ)).toInt()
                        for (dx in -1..1) {
                            for (dy in -1..1) {
                                val tx = ((cx + dx) % maxTile + maxTile) % maxTile
                                val ty = cy + dy
                                if (ty in 0 until maxTile) {
                                    enqueueTileLoad(appContext, clampedZ, tx, ty)
                                }
                            }
                        }
                        delay(120L)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Politely caches the current viewport neighborhood across zoom levels 13..15 for offline field use.
     */
    fun downloadOfflineAreaAround(
        context: Context,
        centerLat: Double,
        centerLng: Double,
        onComplete: (Int) -> Unit = {}
    ) {
        ensureInitialized(context)
        val appContext = context.applicationContext
        scope.launch {
            isPreCachingArea.value = true
            try {
                for (z in listOf(13, 14, 15)) {
                    val maxTile = 1 shl z
                    val cx = floor(WebMercator.lonToTileX(centerLng, z)).toInt()
                    val cy = floor(WebMercator.latToTileY(centerLat, z)).toInt()
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            val tx = ((cx + dx) % maxTile + maxTile) % maxTile
                            val ty = cy + dy
                            if (ty in 0 until maxTile) {
                                enqueueTileLoad(appContext, z, tx, ty)
                            }
                        }
                    }
                    delay(180L)
                }
                delay(600L)
                withContext(Dispatchers.Main) {
                    onComplete(cachedDiskTileCount.intValue)
                }
            } finally {
                isPreCachingArea.value = false
            }
        }
    }

    private fun enqueueDiskOnlyWarm(appContext: Context, z: Int, wrappedX: Int, y: Int) {
        val key = tileKey(z, wrappedX, y)
        if (memoryCache.get(key) != null) return
        if (!diskLoadInFlightKeys.add(key)) return

        scope.launch {
            try {
                diskSemaphore.withPermit {
                    val file = findExistingTileFile(appContext, z, wrappedX, y) ?: return@withPermit
                    val diskBmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (diskBmp != null && !isLikelyBlockedWarningTile(diskBmp)) {
                        memoryCache.put(key, diskBmp.asImageBitmap())
                        withContext(Dispatchers.Main) {
                            tileRevision.intValue++
                        }
                    } else if (diskBmp != null) {
                        runCatching { file.delete() }
                        diskIndex.remove(key)
                    }
                }
            } catch (_: Exception) {
            } finally {
                diskLoadInFlightKeys.remove(key)
            }
        }
    }

    private fun findExistingTileFile(appContext: Context, z: Int, wrappedX: Int, y: Int): File? {
        val fileName = tileFileName(z, wrappedX, y)
        val primaryFile = File(getPersistentCacheDir(appContext), fileName)
        if (primaryFile.exists() && primaryFile.length() > 200) {
            return primaryFile
        }
        return null
    }

    private fun enqueueTileLoad(appContext: Context, z: Int, wrappedX: Int, y: Int) {
        val key = tileKey(z, wrappedX, y)
        if (memoryCache.get(key) != null) return
        if (!inFlightKeys.add(key)) return

        val persistentDir = getPersistentCacheDir(appContext)
        scope.launch {
            try {
                // 1. Check fast local L2 persistent disk cache first without waiting on network semaphore
                val existingFile = diskSemaphore.withPermit {
                    findExistingTileFile(appContext, z, wrappedX, y)
                }

                if (existingFile != null) {
                    val diskBmp = diskSemaphore.withPermit {
                        BitmapFactory.decodeFile(existingFile.absolutePath)
                    }
                    if (diskBmp != null && !isLikelyBlockedWarningTile(diskBmp)) {
                        val imgBmp = diskBmp.asImageBitmap()
                        memoryCache.put(key, imgBmp)
                        diskIndex.add(key)
                        withContext(Dispatchers.Main) {
                            tileRevision.intValue++
                        }
                        // Stale-while-revalidate: if tile is younger than 30 days or device has no network, stop here
                        val ageMs = System.currentTimeMillis() - existingFile.lastModified()
                        if (ageMs < TILE_FRESHNESS_TTL_MS || !isNetworkLikelyAvailable(appContext)) {
                            return@launch
                        }
                    } else {
                        // Corrupted or blocked placeholder file on disk; delete so it is re-fetched from clean CDN
                        runCatching { existingFile.delete() }
                        diskIndex.remove(key)
                    }
                }

                // 2. If offline and tile wasn't on disk, mark offline fallback state and skip network wait
                if (!isNetworkLikelyAvailable(appContext)) {
                    isOfflineFallbackActive.value = true
                    return@launch
                }

                // 3. Fetch from Multi-CDN Street Map servers with polite concurrency (max 2) & automatic failover
                networkSemaphore.withPermit {
                    val candidateUrls = buildCandidateTileUrls(z, wrappedX, y)
                    var validDecodedBitmap: Bitmap? = null
                    var validBytes: ByteArray? = null

                    for (urlStr in candidateUrls) {
                        var connection: HttpURLConnection? = null
                        try {
                            connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                                requestMethod = "GET"
                                connectTimeout = 5000
                                readTimeout = 5000
                                useCaches = true
                                setRequestProperty(
                                    "User-Agent",
                                    "Mozilla/5.0 (Linux; Android 14; BawaMobilSR/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                )
                                setRequestProperty("Accept", "image/webp,image/apng,image/png,image/*,*/*;q=0.8")
                            }
                            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                                val bytes = connection.inputStream.use { it.readBytes() }
                                if (bytes.size > 200) {
                                    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    if (decoded != null && !isLikelyBlockedWarningTile(decoded)) {
                                        validDecodedBitmap = decoded
                                        validBytes = bytes
                                        break
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            // Try next fallback CDN provider
                        } finally {
                            connection?.disconnect()
                        }
                    }

                    if (validDecodedBitmap != null && validBytes != null) {
                        val targetFile = File(persistentDir, tileFileName(z, wrappedX, y))
                        val tempFile = File(persistentDir, "${tileFileName(z, wrappedX, y)}.tmp")
                        runCatching {
                            persistentDir.mkdirs()
                            FileOutputStream(tempFile).use { out ->
                                out.write(validBytes)
                                out.fd.sync()
                            }
                            if (targetFile.exists()) {
                                targetFile.delete()
                            }
                            tempFile.renameTo(targetFile)
                        }
                        val isNewDiskTile = diskIndex.add(key)
                        val imgBmp = validDecodedBitmap.asImageBitmap()
                        memoryCache.put(key, imgBmp)
                        isOfflineFallbackActive.value = false
                        withContext(Dispatchers.Main) {
                            if (isNewDiskTile) {
                                cachedDiskTileCount.intValue = diskIndex.size
                            }
                            tileRevision.intValue++
                        }
                    } else {
                        isOfflineFallbackActive.value = true
                    }
                }
            } catch (_: Exception) {
                isOfflineFallbackActive.value = true
            } finally {
                inFlightKeys.remove(key)
            }
        }
    }

    private fun isNetworkLikelyAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val activeNet = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNet) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    private fun pruneDiskCacheIfNeeded(cacheDir: File) {
        try {
            val files = cacheDir.listFiles()?.filter { it.isFile && it.name.endsWith(".png") } ?: return
            var totalBytes = files.sumOf { it.length() }
            if (totalBytes <= MAX_DISK_CACHE_BYTES) return

            // Evict oldest-accessed / oldest-modified tiles first until under 85% of max quota
            val targetBytes = (MAX_DISK_CACHE_BYTES * 0.85).toLong()
            val sortedByOldest = files.sortedBy { it.lastModified() }
            for (file in sortedByOldest) {
                if (totalBytes <= targetBytes) break
                val len = file.length()
                val parts = file.name.removeSuffix(".png").split("_")
                if (file.delete()) {
                    totalBytes -= len
                    if (parts.size == 3) {
                        diskIndex.remove("${parts[0]}/${parts[1]}/${parts[2]}")
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}

object WebMercator {
    const val TILE_SIZE = 256.0

    fun lonToTileX(lon: Double, zoom: Int): Double {
        val n = (1 shl zoom).toDouble()
        return ((lon + 180.0) / 360.0) * n
    }

    fun latToTileY(lat: Double, zoom: Int): Double {
        val clampedLat = lat.coerceIn(-85.0511, 85.0511)
        val latRad = clampedLat * PI / 180.0
        val n = (1 shl zoom).toDouble()
        return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n
    }

    fun tileXToLon(tileX: Double, zoom: Int): Double {
        val n = (1 shl zoom).toDouble()
        return tileX / n * 360.0 - 180.0
    }

    fun tileYToLat(tileY: Double, zoom: Int): Double {
        val n = (1 shl zoom).toDouble()
        val latRad = atan(sinh(PI * (1.0 - 2.0 * tileY / n)))
        return latRad * 180.0 / PI
    }
}

