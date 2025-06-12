package data.dao

import androidx.room.*
import data.entities.Trip

@Dao
interface TripDao {
    @Insert suspend fun insert(trip: Trip): Long

    @Query("SELECT * FROM trips")
    suspend fun getAll(): List<Trip>

    @Query("SELECT * FROM trips ORDER BY plannedTime ASC")
    suspend fun getAllSortedByTime(): List<Trip>

    @Query("SELECT * FROM trips WHERE userId = :userId")
    suspend fun getByUser(userId: Long): List<Trip>

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY plannedTime ASC")
    suspend fun getByUserSortedByTime(userId: Int): List<Trip>

    @Delete suspend fun delete(trip: Trip)
    @Update suspend fun update(trip: Trip)
}
