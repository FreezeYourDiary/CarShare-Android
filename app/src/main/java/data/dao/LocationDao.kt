package data.dao

import androidx.room.*
import data.entities.Location

@Dao
interface LocationDao {
    @Insert suspend fun insert(location: Location): Long
    @Query("SELECT * FROM locations") suspend fun getAll(): List<Location>
    @Delete suspend fun delete(location: Location)
    @Update suspend fun update(location: Location)
    @Query("SELECT * FROM locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Location?

}
