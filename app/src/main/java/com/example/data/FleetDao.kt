package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
    @Query("SELECT * FROM vehicles ORDER BY id ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehicleEntity>)

    @Query("SELECT * FROM destinations ORDER BY isCustom DESC, id ASC")
    fun getAllDestinations(): Flow<List<DestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: DestinationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestinations(destinations: List<DestinationEntity>)

    @Query("SELECT * FROM trip_requests ORDER BY createdAt DESC")
    fun getAllTripRequests(): Flow<List<TripRequestEntity>>

    @Query("SELECT * FROM trip_requests WHERE status = 'IN_TRANSIT' ORDER BY updatedAt DESC")
    fun getActiveTripsSnapshot(): List<TripRequestEntity>

    @Query("SELECT * FROM trip_requests WHERE id = :tripId LIMIT 1")
    suspend fun getTripById(tripId: Int): TripRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripRequest(trip: TripRequestEntity): Long

    @Update
    suspend fun updateTripRequest(trip: TripRequestEntity)

    @Query("DELETE FROM trip_requests WHERE id = :tripId")
    suspend fun deleteTripRequest(tripId: Int)
}
