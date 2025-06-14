package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import data.entities.User
/**
 * UserDao - interfejs dostępu do danych użytkownika w Room.
 * czyli kazda z tych funkcji zapewnia jak beda wywolywane pracy na bazie danych w programie pozniej
 */
@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1") // basic login?
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?
    // flow return type for in real time data?
}