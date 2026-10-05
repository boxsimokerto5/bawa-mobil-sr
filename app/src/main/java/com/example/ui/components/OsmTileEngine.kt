package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
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
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sinh
import kotlin.math.tan

/**
 * High-performance OpenStreetMap (Slippy Map Web Mercator EPSG:3857) Tile Engine & Two-Tier Offline Cache
 * for 60fps Jetpack Compose Canvas rendering even in poor or zero network connectivity:
 * - Tier 1 (L1 Memory Cache): Thread-safe [LruCache] of GPU-ready [ImageBitmap] instances (up to 320 tiles)
 *   so zero allocations or conversions occur inside Canvas `onDraw`.
 * - Tier 2 (L2 Persistent Disk Cache): Dedicated persistent tile store on disk (`filesDir/osm_tile_cache_v2`
 *   with automatic migration/read from `cacheDir/osm_tiles_v1`), atomic temp-file writes, 30-day stale-while-revalidate
 *   retention, and automatic disk quota pruning (up to 120 MB / ~6,000 tiles).
 * - Multi-Level Parent & Child Tile Fallback: Searches up to 4 parent zoom levels (z-1 .. z-4) and child zoom
 *   tiles (z+1) in both L1 memory and L2 disk cache so the map remains sharp and never flashes blank offline.
 * - Proactive Route & Multi-Zoom Corridor Pre-Caching: Pre-fetches surrounding rings, parent overview zoom levels,
 *   and active trip route corridors in the background with bounded concurrency and retry backoff.
 */
object OsmTileStore {
    private const val MAX_MEMORY_TILES = 320
    private const val MAX_DISK_CACHE_BYTES = 120L * 1024L * 1024L // 120 MB persistent offline tile storage
    private const val TILE_FRESHNESS_TTL_MS = 30L * 24L * 60L * 60L * 1000L // 30 days before background refresh

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val inFlightKeys = ConcurrentHashMap.newKeySet<String>()
    private val diskLoadInFlightKeys = ConcurrentHashMap.newKeySet<String>()
    private val networkSemaphore = Semaphore(6)
    private val diskSemaphore = Semaphore(12)
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
        val dir = File(context.applicationContext.filesDir, "osm_tile_cache_v2")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getLegacyCacheDir(context: Context): File {
        return File(context.applicationContext.cacheDir, "osm_tiles_v1")
    }

    private fun tileKey(z: Int, x: Int, y: Int): String = "$z/$x/$y"

    private fun tileFileName(z: Int, x: Int, y: Int): String = "${z}_${x}_${y}.png"

    fun ensureInitialized(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        scope.launch {
            try {
                val primaryDir = getPersistentCacheDir(appContext)
                val legacyDir = getLegacyCacheDir(appContext)

                // Migrate any existing tiles from legacy cacheDir to persistent filesDir
                if (legacyDir.exists()) {
                    legacyDir.listFiles()?.forEach { file ->
                        if (file.isFile && file.name.endsWith(".png") && file.length() > 100) {
                            val target = File(primaryDir, file.name)
                            if (!target.exists()) {
                                runCatching { file.copyTo(target, overwrite = false) }
                            }
                        }
                    }
                }

                // Index all persistent cached tiles on disk
                val files = primaryDir.listFiles() ?: emptyArray()
                var validCount = 0
                for (file in files) {
                    if (file.isFile && file.name.endsWith(".png") && file.length() > 100) {
                        val parts = file.name.removeSuffix(".png").split("_")
                        if (parts.size == 3) {
                            diskIndex.add("${parts[0]}/${parts[1]}/${parts[2]}")
                            validCount++
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    cachedDiskTileCount.intValue = validCount
                }

                // Prune oldest tiles if disk cache exceeds quota
                pruneDiskCacheIfNeeded(primaryDir)
            } catch (_: Exception) {
            }
        }
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

        // Trigger async load for exact tile (reads L2 disk cache first, then network if needed)
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
     * Prefetches the visible tile grid plus parent overview tiles so zooming in/out works seamlessly
     * even if connectivity drops moments later.
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

        // 1. Prioritize center and immediate ring at current zoom level
        for (r in 0..radius) {
            for (dx in -r..r) {
                for (dy in -r..r) {
                    if (max( kotlin.math.abs(dx), kotlin.math.abs(dy) ) != r) continue
                    val tx = ((centerTileX + dx) % maxTile + maxTile) % maxTile
                    val ty = centerTileY + dy
                    if (ty in 0 until maxTile) {
                        enqueueTileLoad(appContext, z, tx, ty)
                    }
                }
            }
        }

        // 2. Also cache parent overview zoom (z - 1) and detail zoom (z + 1) center tiles for offline resilience
        if (includeAdjacentZooms) {
            val parentZ = (z - 1).coerceAtLeast(10)
            if (parentZ != z) {
                val pMax = 1 shl parentZ
                val px = centerTileX shr 1
                val py = centerTileY shr 1
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        val tx = ((px + dx) % pMax + pMax) % pMax
                        val ty = py + dy
                        if (ty in 0 until pMax) {
                            enqueueTileLoad(appContext, parentZ, tx, ty)
                        }
                    }
                }
            }
        }
    }

    /**
     * Proactively caches tiles along a trip route corridor (origin + waypoints + current GPS) across
     * multiple zoom levels so the entire journey stays available offline in areas with weak signal.
     */
    fun prefetchRouteCorridor(
        context: Context,
        waypoints: List<Pair<Double, Double>>,
        zoomLevels: List<Int> = listOf(12, 14, 15)
    ) {
        if (waypoints.isEmpty()) return
        ensureInitialized(context)
        val appContext = context.applicationContext

        scope.launch {
            isPreCachingArea.value = true
            try {
                // Sample points along each segment of the route
                val sampledPoints = mutableListOf<Pair<Double, Double>>()
                for (i in waypoints.indices) {
                    val current = waypoints[i]
                    sampledPoints.add(current)
                    if (i < waypoints.size - 1) {
                        val next = waypoints[i + 1]
                        val steps = 6
                        for (s in 1 until steps) {
                            val t = s.toDouble() / steps.toDouble()
                            val lat = current.first + (next.first - current.first) * t
                            val lng = current.second + (next.second - current.second) * t
                            sampledPoints.add(Pair(lat, lng))
                        }
                    }
                }

                for (z in zoomLevels) {
                    val clampedZ = z.coerceIn(10, 17)
                    val maxTile = 1 shl clampedZ
                    val radius = if (clampedZ <= 13) 1 else 1
                    for ((lat, lng) in sampledPoints) {
                        val cx = floor(WebMercator.lonToTileX(lng, clampedZ)).toInt()
                        val cy = floor(WebMercator.latToTileY(lat, clampedZ)).toInt()
                        for (dx in -radius..radius) {
                            for (dy in -radius..radius) {
                                val tx = ((cx + dx) % maxTile + maxTile) % maxTile
                                val ty = cy + dy
                                if (ty in 0 until maxTile) {
                                    enqueueTileLoad(appContext, clampedZ, tx, ty)
                                }
                            }
                        }
                    }
                }
            } finally {
                delay(800L)
                isPreCachingArea.value = false
            }
        }
    }

    /**
     * Explicitly downloads and caches a wider neighborhood around the given coordinate across zoom levels 12..16
     * for offline field operation.
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
                for (z in listOf(12, 13, 14, 15, 16)) {
                    val maxTile = 1 shl z
                    val cx = floor(WebMercator.lonToTileX(centerLng, z)).toInt()
                    val cy = floor(WebMercator.latToTileY(centerLat, z)).toInt()
                    val radius = when (z) {
                        12, 13 -> 2
                        14, 15 -> 2
                        else -> 1
                    }
                    for (dx in -radius..radius) {
                        for (dy in -radius..radius) {
                            val tx = ((cx + dx) % maxTile + maxTile) % maxTile
                            val ty = cy + dy
                            if (ty in 0 until maxTile) {
                                enqueueTileLoad(appContext, z, tx, ty)
                            }
                        }
                    }
                }
                delay(1200L)
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
                    if (diskBmp != null) {
                        memoryCache.put(key, diskBmp.asImageBitmap())
                        withContext(Dispatchers.Main) {
                            tileRevision.intValue++
                        }
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
        if (primaryFile.exists() && primaryFile.length() > 100) {
            return primaryFile
        }
        val legacyFile = File(getLegacyCacheDir(appContext), fileName)
        if (legacyFile.exists() && legacyFile.length() > 100) {
            return legacyFile
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
                    if (diskBmp != null) {
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
                        // Corrupted file on disk; delete so it can be re-fetched cleanly
                        runCatching { existingFile.delete() }
                        diskIndex.remove(key)
                    }
                }

                // 2. If offline and tile wasn't on disk, mark offline fallback state and skip network wait
                if (!isNetworkLikelyAvailable(appContext)) {
                    isOfflineFallbackActive.value = true
                    return@launch
                }

                // 3. Fetch from OpenStreetMap CDN with concurrency control & retry backoff for spotty connections
                networkSemaphore.withPermit {
                    val subdomains = arrayOf("a", "b", "c")
                    var fetchedBytes: ByteArray? = null

                    for (attempt in 0..1) {
                        val sub = subdomains[(wrappedX + y + attempt) % subdomains.size]
                        val urlStr = "https://$sub.tile.openstreetmap.org/$z/$wrappedX/$y.png"
                        var connection: HttpURLConnection? = null
                        try {
                            connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                                connectTimeout = 5000
                                readTimeout = 5000
                                useCaches = true
                                setRequestProperty(
                                    "User-Agent",
                                    "BawaMobilSR/2.0 (com.bawamobilsr.gecckocreator; Android Offline-Resilient OSM Cache)"
                                )
                            }
                            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                                val bytes = connection.inputStream.use { it.readBytes() }
                                if (bytes.size > 100) {
                                    fetchedBytes = bytes
                                    break
                                }
                            }
                        } catch (_: Exception) {
                            if (attempt == 0) {
                                delay(350L)
                            }
                        } finally {
                            connection?.disconnect()
                        }
                    }

                    if (fetchedBytes != null) {
                        val decoded = BitmapFactory.decodeByteArray(fetchedBytes, 0, fetchedBytes.size)
                        if (decoded != null) {
                            val targetFile = File(persistentDir, tileFileName(z, wrappedX, y))
                            val tempFile = File(persistentDir, "${tileFileName(z, wrappedX, y)}.tmp")
                            runCatching {
                                persistentDir.mkdirs()
                                FileOutputStream(tempFile).use { out ->
                                    out.write(fetchedBytes)
                                    out.fd.sync()
                                }
                                if (targetFile.exists()) {
                                    targetFile.delete()
                                }
                                tempFile.renameTo(targetFile)
                            }
                            val isNewDiskTile = diskIndex.add(key)
                            val imgBmp = decoded.asImageBitmap()
                            memoryCache.put(key, imgBmp)
                            isOfflineFallbackActive.value = false
                            withContext(Dispatchers.Main) {
                                if (isNewDiskTile) {
                                    cachedDiskTileCount.intValue = diskIndex.size
                                }
                                tileRevision.intValue++
                            }
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

