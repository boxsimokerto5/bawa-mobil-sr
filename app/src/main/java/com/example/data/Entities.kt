package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TripStatus {
    PENDING_APPROVAL,
    IN_TRANSIT,
    COMPLETED,
    REJECTED
}

enum class UserRole {
    NONE,
    REGISTER_ACCOUNT,
    VERIFY_EMAIL,
    COMPLETE_PROFILE_FORM,
    WAITING_ADMIN_APPROVAL,
    USER_MONITOR,
    SECURITY,
    SCHOOL_ADMIN
}

enum class AccountRegistrationStatus {
    PENDING_EMAIL_VERIFICATION,
    PENDING_PROFILE_COMPLETION,
    PENDING_ADMIN_APPROVAL,
    APPROVED,
    REJECTED
}

enum class SchoolTaskType(val label: String, val mapsToSecurityRole: Boolean) {
    KEAMANAN("Keamanan", true),
    DAPUR("Dapur", false),
    KEBERSIHAN("Kebersihan", false),
    WALIASUH("Waliasuh", false),
    WALI_ASRAMA("Wali Asrama", false),
    GURU("Guru", false),
    TU_TENDIK("TU Tendik", false);

    companion object {
        fun fromLabel(label: String): SchoolTaskType {
            return entries.firstOrNull { it.label.equals(label.trim(), ignoreCase = true) }
                ?: GURU
        }
    }
}

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val initialAccountCategory: String, // "PENGGUNA" or "KEAMANAN"
    val verificationCode: String = "482910",
    val isEmailVerified: Boolean = false,
    val fullName: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val taskRole: String = "", // Keamanan, Dapur, Kebersihan, Waliasuh, Wali Asrama, Guru, TU Tendik
    val selfPhotoUri: String = "",
    val ktpPhotoUri: String = "",
    val isProfileSubmitted: Boolean = false,
    val accountStatus: String = AccountRegistrationStatus.PENDING_EMAIL_VERIFICATION.name,
    val adminNotes: String = "",
    val approvedByAdmin: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val statusEnum: AccountRegistrationStatus
        get() = try {
            AccountRegistrationStatus.valueOf(accountStatus)
        } catch (_: Exception) {
            AccountRegistrationStatus.PENDING_EMAIL_VERIFICATION
        }

    val isSecurityTask: Boolean
        get() = taskRole.equals("Keamanan", ignoreCase = true) ||
            (taskRole.isBlank() && initialAccountCategory.equals("KEAMANAN", ignoreCase = true))
}

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val plateNumber: String,
    val category: String,
    val capacityInfo: String,
    val fuelLevelPercent: Int = 90
)

@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val addressCategory: String,
    val latitude: Double,
    val longitude: Double,
    val isCustom: Boolean = false
)

@Entity(tableName = "trip_requests")
data class TripRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: String,
    val vehicleName: String,
    val vehiclePlate: String,
    val driverName: String,
    val driverDivision: String,
    val purpose: String,
    // Pipe-separated destination names: "Gudang Utama SR|Kantor Cabang Pusat"
    val destinationsText: String,
    // Semicolon-separated lat,lng pairs: "-7.2504,112.7688;-7.2650,112.7520"
    val destinationCoordsText: String,
    val departureEstimate: String,
    val returnEstimate: String,
    val startOdometerKm: Int,
    val status: String, // TripStatus.name
    val securityNotes: String = "",
    val approvedByOfficer: String = "",
    val completedByOfficer: String = "",
    val currentLat: Double = -7.872575,
    val currentLng: Double = 112.169353,
    val currentSpeedKmh: Int = 0,
    val progressPercent: Float = 0f,
    val currentTargetIndex: Int = 0,
    val isGpsRealDevice: Boolean = false,
    // Semicolon-separated recorded trail points from SR departure -> destinations -> back to SR
    val routeTrailCoordsText: String = "-7.872575,112.169353",
    val totalDistanceTraveledKm: Double = 0.0,
    val endOdometerKm: Int = 0,
    val isArrivedBackAtSrGate: Boolean = false,
    val approvedAt: Long = 0L,
    val completedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val parsedDestinations: List<String>
        get() = destinationsText.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    val parsedCoordinates: List<Pair<Double, Double>>
        get() = destinationCoordsText.split(";").mapNotNull { token ->
            val parts = token.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) Pair(lat, lng) else null
            } else null
        }

    val parsedTrailCoordinates: List<Pair<Double, Double>>
        get() = routeTrailCoordsText.split(";").mapNotNull { token ->
            val parts = token.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) Pair(lat, lng) else null
            } else null
        }

    val computedEndOdometerKm: Int
        get() = if (endOdometerKm > 0) {
            endOdometerKm
        } else {
            startOdometerKm + kotlin.math.ceil(totalDistanceTraveledKm).toInt()
        }

    val fullRouteSummaryText: String
        get() {
            val stops = parsedDestinations
            return if (stops.isEmpty()) {
                "Sekolah Rakyat (Pos SR) ➔ Sekolah Rakyat (Pos SR)"
            } else {
                "Sekolah Rakyat (Pos SR) ➔ ${stops.joinToString(" ➔ ")} ➔ Kembali ke Sekolah Rakyat (Pos SR)"
            }
        }

    val tripStatusEnum: TripStatus
        get() = try {
            TripStatus.valueOf(status)
        } catch (_: Exception) {
            TripStatus.PENDING_APPROVAL
        }
}
