package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sinh
import kotlin.math.tan

/**
 * High-performance OpenStreetMap (Slippy Map Web Mercator EPSG:3857) Tile Engine & Projection Math
 * for 60fps Jetpack Compose Canvas rendering:
 * - Caches GPU-ready [ImageBitmap] instances in a thread-safe map so zero conversions occur in onDraw.
 * - Uses parent/child tile fallback during zoom transitions so the map never flashes blank.
 * - Prefetches surrounding ring tiles in the background with bounded concurrency.
 */
object OsmTileStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val inFlightKeys = ConcurrentHashMap.newKeySet<String>()
    private val networkSemaphore = Semaphore(8)

    // Pre-converted ImageBitmaps for zero-allocation Canvas drawing
    private val memoryCache = ConcurrentHashMap<String, ImageBitmap>()

    // Single lightweight observable counter to trigger Canvas redraw when new tiles arrive
    val tileRevision = mutableIntStateOf(0)

    data class TileDrawSpec(
        val imageBitmap: ImageBitmap,
        val srcLeft: Int = 0,
        val srcTop: Int = 0,
        val srcWidth: Int = 256,
        val srcHeight: Int = 256
    )

    fun getTileOrFallback(context: Context, z: Int, x: Int, y: Int): TileDrawSpec? {
        val maxTile = 1 shl z
        val wrappedX = ((x % maxTile) + maxTile) % maxTile
        if (y < 0 || y >= maxTile) return null

        val exactKey = "$z/$wrappedX/$y"
        memoryCache[exactKey]?.let { bmp ->
            return TileDrawSpec(bmp, 0, 0, bmp.width, bmp.height)
        }

        // Trigger async load for exact tile
        enqueueTileLoad(context.applicationContext, z, wrappedX, y)

        // Fallback 1: Use parent zoom level tile (z - 1) cropped to the corresponding quadrant
        if (z > 1) {
            val parentZ = z - 1
            val parentX = wrappedX shr 1
            val parentY = y shr 1
            val parentKey = "$parentZ/$parentX/$parentY"
            memoryCache[parentKey]?.let { parentBmp ->
                val halfW = parentBmp.width / 2
                val halfH = parentBmp.height / 2
                val qx = (wrappedX and 1) * halfW
                val qy = (y and 1) * halfH
                return TileDrawSpec(parentBmp, qx, qy, halfW, halfH)
            }
        }

        // Fallback 2: Use grandparent zoom level tile (z - 2) cropped to 1/4 quadrant
        if (z > 2) {
            val gpZ = z - 2
            val gpX = wrappedX shr 2
            val gpY = y shr 2
            val gpKey = "$gpZ/$gpX/$gpY"
            memoryCache[gpKey]?.let { gpBmp ->
                val qW = gpBmp.width / 4
                val qH = gpBmp.height / 4
                val qx = (wrappedX and 3) * qW
                val qy = (y and 3) * qH
                return TileDrawSpec(gpBmp, qx, qy, qW, qH)
            }
        }

        return null
    }

    fun prefetchRegion(context: Context, z: Int, centerTileX: Int, centerTileY: Int, radius: Int = 2) {
        val appContext = context.applicationContext
        val maxTile = 1 shl z
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val tx = ((centerTileX + dx) % maxTile + maxTile) % maxTile
                val ty = centerTileY + dy
                if (ty in 0 until maxTile) {
                    enqueueTileLoad(appContext, z, tx, ty)
                }
            }
        }
    }

    private fun enqueueTileLoad(appContext: Context, z: Int, wrappedX: Int, y: Int) {
        val key = "$z/$wrappedX/$y"
        if (memoryCache.containsKey(key)) return
        if (!inFlightKeys.add(key)) return

        val cacheDir = File(appContext.cacheDir, "osm_tiles_v1")
        scope.launch {
            try {
                // 1. Check fast local disk cache first without network semaphore
                val tileFile = File(cacheDir, "${z}_${wrappedX}_${y}.png")
                if (tileFile.exists() && tileFile.length() > 100) {
                    val diskBmp = BitmapFactory.decodeFile(tileFile.absolutePath)
                    if (diskBmp != null) {
                        val imgBmp = diskBmp.asImageBitmap()
                        memoryCache[key] = imgBmp
                        withContext(Dispatchers.Main) {
                            tileRevision.intValue++
                        }
                        return@launch
                    }
                }

                // 2. Fetch from OpenStreetMap CDN with concurrency control
                networkSemaphore.withPermit {
                    val subdomains = arrayOf("a", "b", "c")
                    val sub = subdomains[(wrappedX + y) % subdomains.size]
                    val urlStr = "https://$sub.tile.openstreetmap.org/$z/$wrappedX/$y.png"
                    val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 6000
                        readTimeout = 6000
                        setRequestProperty(
                            "User-Agent",
                            "BawaMobilSR/1.0 (com.bawamobilsr.gecckocreator; Android Smooth OSM Engine)"
                        )
                    }

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val bytes = connection.inputStream.use { it.readBytes() }
                        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (decoded != null) {
                            cacheDir.mkdirs()
                            FileOutputStream(tileFile).use { it.write(bytes) }
                            val imgBmp = decoded.asImageBitmap()
                            memoryCache[key] = imgBmp
                            withContext(Dispatchers.Main) {
                                tileRevision.intValue++
                            }
                        }
                    }
                    connection.disconnect()
                }
            } catch (_: Exception) {
                // Ignore transient network errors
            } finally {
                inFlightKeys.remove(key)
            }
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
