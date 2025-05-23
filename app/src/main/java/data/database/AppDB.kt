package data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.dao.UserDao
import data.entities.User

@Database(entities = [User::class], version = 1)
abstract class AppDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    // abstract fun carDao(): CarDao // car info[name, year, color, state, plate, etc.]
    // abstract fun tripDao(): CarDao  // trip info[startLocation : locationDao, endLocation, users : UserDao, planned time] + func to match locations
    // abstract fun locationDao(): CarDao // location info[address, coords?, name(adress)]
    companion object {
        @Volatile private var INSTANCE: AppDB? = null

        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "carshare_db"
                )
                    .fallbackToDestructiveMigration(true)
                    // przy update (np dodaniu) nowych var w tabelach, dodaniu nowych tabel.
                    .build().also { INSTANCE = it }

            }
        }
    }
}
