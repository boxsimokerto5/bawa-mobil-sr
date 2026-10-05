package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateMapOf
import com.example.ui.theme.VintageMapRoad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan

/**
 * Native OpenStreetMap (Slippy Map Web Mercator EPSG:3857) Tile Engine & Projection Math
 * for Jetpack Compose Canvas. Fetches real street map tiles from tile.openstreetmap.org
 * with memory & disk caching and triggers Compose state invalidation as tiles arrive.
 */
object OsmTileStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val inFlightKeys = ConcurrentHashMap.newKeySet<String>()

    // Snapshot-observable map so Compose Canvas automatically redraws when a tile finishes loading
    val loadedBitmaps = mutableStateMapOf<String, Bitmap>()

    fun getOrLoadTile(context: Context, z: Int, x: Int, y: Int): Bitmap? {
        val maxTile = 1 shl z
        val wrappedX = ((x % maxTile) + maxTile) % maxTile
        if (y < 0 || y >= maxTile) return null

        val key = "$z/$wrappedX/$y"
        loadedBitmaps[key]?.let { return it }

        if (inFlightKeys.add(key)) {
            val cacheDir = File(context.cacheDir, "osm_tiles_v1")
            scope.launch {
                try {
                    val tileFile = File(cacheDir, "${z}_${wrappedX}_${y}.png")
                    if (tileFile.exists() && tileFile.length() > 100) {
                        val diskBmp = BitmapFactory.decodeFile(tileFile.absolutePath)
                        if (diskBmp != null) {
                            withContext(Dispatchers.Main) {
                                loadedBitmaps[key] = diskBmp
                            }
                            return@launch
                        }
                    }

                    // Download from official OpenStreetMap tile servers
                    val subdomains = arrayOf("a", "b", "c")
                    val sub = subdomains[(wrappedX + y) % subdomains.size]
                    val urlStr = "https://$sub.tile.openstreetmap.org/$z/$wrappedX/$y.png"
                    val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 7000
                        readTimeout = 7000
                        setRequestProperty(
                            "User-Agent",
                            "BawaMobilSR/1.0 (com.bawamobilsr.gecckocreator; Android OpenStreetMap Client)"
                        )
                    }

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val bytes = connection.inputStream.use { it.readBytes() }
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) {
                            cacheDir.mkdirs()
                            FileOutputStream(tileFile).use { it.write(bytes) }
                            withContext(Dispatchers.Main) {
                                loadedBitmaps[key] = bmp
                            }
                        }
                    }
                    connection.disconnect()
                } catch (_: Exception) {
                    // Gracefully fallback if offline
                } finally {
                    inFlightKeys.remove(key)
                }
            }
        }
        return null
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
