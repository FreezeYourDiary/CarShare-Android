package com.example.carshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import com.example.carshare.session.UserSession
import data.database.AppDB
import data.entities.Car
import data.entities.Location
import data.entities.Trip
import kotlinx.coroutines.launch

@Composable
fun PostTripScreen(db: AppDB, onBack: () -> Unit) {

    // dummy hardcode integrate nav like L https://developer.android.com/guide/navigation/use-graph/pass-data
    // or like add map i przy click --> pobiera loke?
// https://googlemaps.github.io/android-maps-compose/maps-compose/com.google.maps.android.compose/-google-map.html
    // places - compose
    var startLocationName by remember { mutableStateOf("") }
    var startLocationAddress by remember { mutableStateOf("") }
    val startLat = "00.0"
    val startLng = "00.0"

    var endLocationName by remember { mutableStateOf("") }
    var endLocationAddress by remember { mutableStateOf("") }
    val endLat = "1.11"
    val endLng = "11.1"

    var plannedHour by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var selectedCarId by remember { mutableIntStateOf(-1) }
    var carList by remember { mutableStateOf(emptyList<Car>()) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val userId = UserSession.currentUserId
        if (userId != null) {
            // upd(fetch) cars jakie sa pod uzytkownikiem z bd + store them in the carList state(update ui) var
            // carlist to chose car to post
            carList = db.carDao().getCarsForUser(userId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Create New Trip", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Text("start inpt ")
            // mozna tez implementowac to in cardetails for ex
            OutlinedTextField(value = startLocationName, onValueChange = { startLocationName = it }, label = { Text("Name") })
            OutlinedTextField(value = startLocationAddress, onValueChange = { startLocationAddress = it }, label = { Text("Address") })
        }

        item {
            Text("End inpt")
            OutlinedTextField(value = endLocationName, onValueChange = { endLocationName = it }, label = { Text("Name") })
            OutlinedTextField(value = endLocationAddress, onValueChange = { endLocationAddress = it }, label = { Text("Address") })
        }

        item {
            Text("Planned Hour (0–24)")
            OutlinedTextField(
                value = plannedHour,
                onValueChange = { plannedHour = it },
                label = { Text("Hour") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Text("ilosc uzytkownikow (0–4)")
            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Capacity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        item {
            Text("Select Car")
        }

        items(carList) { car ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = car.id == selectedCarId,
                    onClick = { selectedCarId = car.id }
                )
                Text("${car.name} (${car.plate})")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val startLocation = Location(
                            name = startLocationName,
                            address = startLocationAddress,
                            latitude = startLat.toDouble(),
                            longitude = startLng.toDouble()
                        )

                        val endLocation = Location(
                            name = endLocationName,
                            address = endLocationAddress,
                            latitude = endLat.toDouble(),
                            longitude = endLng.toDouble()
                        )

                        val startLocationId = db.locationDao().insert(startLocation).toInt()
                        val endLocationId = db.locationDao().insert(endLocation).toInt()

                        val userId = UserSession.currentUserId ?: return@launch

                        val trip = Trip(
                            startLocationId = startLocationId,
                            endLocationId = endLocationId,
                            userId = userId,
                            carId = selectedCarId,
                            plannedHour = plannedHour.toIntOrNull() ?: 0,
                            tripState = "planned",
                            passengerCapacity = capacity.toIntOrNull() ?: 4,
                            currentPassengers = 0
                        )

                        db.tripDao().insert(trip)

                        Toast.makeText(context, "posted!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Text("Post Trip")
            }
        }
    }

}
