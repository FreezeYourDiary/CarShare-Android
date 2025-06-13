package data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Trip.kt
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startLocationId: Int,
    val endLocationId: Int,
    val userId: Int, // driver ID
    val carId: Int,
    val plannedHour: Int, // 0-24, as Int
    val tripState: String = "planned", // "planned", "active", "canceled", "ended"
    val passengerCapacity: Int = 4,
    val currentPassengers: Int = 0
)
