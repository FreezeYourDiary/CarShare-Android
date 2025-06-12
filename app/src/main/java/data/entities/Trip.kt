package data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startLocationId: Int,
    val endLocationId: Int,
    val userId: Int, // tez user/kierowca logika w userDB
    val plannedTime: Long // timestamp w ms?
)
