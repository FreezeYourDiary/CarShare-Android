package data.dao

import androidx.room.*
import data.entities.Car

@Dao
interface CarDao {

    // rooms insert return is long
    @Insert suspend fun insert(car: Car): Long

    @Query("SELECT * FROM cars")
    suspend fun getAll(): List<Car>

//    @Query("SELECT * FROM cars ORDER BY year DESC")
//    suspend fun getAllByYear(): List<Car>

    @Delete suspend fun delete(car: Car)
    @Update suspend fun update(car: Car)

    @Query("SELECT * FROM cars WHERE ownerUserId = :userId")
    suspend fun getCarsForUser(userId: Int): List<Car>

}
