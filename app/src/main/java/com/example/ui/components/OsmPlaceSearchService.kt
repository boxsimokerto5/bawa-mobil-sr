package com.example.ui.components

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class OsmPlaceSuggestion(
    val title: String,
    val addressSubtitle: String,
    val latitude: Double,
    val longitude: Double,
    val categoryBadge: String = "OpenStreetMap"
)

/**
 * Provides instant + real-time OpenStreetMap (Nominatim) place autocomplete suggestions.
 * Combines a rich curated Indonesian & East Java POI directory (including RS SLG / RSUD Simpang Lima Gumul,
 * hospitals, airports, stations, campuses, government offices, and industrial zones) with live
 * OpenStreetMap Nominatim geocoding search so typing "rumah sakit slg", "rs slg", etc. immediately
 * surfaces accurate suggestions and pins them on the map.
 */
object OsmPlaceSearchService {

    private val curatedIndonesianPlaces = listOf(
        // RS SLG / Kediri & Sekitarnya (sesuai contoh pengguna)
        OsmPlaceSuggestion(
            title = "RSUD Simpang Lima Gumul (RS SLG) Kediri",
            addressSubtitle = "Jl. Galuh Candrakirana, Tugurejo, Kec. Ngasem, Kab. Kediri, Jawa Timur",
            latitude = -7.8194,
            longitude = 112.0637,
            categoryBadge = "Rumah Sakit"
        ),
        OsmPlaceSuggestion(
            title = "IGD & Poliklinik RS SLG (RSUD Simpang Lima Gumul)",
            addressSubtitle = "Kawasan Monumen Simpang Lima Gumul (SLG), Ngasem, Kediri",
            latitude = -7.8189,
            longitude = 112.0642,
            categoryBadge = "Layanan Medis"
        ),
        OsmPlaceSuggestion(
            title = "Monumen Simpang Lima Gumul (SLG) Kediri",
            addressSubtitle = "Tugurejo, Kec. Ngasem, Kabupaten Kediri, Jawa Timur",
            latitude = -7.8147,
            longitude = 112.0600,
            categoryBadge = "Kawasan SLG"
        ),
        OsmPlaceSuggestion(
            title = "RS Baptis Kediri",
            addressSubtitle = "Jl. Brigjen Pol. IBH Pranoto No.1, Bangsal, Kec. Pesantren, Kota Kediri",
            latitude = -7.8266,
            longitude = 112.0305,
            categoryBadge = "Rumah Sakit"
        ),
        OsmPlaceSuggestion(
            title = "RSUD Gambiran Kota Kediri",
            addressSubtitle = "Jl. Kapten Tendean No.16, Pakunden, Kec. Pesantren, Kota Kediri",
            latitude = -7.8384,
            longitude = 112.0279,
            categoryBadge = "Rumah Sakit"
        ),
        // Rumah Sakit Utama Surabaya & Jawa Timur
        OsmPlaceSuggestion(
            title = "RSUD Dr. Soetomo Surabaya",
            addressSubtitle = "Jl. Mayjen Prof. Dr. Moestopo No.6-8, Airlangga, Gubeng, Surabaya",
            latitude = -7.2682,
            longitude = 112.7582,
            categoryBadge = "Rumah Sakit"
        ),
        OsmPlaceSuggestion(
            title = "Rumah Sakit Universitas Airlangga (RSUA)",
            addressSubtitle = "Kampus C UNAIR, Jl. Dharmahusada Permai, Mulyorejo, Surabaya",
            latitude = -7.2697,
            longitude = 112.7848,
            categoryBadge = "Rumah Sakit"
        ),
        OsmPlaceSuggestion(
            title = "RS Islam Surabaya (RSI Wonokromo)",
            addressSubtitle = "Jl. Achmad Yani No.2-4, Wonokromo, Surabaya",
            latitude = -7.3056,
            longitude = 112.7354,
            categoryBadge = "Rumah Sakit"
        ),
        OsmPlaceSuggestion(
            title = "RS Premier Surabaya",
            addressSubtitle = "Jl. Nginden Intan Barat No.Blok B, Nginden Jangkungan, Sukolilo, Surabaya",
            latitude = -7.3046,
            longitude = 112.7654,
            categoryBadge = "Rumah Sakit"
        ),
        OsmPlaceSuggestion(
            title = "RSUD Sidoarjo (RSUD R.T. Notopuro)",
            addressSubtitle = "Jl. Mojopahit No.667, Celep, Kec. Sidoarjo, Kabupaten Sidoarjo",
            latitude = -7.4658,
            longitude = 112.7163,
            categoryBadge = "Rumah Sakit"
        ),
        OsmPlaceSuggestion(
            title = "RSUD Dr. Saiful Anwar Malang (RSSA)",
            addressSubtitle = "Jl. Jaksa Agung Suprapto No.2, Klojen, Kota Malang",
            latitude = -7.9723,
            longitude = 112.6315,
            categoryBadge = "Rumah Sakit"
        ),
        // Bandara, Stasiun, Terminal, Pelabuhan & Lokasi Operasional Sekolah SR
        OsmPlaceSuggestion(
            title = "Bandara Internasional Juanda (T1 & T2)",
            addressSubtitle = "Jl. Ir. H. Juanda, Betro, Kec. Sedati, Kabupaten Sidoarjo",
            latitude = -7.3798,
            longitude = 112.7869,
            categoryBadge = "Bandara"
        ),
        OsmPlaceSuggestion(
            title = "Stasiun Surabaya Gubeng",
            addressSubtitle = "Jl. Gubeng Masjid, Pacar Keling, Kec. Tambaksari, Surabaya",
            latitude = -7.2653,
            longitude = 112.7520,
            categoryBadge = "Stasiun KA"
        ),
        OsmPlaceSuggestion(
            title = "Stasiun Surabaya Pasar Turi",
            addressSubtitle = "Jl. Bubutan, Gundih, Kec. Bubutan, Surabaya",
            latitude = -7.2486,
            longitude = 112.7310,
            categoryBadge = "Stasiun KA"
        ),
        OsmPlaceSuggestion(
            title = "Terminal Purabaya (Bungurasih)",
            addressSubtitle = "Jl. Letjend Sutoyo, Bungurasih, Kec. Waru, Kabupaten Sidoarjo",
            latitude = -7.3508,
            longitude = 112.7248,
            categoryBadge = "Terminal Bus"
        ),
        OsmPlaceSuggestion(
            title = "Pelabuhan Tanjung Perak Surabaya",
            addressSubtitle = "Jl. Perak Timur, Perak Utara, Kec. Pabean Cantian, Surabaya",
            latitude = -7.2048,
            longitude = 112.7294,
            categoryBadge = "Pelabuhan"
        ),
        OsmPlaceSuggestion(
            title = "Dinas Pendidikan Provinsi Jawa Timur",
            addressSubtitle = "Jl. Gentengkali No.33, Genteng, Kec. Genteng, Surabaya",
            latitude = -7.2568,
            longitude = 112.7376,
            categoryBadge = "Instansi"
        ),
        OsmPlaceSuggestion(
            title = "Kantor Gubernur Jawa Timur (Tugu Pahlawan)",
            addressSubtitle = "Jl. Pahlawan No.110, Alun-alun Contong, Kec. Bubutan, Surabaya",
            latitude = -7.2459,
            longitude = 112.7378,
            categoryBadge = "Pemerintahan"
        ),
        OsmPlaceSuggestion(
            title = "Pasar Induk Osowilangun Surabaya",
            addressSubtitle = "Jl. Tambak Osowilangun, Kec. Benowo, Surabaya (Logistik Dapur)",
            latitude = -7.2215,
            longitude = 112.6624,
            categoryBadge = "Logistik Dapur"
        ),
        OsmPlaceSuggestion(
            title = "Pasar Besar Keputran Surabaya",
            addressSubtitle = "Jl. Keputran Raya, Tegalsari, Surabaya (Belanja Bahan Pangan Dapur)",
            latitude = -7.2756,
            longitude = 112.7438,
            categoryBadge = "Logistik Dapur"
        ),
        OsmPlaceSuggestion(
            title = "Kawasan Industri Rungkut (SIER)",
            addressSubtitle = "Jl. Rungkut Industri Raya, Tenggilis Mejoyo, Surabaya",
            latitude = -7.3185,
            longitude = 112.7712,
            categoryBadge = "Kawasan Industri"
        ),
        OsmPlaceSuggestion(
            title = "Masjid Nasional Al-Akbar Surabaya",
            addressSubtitle = "Jl. Masjid Al-Akbar Timur No.1, Pagesangan, Jambangan, Surabaya",
            latitude = -7.3364,
            longitude = 112.7151,
            categoryBadge = "Ibadah & Kegiatan"
        ),
        OsmPlaceSuggestion(
            title = "Kampus ITS Sukolilo Surabaya",
            addressSubtitle = "Jl. Raya ITS, Keputih, Kec. Sukolilo, Surabaya",
            latitude = -7.2823,
            longitude = 112.7949,
            categoryBadge = "Kampus"
        ),
        OsmPlaceSuggestion(
            title = "Kampus Universitas Negeri Surabaya (UNESA Ketintang)",
            addressSubtitle = "Jl. Ketintang, Ketintang, Kec. Gayungan, Surabaya",
            latitude = -7.3119,
            longitude = 112.7285,
            categoryBadge = "Kampus"
        )
    )

    /**
     * Returns instant local fuzzy matches so suggestions appear immediately on the first keystroke
     * (including aliases like "rumah sakit" <-> "rs", "slg" <-> "simpang lima gumul").
     */
    fun getInstantLocalSuggestions(query: String): List<OsmPlaceSuggestion> {
        val cleaned = query.trim().lowercase()
        if (cleaned.isEmpty()) {
            return curatedIndonesianPlaces.take(5)
        }

        // Normalize common Indonesian abbreviations so "rumah sakit slg" matches "RS SLG / RSUD Simpang Lima Gumul"
        val normalizedQuery = cleaned
            .replace("rumah sakit", "rs")
            .replace("rumahsakit", "rs")
            .replace("rsud", "rs")
            .trim()

        val tokens = normalizedQuery.split(Regex("\\s+")).filter { it.isNotEmpty() }

        val scored = curatedIndonesianPlaces.mapNotNull { place ->
            val searchable = buildString {
                append(place.title.lowercase())
                append(" ")
                append(place.addressSubtitle.lowercase())
                append(" ")
                append(place.categoryBadge.lowercase())
                // Add explicit synonym expansion
                if (place.title.contains("RS", ignoreCase = true) || place.categoryBadge.contains("Rumah Sakit", ignoreCase = true)) {
                    append(" rumah sakit rs rsud klinik medis ")
                }
                if (place.title.contains("SLG", ignoreCase = true) || place.title.contains("Simpang Lima Gumul", ignoreCase = true)) {
                    append(" rs slg rumah sakit slg simpang lima gumul kediri ")
                }
            }

            val matchedTokens = tokens.count { token -> searchable.contains(token) }
            if (matchedTokens == 0 && !searchable.contains(cleaned)) {
                null
            } else {
                val exactBoost = if (searchable.contains(cleaned) || searchable.contains(normalizedQuery)) 10 else 0
                val startsWithBoost = if (place.title.lowercase().startsWith(cleaned)) 5 else 0
                val score = matchedTokens * 4 + exactBoost + startsWithBoost
                Pair(score, place)
            }
        }

        return scored
            .sortedByDescending { it.first }
            .map { it.second }
            .take(6)
    }

    /**
     * Queries OpenStreetMap Nominatim API for real-world places in Indonesia and merges with
     * instant curated matches so both local landmarks (e.g. "RS SLG") and any street/place across
     * Indonesia are suggested.
     */
    suspend fun searchPlacesWithNominatim(query: String): List<OsmPlaceSuggestion> = withContext(Dispatchers.IO) {
        val localMatches = getInstantLocalSuggestions(query)
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return@withContext localMatches
        }

        val remoteMatches = mutableListOf<OsmPlaceSuggestion>()
        try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?format=jsonv2&q=$encoded&countrycodes=id&limit=6&addressdetails=1"
            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4500
                readTimeout = 4500
                setRequestProperty("User-Agent", "BawaMobilSR-FleetTracker/1.0 (Android; OpenStreetMap)")
                setRequestProperty("Accept-Language", "id,en")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(body)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val lat = obj.optString("lat").toDoubleOrNull() ?: continue
                    val lon = obj.optString("lon").toDoubleOrNull() ?: continue
                    val displayName = obj.optString("display_name", "")
                    val rawName = obj.optString("name", "")
                    val category = obj.optString("type", "Lokasi OSM").replace("_", " ")

                    val parts = displayName.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val primaryTitle = if (rawName.isNotBlank()) {
                        rawName
                    } else {
                        parts.firstOrNull() ?: trimmed
                    }
                    val subtitle = if (parts.size > 1) {
                        parts.drop(1).take(4).joinToString(", ")
                    } else {
                        displayName
                    }

                    remoteMatches.add(
                        OsmPlaceSuggestion(
                            title = primaryTitle,
                            addressSubtitle = subtitle,
                            latitude = lat,
                            longitude = lon,
                            categoryBadge = category.replaceFirstChar { it.uppercase() }
                        )
                    )
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
            // If offline or rate-limited, localMatches still provide instant suggestions
        }

        val combined = mutableListOf<OsmPlaceSuggestion>()
        combined.addAll(localMatches)
        for (remote in remoteMatches) {
            val isDuplicate = combined.any { existing ->
                existing.title.equals(remote.title, ignoreCase = true) ||
                    (kotlin.math.abs(existing.latitude - remote.latitude) < 0.002 &&
                        kotlin.math.abs(existing.longitude - remote.longitude) < 0.002)
            }
            if (!isDuplicate) {
                combined.add(remote)
            }
        }
        combined.take(8)
    }
}
