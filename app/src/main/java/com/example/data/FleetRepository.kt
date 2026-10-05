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
    val userAccounts: Flow<List<UserAccountEntity>> = dao.getAllUserAccounts()

    companion object {
        // Lokasi Sekolah Rakyat / Pos Utama Keamanan SR (Titik Asal Seluruh Kendaraan)
        const val BASE_LAT = -7.872575
        const val BASE_LNG = 112.169353
        const val BASE_NAME = "Sekolah Rakyat (Pos Utama SR • -7.872575, 112.169353)"
    }

    suspend fun ensureSeedData() {
        if (dao.getVehicleCount() > 0) return

        val initialVehicles = listOf(
            VehicleEntity(
                id = "VH_GRANMAX",
                name = "Gran Max",
                plateNumber = "AG 8841 SR",
                category = "Mobil Operasional & Logistik SR",
                capacityInfo = "Pick-Up / Box • 3 Penumpang + Kargo",
                fuelLevelPercent = 92
            ),
            VehicleEntity(
                id = "VH_NMAX",
                name = "N-Max",
                plateNumber = "AG 4029 SR",
                category = "Motor Operasional Cepat SR",
                capacityInfo = "Yamaha N-Max 155 • 2 Penumpang",
                fuelLevelPercent = 88
            ),
            VehicleEntity(
                id = "VH_VELOZ",
                name = "Avanza Veloz",
                plateNumber = "AG 1925 SR",
                category = "Mobil Dinas & Tamu Sekolah Rakyat",
                capacityInfo = "MPV 7 Penumpang • Kabin Nyaman",
                fuelLevelPercent = 95
            )
        )
        dao.insertVehicles(initialVehicles)

        val defaultDestinations = listOf(
            DestinationEntity(
                name = "Puskesmas Wates Kediri",
                addressCategory = "Jl. Raya Kediri - Blitar, Kec. Wates, Kab. Kediri",
                latitude = -7.9165,
                longitude = 112.1118,
                isCustom = false
            ),
            DestinationEntity(
                name = "RSUD Simpang Lima Gumul (RS SLG)",
                addressCategory = "Jl. Galuh Candrakirana, Ngasem, Kab. Kediri",
                latitude = -7.8194,
                longitude = 112.0637,
                isCustom = false
            ),
            DestinationEntity(
                name = "Pasar & Pusat Logistik Wates",
                addressCategory = "Kec. Wates, Kab. Kediri (Belanja Dapur & Logistik SR)",
                latitude = -7.9182,
                longitude = 112.1132,
                isCustom = false
            ),
            DestinationEntity(
                name = "RSUD Kabupaten Kediri (RSKK Pare)",
                addressCategory = "Jl. Pahlawan Kusuma Bangsa No. 1, Pare, Kab. Kediri",
                latitude = -7.7621,
                longitude = 112.1874,
                isCustom = false
            ),
            DestinationEntity(
                name = "Dinas Sosial & Pendidikan Kab. Kediri",
                addressCategory = "Kawasan Pemerintahan Kab. Kediri, Ngasem",
                latitude = -7.8112,
                longitude = 112.0568,
                isCustom = false
            ),
            DestinationEntity(
                name = "Stasiun Kediri",
                addressCategory = "Jl. Stasiun, Balowerti, Kec. Kota, Kota Kediri",
                latitude = -7.8174,
                longitude = 112.0154,
                isCustom = false
            ),
            DestinationEntity(
                name = "SPBU & Bengkel Operasional Terdekat",
                addressCategory = "Jalur Utama Plosoklaten - Wates, Kab. Kediri",
                latitude = -7.8840,
                longitude = 112.1485,
                isCustom = false
            )
        )
        dao.insertDestinations(defaultDestinations)
    }

    private fun addressSubtitleOrCategory(text: String): String = text

    suspend fun getAccountByEmail(email: String): UserAccountEntity? {
        return dao.getUserAccountByEmail(email.trim())
    }

    suspend fun getAccountById(accountId: Int): UserAccountEntity? {
        return dao.getUserAccountById(accountId)
    }

    suspend fun registerNewAccount(
        email: String,
        password: String,
        initialCategory: String
    ): Result<UserAccountEntity> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Format email tidak valid."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Kata sandi minimal 4 karakter."))
        }
        val existing = dao.getUserAccountByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(
                IllegalStateException("Email '$cleanEmail' sudah terdaftar. Silakan langsung login.")
            )
        }

        val generatedCode = ((100000..999999).random()).toString()
        val now = System.currentTimeMillis()
        val newAccount = UserAccountEntity(
            email = cleanEmail,
            password = password,
            initialAccountCategory = initialCategory,
            verificationCode = generatedCode,
            isEmailVerified = false,
            isProfileSubmitted = false,
            accountStatus = AccountRegistrationStatus.PENDING_EMAIL_VERIFICATION.name,
            createdAt = now,
            updatedAt = now
        )
        val id = dao.insertUserAccount(newAccount).toInt()
        return Result.success(newAccount.copy(id = id))
    }

    suspend fun verifyAccountEmail(accountId: Int): UserAccountEntity? {
        val account = dao.getUserAccountById(accountId) ?: return null
        val nextStatus = if (account.isProfileSubmitted) {
            account.accountStatus
        } else {
            AccountRegistrationStatus.PENDING_PROFILE_COMPLETION.name
        }
        val updated = account.copy(
            isEmailVerified = true,
            accountStatus = nextStatus,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateUserAccount(updated)
        return updated
    }

    suspend fun regenerateVerificationCode(accountId: Int): UserAccountEntity? {
        val account = dao.getUserAccountById(accountId) ?: return null
        val newCode = ((100000..999999).random()).toString()
        val updated = account.copy(
            verificationCode = newCode,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateUserAccount(updated)
        return updated
    }

    suspend fun submitUserProfileForm(
        accountId: Int,
        fullName: String,
        address: String,
        phoneNumber: String,
        taskRole: String,
        selfPhotoUri: String,
        ktpPhotoUri: String
    ): UserAccountEntity? {
        val account = dao.getUserAccountById(accountId) ?: return null
        val updated = account.copy(
            fullName = fullName.trim(),
            address = address.trim(),
            phoneNumber = phoneNumber.trim(),
            taskRole = taskRole.trim(),
            selfPhotoUri = selfPhotoUri.trim(),
            ktpPhotoUri = ktpPhotoUri.trim(),
            isProfileSubmitted = true,
            accountStatus = AccountRegistrationStatus.PENDING_ADMIN_APPROVAL.name,
            adminNotes = "",
            updatedAt = System.currentTimeMillis()
        )
        dao.updateUserAccount(updated)
        return updated
    }

    suspend fun approveUserAccountByAdmin(
        accountId: Int,
        adminUsername: String,
        notes: String
    ): UserAccountEntity? {
        val account = dao.getUserAccountById(accountId) ?: return null
        val updated = account.copy(
            accountStatus = AccountRegistrationStatus.APPROVED.name,
            approvedByAdmin = adminUsername.ifBlank { "Admin Sekolah (eccko1101)" },
            adminNotes = notes.ifBlank { "Berkas Foto Diri, KTP, dan Tugas telah diverifikasi oleh Admin Sekolah." },
            updatedAt = System.currentTimeMillis()
        )
        dao.updateUserAccount(updated)
        return updated
    }

    suspend fun rejectUserAccountByAdmin(
        accountId: Int,
        adminUsername: String,
        reason: String
    ): UserAccountEntity? {
        val account = dao.getUserAccountById(accountId) ?: return null
        val updated = account.copy(
            accountStatus = AccountRegistrationStatus.REJECTED.name,
            approvedByAdmin = adminUsername.ifBlank { "Admin Sekolah (eccko1101)" },
            adminNotes = reason.ifBlank { "Berkas KTP atau biodata belum sesuai, silakan perbarui formulir." },
            updatedAt = System.currentTimeMillis()
        )
        dao.updateUserAccount(updated)
        return updated
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
        startOdometerKm: Int,
        initialLat: Double? = null,
        initialLng: Double? = null
    ): Long {
        val destNames = selectedDestinations.joinToString("|") { it.name }
        val destCoords = selectedDestinations.joinToString(";") { "${it.latitude},${it.longitude}" }
        val now = System.currentTimeMillis()
        val hasRealGps = initialLat != null && initialLng != null
        val startLat = initialLat ?: BASE_LAT
        val startLng = initialLng ?: BASE_LNG
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
            currentLat = startLat,
            currentLng = startLng,
            currentSpeedKmh = 0,
            progressPercent = 0f,
            currentTargetIndex = 0,
            isGpsRealDevice = hasRealGps,
            createdAt = now,
            updatedAt = now
        )
        return dao.insertTripRequest(trip)
    }

    suspend fun approveTrip(
        tripId: Int,
        officerName: String,
        notes: String,
        deviceLat: Double? = null,
        deviceLng: Double? = null
    ) {
        val trip = dao.getTripById(tripId) ?: return
        val firstCoord = trip.parsedCoordinates.firstOrNull()
        val now = System.currentTimeMillis()
        val useRealGps = (deviceLat != null && deviceLng != null) || trip.isGpsRealDevice
        val startBaseLat = deviceLat ?: if (trip.isGpsRealDevice) trip.currentLat else BASE_LAT
        val startBaseLng = deviceLng ?: if (trip.isGpsRealDevice) trip.currentLng else BASE_LNG

        // If real GPS is active, keep exact real GPS coordinate; otherwise move slightly toward destination
        val initialLat = if (useRealGps) {
            startBaseLat
        } else if (firstCoord != null) {
            startBaseLat + (firstCoord.first - startBaseLat) * 0.06
        } else startBaseLat

        val initialLng = if (useRealGps) {
            startBaseLng
        } else if (firstCoord != null) {
            startBaseLng + (firstCoord.second - startBaseLng) * 0.06
        } else startBaseLng

        val initialDistKm = haversineKm(startBaseLat, startBaseLng, initialLat, initialLng)
        val initialTrail = if (useRealGps) {
            formatCoordPair(initialLat, initialLng)
        } else {
            buildString {
                append(formatCoordPair(startBaseLat, startBaseLng))
                append(";")
                append(formatCoordPair(initialLat, initialLng))
            }
        }

        dao.updateTripRequest(
            trip.copy(
                status = TripStatus.IN_TRANSIT.name,
                approvedByOfficer = officerName.ifBlank { "Petugas Pos Keamanan" },
                securityNotes = notes.ifBlank { "Izin keluar disetujui Keamanan. Jejak rute & jarak tempuh direkam otomatis." },
                currentLat = initialLat,
                currentLng = initialLng,
                currentSpeedKmh = if (useRealGps) 0 else if (trip.vehicleId == "VH_NMAX") 38 else 45,
                progressPercent = 0.04f,
                isGpsRealDevice = useRealGps,
                routeTrailCoordsText = initialTrail,
                totalDistanceTraveledKm = roundKm(initialDistKm),
                endOdometerKm = trip.startOdometerKm + kotlin.math.ceil(initialDistKm).toInt(),
                isArrivedBackAtSrGate = false,
                approvedAt = now,
                updatedAt = now
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

    suspend fun completeTrip(
        tripId: Int,
        officerName: String = "Petugas Pos Keamanan",
        completionNote: String = ""
    ) {
        val trip = dao.getTripById(tripId) ?: return
        val now = System.currentTimeMillis()
        // Ensure the trail connects all visited waypoints and closes cleanly back at Pos Utama SR
        val existingTrail = trip.parsedTrailCoordinates.toMutableList()
        if (existingTrail.isEmpty()) {
            existingTrail.add(Pair(BASE_LAT, BASE_LNG))
        }
        // If completed early before passing all destinations, include planned waypoints so full route trail is preserved
        if (trip.progressPercent < 0.85f) {
            for (destCoord in trip.parsedCoordinates) {
                val alreadyNear = existingTrail.any { haversineKm(it.first, it.second, destCoord.first, destCoord.second) < 0.25 }
                if (!alreadyNear) {
                    existingTrail.add(destCoord)
                }
            }
        }
        val lastPt = existingTrail.last()
        val distToGate = haversineKm(lastPt.first, lastPt.second, BASE_LAT, BASE_LNG)
        if (distToGate > 0.02) {
            existingTrail.add(Pair(BASE_LAT, BASE_LNG))
        }

        val fullTrailKm = calculatePolylineDistanceKm(existingTrail)
            .coerceAtLeast(trip.totalDistanceTraveledKm + distToGate)
        val finalDistanceKm = roundKm(fullTrailKm.coerceAtLeast(0.5))
        val finalOdometer = trip.startOdometerKm + kotlin.math.ceil(finalDistanceKm).toInt()

        val updatedNotes = if (completionNote.isNotBlank()) {
            "${trip.securityNotes} • $completionNote".trim(' ', '•')
        } else {
            "${trip.securityNotes} • Diakhiri oleh Keamanan ($officerName) setelah kembali di Pos Utama SR.".trim(' ', '•')
        }

        dao.updateTripRequest(
            trip.copy(
                status = TripStatus.COMPLETED.name,
                completedByOfficer = officerName.ifBlank { "Petugas Pos Keamanan" },
                currentLat = BASE_LAT,
                currentLng = BASE_LNG,
                currentSpeedKmh = 0,
                progressPercent = 1.0f,
                routeTrailCoordsText = existingTrail.joinToString(";") { formatCoordPair(it.first, it.second) },
                totalDistanceTraveledKm = finalDistanceKm,
                endOdometerKm = finalOdometer,
                isArrivedBackAtSrGate = true,
                completedAt = now,
                securityNotes = updatedNotes,
                updatedAt = now
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
        if (activeTrips.isEmpty()) return

        // Update matching driver trip or all active trips when monitoring from the device
        val targets = activeTrips.filter {
            driverName.isNotBlank() && it.driverName.equals(driverName, ignoreCase = true)
        }.ifEmpty { activeTrips }

        for (matchingTrip in targets) {
            val existingTrail = matchingTrip.parsedTrailCoordinates.toMutableList()
            // If previous trail was only simulated around Surabaya BASE_LAT while real GPS is elsewhere, reset trail to real GPS
            if (existingTrail.isNotEmpty() && !matchingTrip.isGpsRealDevice) {
                val distFromFirst = haversineKm(existingTrail.first().first, existingTrail.first().second, lat, lng)
                if (distFromFirst > 5.0) {
                    existingTrail.clear()
                }
            }
            if (existingTrail.isEmpty()) {
                existingTrail.add(Pair(lat, lng))
            }
            val lastPt = existingTrail.last()
            val stepKm = haversineKm(lastPt.first, lastPt.second, lat, lng)
            if (stepKm >= 0.008) {
                existingTrail.add(Pair(lat, lng))
            }
            val trimmedTrail = if (existingTrail.size > 160) {
                listOf(existingTrail.first()) + existingTrail.takeLast(159)
            } else {
                existingTrail
            }

            val newTotalKm = roundKm(calculatePolylineDistanceKm(trimmedTrail))
            val newEndOdo = matchingTrip.startOdometerKm + kotlin.math.ceil(newTotalKm).toInt()

            val coords = matchingTrip.parsedCoordinates
            val originPt = trimmedTrail.firstOrNull() ?: Pair(lat, lng)
            val target = coords.getOrNull(matchingTrip.currentTargetIndex) ?: coords.lastOrNull()
            val computedProgress = if (target != null) {
                val totalDist = haversineKm(originPt.first, originPt.second, target.first, target.second).coerceAtLeast(0.3)
                val remDist = haversineKm(lat, lng, target.first, target.second)
                (1.0 - (remDist / totalDist)).toFloat().coerceIn(0.05f, 0.99f)
            } else matchingTrip.progressPercent

            dao.updateTripRequest(
                matchingTrip.copy(
                    currentLat = lat,
                    currentLng = lng,
                    currentSpeedKmh = speedKmh.coerceAtLeast(0),
                    progressPercent = computedProgress,
                    isGpsRealDevice = true,
                    routeTrailCoordsText = trimmedTrail.joinToString(";") { formatCoordPair(it.first, it.second) },
                    totalDistanceTraveledKm = newTotalKm,
                    endOdometerKm = newEndOdo,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun advanceActiveTripsTelemetry(stepMultiplier: Float = 1f, forceEvenIfRealGps: Boolean = false) {
        val activeTrips = dao.getActiveTripsSnapshot()
        val now = System.currentTimeMillis()
        for (trip in activeTrips) {
            // Do not overwrite real-time GPS hardware position unless the user explicitly presses manual step button
            if (trip.isGpsRealDevice && !forceEvenIfRealGps) {
                continue
            }
            val coords = trip.parsedCoordinates
            if (coords.isEmpty()) continue

            // Full round-trip waypoint list: Pos Utama SR -> Dest 1 -> Dest 2 -> ... -> Kembali ke Pos Utama SR
            val waypoints = buildList {
                add(Pair(BASE_LAT, BASE_LNG))
                addAll(coords)
                add(Pair(BASE_LAT, BASE_LNG))
            }

            val totalSegments = waypoints.size - 1
            if (totalSegments <= 0) continue

            // If vehicle has already arrived back at SR gate (progress >= 1.0f), it waits at Pos SR for Security to end the trip
            if (trip.progressPercent >= 1.0f || trip.isArrivedBackAtSrGate) {
                if (trip.currentSpeedKmh != 0 || !trip.isArrivedBackAtSrGate) {
                    dao.updateTripRequest(
                        trip.copy(
                            currentLat = BASE_LAT,
                            currentLng = BASE_LNG,
                            currentSpeedKmh = 0,
                            progressPercent = 1.0f,
                            isArrivedBackAtSrGate = true,
                            updatedAt = now
                        )
                    )
                }
                continue
            }

            val increment = 0.022f * stepMultiplier
            val newProgress = (trip.progressPercent + increment).coerceAtMost(1.0f)
            val hasArrivedBackAtSr = newProgress >= 1.0f

            val scaledProgress = newProgress * totalSegments
            val segIndex = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
            val localT = (scaledProgress - segIndex).coerceIn(0f, 1f)

            val startPt = waypoints[segIndex]
            val endPt = waypoints[segIndex + 1]

            // Add realistic road-curve deflection along each segment so the trail follows an authentic street path
            val curveFactor = sin(localT * Math.PI).toFloat() * 0.0014f * (if (segIndex % 2 == 0) 1f else -1f)
            val interpolatedLat = if (hasArrivedBackAtSr) {
                BASE_LAT
            } else {
                startPt.first + (endPt.first - startPt.first) * localT + curveFactor
            }
            val interpolatedLng = if (hasArrivedBackAtSr) {
                BASE_LNG
            } else {
                startPt.second + (endPt.second - startPt.second) * localT - curveFactor * 0.8f
            }

            // Append new coordinate to breadcrumb trail and accumulate real distance in KM
            val existingTrail = trip.parsedTrailCoordinates.toMutableList()
            if (existingTrail.isEmpty()) {
                existingTrail.add(Pair(BASE_LAT, BASE_LNG))
            }
            val lastPt = existingTrail.last()
            val stepDistanceKm = haversineKm(lastPt.first, lastPt.second, interpolatedLat, interpolatedLng)
            if (stepDistanceKm >= 0.01 || hasArrivedBackAtSr) {
                existingTrail.add(Pair(interpolatedLat, interpolatedLng))
            }
            val cappedTrail = if (existingTrail.size > 160) {
                listOf(existingTrail.first()) + existingTrail.takeLast(159)
            } else {
                existingTrail
            }

            val newTotalDistanceKm = roundKm(trip.totalDistanceTraveledKm + stepDistanceKm)
            val newEndOdometer = trip.startOdometerKm + kotlin.math.ceil(newTotalDistanceKm).toInt()

            val baseSpeed = when (trip.vehicleId) {
                "VH_NMAX" -> 42
                "VH_GRANMAX" -> 38
                else -> 52
            }
            val speedVariation = ((now / 1000L + trip.id * 7) % 11).toInt() - 5
            val newSpeed = if (hasArrivedBackAtSr) 0 else (baseSpeed + speedVariation).coerceAtLeast(20)

            // Determine current target index (if on final segment, it's returning to Pos Utama SR)
            val targetIdx = segIndex.coerceIn(0, coords.size)

            dao.updateTripRequest(
                trip.copy(
                    currentLat = interpolatedLat,
                    currentLng = interpolatedLng,
                    currentSpeedKmh = newSpeed,
                    progressPercent = newProgress,
                    currentTargetIndex = targetIdx,
                    routeTrailCoordsText = cappedTrail.joinToString(";") { formatCoordPair(it.first, it.second) },
                    totalDistanceTraveledKm = newTotalDistanceKm,
                    endOdometerKm = newEndOdometer,
                    isArrivedBackAtSrGate = hasArrivedBackAtSr,
                    updatedAt = now
                )
            )
        }
    }

    private fun calculatePolylineDistanceKm(points: List<Pair<Double, Double>>): Double {
        if (points.size < 2) return 0.0
        var sum = 0.0
        for (i in 0 until points.size - 1) {
            sum += haversineKm(
                points[i].first,
                points[i].second,
                points[i + 1].first,
                points[i + 1].second
            )
        }
        return sum
    }

    private fun formatCoordPair(lat: Double, lng: Double): String {
        val rLat = kotlin.math.round(lat * 100000.0) / 100000.0
        val rLng = kotlin.math.round(lng * 100000.0) / 100000.0
        return "$rLat,$rLng"
    }

    private fun roundKm(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
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
