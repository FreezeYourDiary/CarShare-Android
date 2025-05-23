package com.example.carshare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.carshare.ui.theme.CarShareTheme
import data.database.AppDB
import data.entities.User
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDB.getDatabase(this)
        val userDao = db.userDao()

        lifecycleScope.launch {
            val userId = userDao.insert(
                User(
                name = "test user",
                phone = "0101010",
                email = "test@test.pwr",
                password = "test123",
                type = "kierowca",
            )
            )

            val users = userDao.getAll()
            users.forEach {
                Log.d("DATABASE_TEST", "User: ${it.name}, Email: ${it.email}") // w logcat filtr DATABASE_TEST
            }
        }
    }
}