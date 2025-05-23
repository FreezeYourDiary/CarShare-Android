package data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val type: String, // kierowca / pasażer / oba - lepiej enum (zapezpieczenie na viewModl??)
    // val carId: Int? = null
)