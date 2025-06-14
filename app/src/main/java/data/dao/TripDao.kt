package data.dao

import androidx.room.*
import data.entities.Trip

@Dao
interface TripDao {
    @Insert suspend fun insert(trip: Trip): Long

    @Query("SELECT * FROM trips")
    suspend fun getAll(): List<Trip>


    @Query("SELECT * FROM trips WHERE userId = :userId")
    suspend fun getByUser(userId: Long): List<Trip>
//    @Query("SELECT * FROM trips ORDER BY plannedTime ASC")
//    suspend fun getAllSortedByTime(): List<Trip>

//    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY plannedTime ASC")
//    suspend fun getByUserSortedByTime(userId: Int): List<Trip>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getById(tripId: Long): Trip? // fetch 1 trip

    // USER IS DRIVER OR PASSENGER BUT ON A TRIP
    @Query("SELECT DISTINCT t.* FROM trips t LEFT JOIN trip_passengers tp ON t.id = tp.tripId WHERE t.userId = :userId OR tp.passengerId = :userId ORDER BY t.id DESC")
    suspend fun getTripsWithUser(userId: Long): List<Trip>

    @Delete suspend fun delete(trip: Trip)
    @Update suspend fun update(trip: Trip)

    @Query("SELECT * FROM trips WHERE tripState = :state")
    suspend fun getTripsByState(state: String = "planned"): List<Trip>

}
