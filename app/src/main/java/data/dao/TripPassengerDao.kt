package data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import data.entities.TripPassenger

@Dao
interface TripPassengerDao {
    @Insert suspend fun insert(tripPassenger: TripPassenger): Long

    @Delete suspend fun delete(tripPassenger: TripPassenger)

    @Query("SELECT * FROM trip_passengers WHERE tripId = :tripId")
    suspend fun getPassengersForTrip(tripId: Long): List<TripPassenger>

    @Query("SELECT * FROM trip_passengers WHERE passengerId = :passengerId")
    suspend fun getTripsForPassenger(passengerId: Long): List<TripPassenger>

    @Query("SELECT * FROM trip_passengers WHERE tripId = :tripId AND passengerId = :passengerId LIMIT 1")
    suspend fun getTripPassenger(tripId: Long, passengerId: Long): TripPassenger?

    @Query("DELETE FROM trip_passengers WHERE tripId = :tripId AND passengerId = :passengerId")
    suspend fun deleteTripPassenger(tripId: Long, passengerId: Long)
}