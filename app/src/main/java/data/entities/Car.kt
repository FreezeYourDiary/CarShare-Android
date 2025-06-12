package data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cars",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["ownerUserId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerUserId: Int,      //  FK  User.id
    val name: String,
    val year: Int,
    val color: String,
    val state: String,
    val plate: String
)
