package com.example.carshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
            OutlinedTextField(
                value = startLocationName,
                onValueChange = { startLocationName = it },
                label = { Text("Name") })
            OutlinedTextField(
                value = startLocationAddress,
                onValueChange = { startLocationAddress = it },
                label = { Text("Address") })
        }

        item {
            Text("End inpt")
            OutlinedTextField(
                value = endLocationName,
                onValueChange = { endLocationName = it },
                label = { Text("Name") })
            OutlinedTextField(
                value = endLocationAddress,
                onValueChange = { endLocationAddress = it },
                label = { Text("Address") })
        }

        item {
            OutlinedTextField(
                value = plannedHour,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^([01]?[0-9]|2[0-3])$"))) {
                        plannedHour = newValue
                    }
                },
                label = { Text("Planned Hour (0-23)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = plannedHour.isNotEmpty() && (plannedHour.toIntOrNull() == null || plannedHour.toInt() < 0 || plannedHour.toInt() > 23),
                supportingText = {
                    if (plannedHour.isNotEmpty() && (plannedHour.toIntOrNull() == null || plannedHour.toInt() < 0 || plannedHour.toInt() > 23)) {
                        Text("Please enter a valid hour (0-23).")
                    }
                }
            )
        }

        item {
            Text("ilosc uzytkownikow (0–4)")
            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Miejsca") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        item {
            Text("Select Car")
            if (carList.isEmpty()) {
                Text(
                    "No cars available.",
                    style = MaterialTheme.typography.bodySmall,
                    // color = MaterialTheme.colorScheme.error
                )
            }
        }
        items(carList) { car ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCarId = car.id }
            ) {
                RadioButton(
                    selected = car.id == selectedCarId,
                    onClick = { selectedCarId = car.id }
                )
                Text("${car.name}")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val userId = UserSession.currentUserId
                            // tez null assertion needed dla db inputa
                            if (userId == null) {
                                Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT)
                                    .show()
                                return@launch
                            }

                            val plannedHourInt = plannedHour.toIntOrNull()
                            val capacityInt = capacity.toIntOrNull()
                            if (plannedHourInt == null || plannedHourInt < 0 || plannedHourInt > 23) {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid hour (0-23).",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }

                            if (capacityInt == null || capacityInt < 0 || capacityInt > 4) {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid capacity (0-4).",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }
                            // init select = -1, czyli nie selected to initselect
                            if (selectedCarId == -1) {
                                Toast.makeText(context, "Please select a car.", Toast.LENGTH_LONG)
                                    .show()
                                return@launch
                            }

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

                            // teoretycznie nie nastapi ale ?null type mismatch anyway
                            if (startLocationId == endLocationId) {
                                Toast.makeText(
                                    context,
                                    "Start and end locations cannot be the same.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }

                            val trip = Trip(
                                startLocationId = startLocationId,
                                endLocationId = endLocationId,
                                userId = userId.toInt(),
                                carId = selectedCarId!!,
                                plannedHour = plannedHourInt,
                                tripState = "planned",
                                passengerCapacity = capacityInt,
                                currentPassengers = 0
                            )

                            db.tripDao().insert(trip)

                            Toast.makeText(context, "posted!", Toast.LENGTH_SHORT)
                                .show()
                            // nav back after post
                            onBack()

                        } catch (e: NumberFormatException) {
                            // input z google maps czy przejdzie to
                            Toast.makeText(
                                context,
                                "Error with location: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error posting trip: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            e.printStackTrace()
                        }
                    }
                }
            ) {
                Text("Post Trip")
            }
        }
    }
}
