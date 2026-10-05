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
        // Seed 1 active trip (Avanza Veloz in transit with trail from SR), 1 pending trip (Gran Max waiting security approval), 1 completed trip (N-Max with full round-trip trail back to SR)
        val sampleTrips = listOf(
            TripRequestEntity(
                vehicleId = "VH_VELOZ",
                vehicleName = "Avanza Veloz",
                vehiclePlate = "L 1925 SR",
                driverName = "Bapak Hendra Wijaya",
                driverDivision = "Guru",
                purpose = "Penjemputan Tamu Direksi & Kunjungan Kantor Cabang",
                destinationsText = "Kantor Cabang Pusat|Bandara Internasional Juanda T2",
                destinationCoordsText = "-7.2654,112.7418;-7.3798,112.7869",
                departureEstimate = "08:30 WIB",
                returnEstimate = "14:00 WIB",
                startOdometerKm = 42810,
                status = TripStatus.IN_TRANSIT.name,
                securityNotes = "Dokumen surat jalan & STNK lengkap. Disetujui keluar gerbang utama.",
                approvedByOfficer = "Komandan Pos Suryo",
                completedByOfficer = "",
                currentLat = -7.2654,
                currentLng = 112.7418,
                currentSpeedKmh = 46,
                progressPercent = 0.34f,
                currentTargetIndex = 0,
                isGpsRealDevice = false,
                routeTrailCoordsText = "-7.2575,112.7521;-7.2595,112.7495;-7.2618,112.7465;-7.2638,112.7440;-7.2654,112.7418",
                totalDistanceTraveledKm = 1.46,
                endOdometerKm = 42812,
                isArrivedBackAtSrGate = false,
                approvedAt = now - 2400_000L,
                completedAt = 0L,
                createdAt = now - 3600_000L,
                updatedAt = now - 60_000L
            ),
            TripRequestEntity(
                vehicleId = "VH_GRANMAX",
                vehicleName = "Gran Max",
                vehiclePlate = "L 8841 SR",
                driverName = "Mas Bagus Prasetyo",
                driverDivision = "Dapur",
                purpose = "Pengambilan Logistik Dapur & Distribusi Bahan Pangan Asrama",
                destinationsText = "Gudang Logistik Utama SR|Workshop & Bengkel Resmi SR",
                destinationCoordsText = "-7.3185,112.7712;-7.3091,112.7345",
                departureEstimate = "09:45 WIB",
                returnEstimate = "15:30 WIB",
                startOdometerKm = 68190,
                status = TripStatus.PENDING_APPROVAL.name,
                securityNotes = "",
                approvedByOfficer = "",
                completedByOfficer = "",
                currentLat = BASE_LAT,
                currentLng = BASE_LNG,
                currentSpeedKmh = 0,
                progressPercent = 0f,
                currentTargetIndex = 0,
                isGpsRealDevice = false,
                routeTrailCoordsText = "$BASE_LAT,$BASE_LNG",
                totalDistanceTraveledKm = 0.0,
                endOdometerKm = 68190,
                isArrivedBackAtSrGate = false,
                approvedAt = 0L,
                completedAt = 0L,
                createdAt = now - 900_000L,
                updatedAt = now - 900_000L
            ),
            TripRequestEntity(
                vehicleId = "VH_NMAX",
                vehicleName = "N-Max",
                vehiclePlate = "L 4029 SR",
                driverName = "Rizky Pratama",
                driverDivision = "TU Tendik",
                purpose = "Antar Dokumen Resmi Sekolah ke Kantor Cabang & Kembali ke SR",
                destinationsText = "Kantor Cabang Pusat|SPBU & Pusat Pengisian BBM",
                destinationCoordsText = "-7.2654,112.7418;-7.2720,112.7505",
                departureEstimate = "07:15 WIB",
                returnEstimate = "08:20 WIB",
                startOdometerKm = 15420,
                status = TripStatus.COMPLETED.name,
                securityNotes = "Unit N-Max telah kembali ke Pos Utama SR dengan aman & odometer dicatat oleh Keamanan.",
                approvedByOfficer = "Komandan Pos Suryo",
                completedByOfficer = "Komandan Pos Suryo",
                currentLat = BASE_LAT,
                currentLng = BASE_LNG,
                currentSpeedKmh = 0,
                progressPercent = 1.0f,
                currentTargetIndex = 1,
                isGpsRealDevice = false,
                routeTrailCoordsText = "-7.2575,112.7521;-7.2612,112.7470;-7.2654,112.7418;-7.2690,112.7462;-7.2720,112.7505;-7.2648,112.7514;-7.2575,112.7521",
                totalDistanceTraveledKm = 4.38,
                endOdometerKm = 15425,
                isArrivedBackAtSrGate = true,
                approvedAt = now - 6800_000L,
                completedAt = now - 4200_000L,
                createdAt = now - 7200_000L,
                updatedAt = now - 4200_000L
            )
        )
        sampleTrips.forEach { dao.insertTripRequest(it) }
        ensureUserAccountsSeedData()
    }

    suspend fun ensureUserAccountsSeedData() {
        if (dao.getUserAccountCount() > 0) return
        val now = System.currentTimeMillis()
        val sampleAccounts = listOf(
            UserAccountEntity(
                email = "ustadz.fauzi@sekolahsr.sch.id",
                password = "Password123",
                initialAccountCategory = "PENGGUNA",
                verificationCode = "739201",
                isEmailVerified = true,
                fullName = "Ustadz Ahmad Fauzi, S.Pd.",
                address = "Jl. Ketintang Baru III No. 24, Surabaya",
                phoneNumber = "081234567801",
                taskRole = SchoolTaskType.WALI_ASRAMA.label,
                selfPhotoUri = "preset://self_wali_asrama",
                ktpPhotoUri = "preset://ktp_verified_357801",
                isProfileSubmitted = true,
                accountStatus = AccountRegistrationStatus.PENDING_ADMIN_APPROVAL.name,
                adminNotes = "",
                approvedByAdmin = "",
                createdAt = now - 1800_000L,
                updatedAt = now - 600_000L
            ),
            UserAccountEntity(
                email = "bambang.sec@sekolahsr.sch.id",
                password = "Password123",
                initialAccountCategory = "KEAMANAN",
                verificationCode = "518402",
                isEmailVerified = true,
                fullName = "Pak Bambang Sudibyo",
                address = "Jl. Wonokromo Tengah No. 11, Surabaya",
                phoneNumber = "081355779902",
                taskRole = SchoolTaskType.KEAMANAN.label,
                selfPhotoUri = "preset://self_keamanan",
                ktpPhotoUri = "preset://ktp_verified_357802",
                isProfileSubmitted = true,
                accountStatus = AccountRegistrationStatus.PENDING_ADMIN_APPROVAL.name,
                adminNotes = "",
                approvedByAdmin = "",
                createdAt = now - 2400_000L,
                updatedAt = now - 900_000L
            ),
            UserAccountEntity(
                email = "andi.guru@sekolahsr.sch.id",
                password = "Password123",
                initialAccountCategory = "PENGGUNA",
                verificationCode = "112233",
                isEmailVerified = true,
                fullName = "Bapak Andi Pratama, M.Pd.",
                address = "Jl. Raya Darmo Permai II No. 18, Surabaya",
                phoneNumber = "081299887711",
                taskRole = SchoolTaskType.GURU.label,
                selfPhotoUri = "preset://self_guru",
                ktpPhotoUri = "preset://ktp_verified_357803",
                isProfileSubmitted = true,
                accountStatus = AccountRegistrationStatus.APPROVED.name,
                adminNotes = "Identitas KTP & Surat Tugas Guru sesuai. Disetujui menggunakan armada sekolah.",
                approvedByAdmin = "Admin Sekolah (eccko1101)",
                createdAt = now - 86_400_000L,
                updatedAt = now - 72_000_000L
            ),
            UserAccountEntity(
                email = "suryo.pos@sekolahsr.sch.id",
                password = "Password123",
                initialAccountCategory = "KEAMANAN",
                verificationCode = "445566",
                isEmailVerified = true,
                fullName = "Komandan Pos Suryo",
                address = "Asrama Kompleks Sekolah SR Blok A-1",
                phoneNumber = "081344556677",
                taskRole = SchoolTaskType.KEAMANAN.label,
                selfPhotoUri = "preset://self_komandan",
                ktpPhotoUri = "preset://ktp_verified_357804",
                isProfileSubmitted = true,
                accountStatus = AccountRegistrationStatus.APPROVED.name,
                adminNotes = "Kepala Regu Keamanan Sekolah. Akses verifikasi keluar-masuk armada aktif.",
                approvedByAdmin = "Admin Sekolah (eccko1101)",
                createdAt = now - 90_000_000L,
                updatedAt = now - 80_000_000L
            )
        )
        dao.insertUserAccounts(sampleAccounts)
    }

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
        val now = System.currentTimeMillis()
        // Move vehicle slightly out of SR gate upon security approval and initialize breadcrumb trail from SR Base
        val initialLat = if (firstCoord != null) {
            BASE_LAT + (firstCoord.first - BASE_LAT) * 0.06
        } else BASE_LAT
        val initialLng = if (firstCoord != null) {
            BASE_LNG + (firstCoord.second - BASE_LNG) * 0.06
        } else BASE_LNG

        val initialDistKm = haversineKm(BASE_LAT, BASE_LNG, initialLat, initialLng)
        val initialTrail = buildString {
            append(formatCoordPair(BASE_LAT, BASE_LNG))
            append(";")
            append(formatCoordPair(initialLat, initialLng))
        }

        dao.updateTripRequest(
            trip.copy(
                status = TripStatus.IN_TRANSIT.name,
                approvedByOfficer = officerName.ifBlank { "Petugas Pos Keamanan" },
                securityNotes = notes.ifBlank { "Izin keluar disetujui Keamanan. Jejak rute & jarak tempuh direkam otomatis." },
                currentLat = initialLat,
                currentLng = initialLng,
                currentSpeedKmh = if (trip.vehicleId == "VH_NMAX") 38 else 45,
                progressPercent = 0.04f,
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
        officerName: String = "Komandan Pos Suryo",
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
                completedByOfficer = officerName.ifBlank { "Komandan Pos Suryo" },
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
        val matchingTrip = activeTrips.firstOrNull {
            it.driverName.equals(driverName, ignoreCase = true)
        } ?: activeTrips.firstOrNull() ?: return

        val existingTrail = matchingTrip.parsedTrailCoordinates.toMutableList()
        if (existingTrail.isEmpty()) {
            existingTrail.add(Pair(BASE_LAT, BASE_LNG))
        }
        val lastPt = existingTrail.last()
        val stepKm = haversineKm(lastPt.first, lastPt.second, lat, lng)
        if (stepKm >= 0.015) {
            existingTrail.add(Pair(lat, lng))
        }
        val trimmedTrail = if (existingTrail.size > 160) {
            listOf(existingTrail.first()) + existingTrail.takeLast(159)
        } else {
            existingTrail
        }

        val newTotalKm = roundKm(matchingTrip.totalDistanceTraveledKm + stepKm)
        val newEndOdo = matchingTrip.startOdometerKm + kotlin.math.ceil(newTotalKm).toInt()

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
                routeTrailCoordsText = trimmedTrail.joinToString(";") { formatCoordPair(it.first, it.second) },
                totalDistanceTraveledKm = newTotalKm,
                endOdometerKm = newEndOdo,
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
