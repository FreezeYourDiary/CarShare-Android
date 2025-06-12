package com.example.carshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carshare.session.UserSession
import data.database.AppDB
import data.entities.Car
import data.entities.User
import kotlinx.coroutines.launch

@Composable
fun UserInfo(db: AppDB, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser = remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current

    val userId = UserSession.currentUserId

    // user info in real time
    LaunchedEffect(userId) {
        if (userId != null) {
            coroutineScope.launch {
                currentUser.value = db.userDao().getUserById(userId)
            }
            // wymaga check type mismatch Int? Int. teoretycznie nie mozliwy else
        } else {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        //styles ewentualnie tez dodac do login/register
        Text("My Profile", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        currentUser.value?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Name: ${user.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${user.email}", style = MaterialTheme.typography.bodyLarge)
                    Text("Phone: ${user.phone}", style = MaterialTheme.typography.bodyLarge)
                    Text("User Type: ${user.type}", style = MaterialTheme.typography.bodyLarge)
                    // Text("User Type: ${user.password}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
//        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
//            Text("Back")
//        }
    }
}


@Composable
fun CarInfo(car: Car, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Car Details", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Name: ${car.name}")
                Text("Year: ${car.year}")
                Text("Color: ${car.color}")
                Text("State: ${car.state}")
                Text("License Plate: ${car.plate}")

                //Text("Owner User ID: ${car.ownerUserId}")
            }
        }
//        Spacer(modifier = Modifier.height(10.dp))
//        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
//            Text("Back")
//        }
    }
}