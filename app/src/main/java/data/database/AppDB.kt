package data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import data.dao.*
import data.entities.*

@Database(
    entities = [User::class, Car::class, Trip::class, Location::class, TripPassenger::class ],
    version = 5
)
abstract class AppDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun carDao(): CarDao
    abstract fun tripDao(): TripDao
    abstract fun locationDao(): LocationDao
    abstract fun tripPassengerDao(): TripPassengerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "carshare_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
