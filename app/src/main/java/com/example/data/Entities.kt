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
    USER_MONITOR,
    SECURITY
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
    val currentLat: Double = -7.2575,
    val currentLng: Double = 112.7521,
    val currentSpeedKmh: Int = 0,
    val progressPercent: Float = 0f,
    val currentTargetIndex: Int = 0,
    val isGpsRealDevice: Boolean = false,
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

    val tripStatusEnum: TripStatus
        get() = try {
            TripStatus.valueOf(status)
        } catch (_: Exception) {
            TripStatus.PENDING_APPROVAL
        }
}
