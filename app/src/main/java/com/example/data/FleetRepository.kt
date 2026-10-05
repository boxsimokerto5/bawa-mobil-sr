package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FleetRepository(private val dao: FleetDao) {

    val vehicles: Flow<List<VehicleEntity>> = dao.getAllVehicles()
    val destinations: Flow<List<DestinationEntity>> = dao.getAllDestinations()
    val tripRequests: Flow<List<TripRequestEntity>> = dao.getAllTripRequests()

    companion object {
        // Pos Utama Keamanan SR (Base Camp Coordinates)
        const val BASE_LAT = -7.2575
        const val BASE_LNG = 112.7521
        const val BASE_NAME = "Pos Utama Keamanan SR (Garasi Pusat)"
    }

    suspend fun ensureSeedData() {
        if (dao.getVehicleCount() > 0) return

        val initialVehicles = listOf(
            VehicleEntity(
                id = "VH_GRANMAX",
                name = "Gran Max",
                plateNumber = "L 8841 SR",
                category = "Mobil Operasional & Logistik",
                capacityInfo = "Pick-Up / Box • 3 Penumpang + Kargo",
                fuelLevelPercent = 92
            ),
            VehicleEntity(
                id = "VH_NMAX",
                name = "N-Max",
                plateNumber = "L 4029 SR",
                category = "Motor Operasional Cepat",
                capacityInfo = "Yamaha N-Max 155 • 2 Penumpang",
                fuelLevelPercent = 88
            ),
            VehicleEntity(
                id = "VH_VELOZ",
                name = "Avanza Veloz",
                plateNumber = "L 1925 SR",
                category = "Mobil Dinas & Tamu Eksekutif",
                capacityInfo = "MPV 7 Penumpang • Kabin Nyaman",
                fuelLevelPercent = 95
            )
        )
        dao.insertVehicles(initialVehicles)

        val defaultDestinations = listOf(
            DestinationEntity(
                name = "Gudang Logistik Utama SR",
                addressCategory = "Kawasan Industri Rungkut Blok B-12",
                latitude = -7.3185,
                longitude = 112.7712,
                isCustom = false
            ),
            DestinationEntity(
                name = "Kantor Cabang Pusat",
                addressCategory = "Jl. Basuki Rahmat No. 88",
                latitude = -7.2654,
                longitude = 112.7418,
                isCustom = false
            ),
            DestinationEntity(
                name = "Workshop & Bengkel Resmi SR",
                addressCategory = "Jl. Ahmad Yani No. 142",
                latitude = -7.3091,
                longitude = 112.7345,
                isCustom = false
            ),
            DestinationEntity(
                name = "Pelabuhan & Cargo Tanjung Perak",
                addressCategory = "Terminal Jamrud Utara",
                latitude = -7.2048,
                longitude = 112.7294,
                isCustom = false
            ),
            DestinationEntity(
                name = "Bandara Internasional Juanda T2",
                addressCategory = "Area Penjemputan Dinas VIP",
                latitude = -7.3798,
                longitude = 112.7869,
                isCustom = false
            ),
            DestinationEntity(
                name = "Kantor Klien / Mitra Distribusi",
                addressCategory = "Kawasan Pergudangan Margomulyo",
                latitude = -7.2432,
                longitude = 112.6815,
                isCustom = false
            ),
            DestinationEntity(
                name = "SPBU & Pusat Pengisian BBM",
                addressCategory = "Jl. Raya Gubeng No. 45",
                latitude = -7.2720,
                longitude = 112.7505,
                isCustom = false
            )
        )
        dao.insertDestinations(defaultDestinations)

        val now = System.currentTimeMillis()
        // Seed 1 active trip (Avanza Veloz in transit), 1 pending trip (Gran Max waiting security approval), 1 completed trip
        val sampleTrips = listOf(
            TripRequestEntity(
                vehicleId = "VH_VELOZ",
                vehicleName = "Avanza Veloz",
                vehiclePlate = "L 1925 SR",
                driverName = "Bapak Hendra Wijaya",
                driverDivision = "Divisi Operasional & Humas",
                purpose = "Penjemputan Tamu Direksi & Kunjungan Kantor Cabang",
                destinationsText = "Kantor Cabang Pusat|Bandara Internasional Juanda T2",
                destinationCoordsText = "-7.2654,112.7418;-7.3798,112.7869",
                departureEstimate = "08:30 WIB",
                returnEstimate = "14:00 WIB",
                startOdometerKm = 42810,
                status = TripStatus.IN_TRANSIT.name,
                securityNotes = "Dokumen surat jalan & STNK lengkap. Disetujui keluar gerbang utama.",
                approvedByOfficer = "Komandan Pos Suryo",
                currentLat = -7.2618,
                currentLng = 112.7465,
                currentSpeedKmh = 46,
                progressPercent = 0.28f,
                currentTargetIndex = 0,
                isGpsRealDevice = false,
                createdAt = now - 3600_000L,
                updatedAt = now - 60_000L
            ),
            TripRequestEntity(
                vehicleId = "VH_GRANMAX",
                vehicleName = "Gran Max",
                vehiclePlate = "L 8841 SR",
                driverName = "Mas Bagus Prasetyo",
                driverDivision = "Divisi Gudang & Pengiriman",
                purpose = "Pengambilan Suku Cadang & Distribusi Barang ke Gudang Utama",
                destinationsText = "Gudang Logistik Utama SR|Workshop & Bengkel Resmi SR",
                destinationCoordsText = "-7.3185,112.7712;-7.3091,112.7345",
                departureEstimate = "09:45 WIB",
                returnEstimate = "15:30 WIB",
                startOdometerKm = 68190,
                status = TripStatus.PENDING_APPROVAL.name,
                securityNotes = "",
                approvedByOfficer = "",
                currentLat = BASE_LAT,
                currentLng = BASE_LNG,
                currentSpeedKmh = 0,
                progressPercent = 0f,
                currentTargetIndex = 0,
                isGpsRealDevice = false,
                createdAt = now - 900_000L,
                updatedAt = now - 900_000L
            ),
            TripRequestEntity(
                vehicleId = "VH_NMAX",
                vehicleName = "N-Max",
                vehiclePlate = "L 4029 SR",
                driverName = "Rizky Pratama",
                driverDivision = "Kurir Dokumen & Administrasi",
                purpose = "Antar Dokumen Invoice Resmi ke Kantor Cabang",
                destinationsText = "Kantor Cabang Pusat",
                destinationCoordsText = "-7.2654,112.7418",
                departureEstimate = "07:15 WIB",
                returnEstimate = "08:20 WIB",
                startOdometerKm = 15420,
                status = TripStatus.COMPLETED.name,
                securityNotes = "Unit N-Max telah kembali ke garasi dengan aman.",
                approvedByOfficer = "Komandan Pos Suryo",
                currentLat = BASE_LAT,
                currentLng = BASE_LNG,
                currentSpeedKmh = 0,
                progressPercent = 1.0f,
                currentTargetIndex = 0,
                isGpsRealDevice = false,
                createdAt = now - 7200_000L,
                updatedAt = now - 4200_000L
            )
        )
        sampleTrips.forEach { dao.insertTripRequest(it) }
    }

    suspend fun addCustomDestination(
        name: String,
        addressCategory: String,
        latitude: Double,
        longitude: Double
    ): DestinationEntity {
        val entity = DestinationEntity(
            name = name.trim(),
            addressCategory = addressCategory.trim().ifEmpty { "Tujuan Tambahan Pengguna" },
            latitude = latitude,
            longitude = longitude,
            isCustom = true
        )
        val id = dao.insertDestination(entity).toInt()
        return entity.copy(id = id)
    }

    suspend fun submitTripPlan(
        vehicle: VehicleEntity,
        driverName: String,
        driverDivision: String,
        purpose: String,
        selectedDestinations: List<DestinationEntity>,
        departureEstimate: String,
        returnEstimate: String,
        startOdometerKm: Int
    ): Long {
        val destNames = selectedDestinations.joinToString("|") { it.name }
        val destCoords = selectedDestinations.joinToString(";") { "${it.latitude},${it.longitude}" }
        val now = System.currentTimeMillis()
        val trip = TripRequestEntity(
            vehicleId = vehicle.id,
            vehicleName = vehicle.name,
            vehiclePlate = vehicle.plateNumber,
            driverName = driverName.trim(),
            driverDivision = driverDivision.trim(),
            purpose = purpose.trim(),
            destinationsText = destNames,
            destinationCoordsText = destCoords,
            departureEstimate = departureEstimate.trim(),
            returnEstimate = returnEstimate.trim(),
            startOdometerKm = startOdometerKm,
            status = TripStatus.PENDING_APPROVAL.name,
            currentLat = BASE_LAT,
            currentLng = BASE_LNG,
            currentSpeedKmh = 0,
            progressPercent = 0f,
            currentTargetIndex = 0,
            createdAt = now,
            updatedAt = now
        )
        return dao.insertTripRequest(trip)
    }

    suspend fun approveTrip(tripId: Int, officerName: String, notes: String) {
        val trip = dao.getTripById(tripId) ?: return
        val firstCoord = trip.parsedCoordinates.firstOrNull()
        // Move vehicle slightly out of gate upon approval so it starts moving on the map
        val initialLat = if (firstCoord != null) {
            BASE_LAT + (firstCoord.first - BASE_LAT) * 0.08
        } else BASE_LAT
        val initialLng = if (firstCoord != null) {
            BASE_LNG + (firstCoord.second - BASE_LNG) * 0.08
        } else BASE_LNG

        dao.updateTripRequest(
            trip.copy(
                status = TripStatus.IN_TRANSIT.name,
                approvedByOfficer = officerName.ifBlank { "Petugas Pos Keamanan" },
                securityNotes = notes.ifBlank { "Izin keluar disetujui. Selamat jalan & utamakan keselamatan." },
                currentLat = initialLat,
                currentLng = initialLng,
                currentSpeedKmh = if (trip.vehicleId == "VH_NMAX") 38 else 45,
                progressPercent = 0.08f,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun rejectTrip(tripId: Int, officerName: String, reason: String) {
        val trip = dao.getTripById(tripId) ?: return
        dao.updateTripRequest(
            trip.copy(
                status = TripStatus.REJECTED.name,
                approvedByOfficer = officerName.ifBlank { "Petugas Pos Keamanan" },
                securityNotes = reason.ifBlank { "Permohonan ditolak oleh keamanan karena jadwal padat / dokumen belum lengkap." },
                currentSpeedKmh = 0,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun completeTrip(tripId: Int, completionNote: String = "") {
        val trip = dao.getTripById(tripId) ?: return
        val updatedNotes = if (completionNote.isNotBlank()) {
            "${trip.securityNotes} • $completionNote".trim(' ', '•')
        } else {
            trip.securityNotes
        }
        dao.updateTripRequest(
            trip.copy(
                status = TripStatus.COMPLETED.name,
                currentLat = BASE_LAT,
                currentLng = BASE_LNG,
                currentSpeedKmh = 0,
                progressPercent = 1.0f,
                securityNotes = updatedNotes,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateLiveDeviceGpsForDriver(
        driverName: String,
        lat: Double,
        lng: Double,
        speedKmh: Int
    ) {
        val activeTrips = dao.getActiveTripsSnapshot()
        val matchingTrip = activeTrips.firstOrNull {
            it.driverName.equals(driverName, ignoreCase = true)
        } ?: activeTrips.firstOrNull() ?: return

        val coords = matchingTrip.parsedCoordinates
        val target = coords.getOrNull(matchingTrip.currentTargetIndex) ?: coords.lastOrNull()
        val computedProgress = if (target != null) {
            val totalDist = haversineKm(BASE_LAT, BASE_LNG, target.first, target.second).coerceAtLeast(0.5)
            val remDist = haversineKm(lat, lng, target.first, target.second)
            (1.0 - (remDist / totalDist)).toFloat().coerceIn(0.05f, 0.99f)
        } else matchingTrip.progressPercent

        dao.updateTripRequest(
            matchingTrip.copy(
                currentLat = lat,
                currentLng = lng,
                currentSpeedKmh = speedKmh.coerceAtLeast(12),
                progressPercent = computedProgress,
                isGpsRealDevice = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun advanceActiveTripsTelemetry(stepMultiplier: Float = 1f) {
        val activeTrips = dao.getActiveTripsSnapshot()
        val now = System.currentTimeMillis()
        for (trip in activeTrips) {
            val coords = trip.parsedCoordinates
            if (coords.isEmpty()) continue

            // Build full waypoint list: Base -> Dest 1 -> Dest 2 -> ... -> Base
            val waypoints = buildList {
                add(Pair(BASE_LAT, BASE_LNG))
                addAll(coords)
            }

            val totalSegments = waypoints.size - 1
            if (totalSegments <= 0) continue

            val increment = 0.025f * stepMultiplier
            var newProgress = trip.progressPercent + increment
            if (newProgress >= 0.98f) {
                // Loop gently around destination or return path so user can always observe live motion until marked completed
                newProgress = 0.12f
            }

            val scaledProgress = newProgress * totalSegments
            val segIndex = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
            val localT = (scaledProgress - segIndex).coerceIn(0f, 1f)

            val startPt = waypoints[segIndex]
            val endPt = waypoints[segIndex + 1]

            val interpolatedLat = startPt.first + (endPt.first - startPt.first) * localT
            val interpolatedLng = startPt.second + (endPt.second - startPt.second) * localT

            val baseSpeed = when (trip.vehicleId) {
                "VH_NMAX" -> 42
                "VH_GRANMAX" -> 38
                else -> 52
            }
            val speedVariation = ((now / 1000L + trip.id * 7) % 11).toInt() - 5
            val newSpeed = (baseSpeed + speedVariation).coerceAtLeast(20)

            dao.updateTripRequest(
                trip.copy(
                    currentLat = interpolatedLat,
                    currentLng = interpolatedLng,
                    currentSpeedKmh = newSpeed,
                    progressPercent = newProgress,
                    currentTargetIndex = segIndex.coerceIn(0, coords.size - 1),
                    updatedAt = now
                )
            )
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
