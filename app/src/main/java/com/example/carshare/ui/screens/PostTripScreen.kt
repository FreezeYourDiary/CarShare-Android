package com.example.carshare.ui.screens

import android.location.Geocoder
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
import androidx.compose.ui.window.Dialog
import com.example.carshare.session.UserSession
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import data.database.AppDB
import data.entities.Car
import data.entities.Location
import data.entities.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

@Composable
fun PostTripScreen(db: AppDB, onBack: () -> Unit) {

    var startLocationName by remember { mutableStateOf("") }
    var startLocationAddress by remember { mutableStateOf("") }

    var endLocationName by remember { mutableStateOf("") }
    var endLocationAddress by remember { mutableStateOf("") }

    var startLatLng by remember { mutableStateOf<LatLng?>(null) }
    var endLatLng by remember { mutableStateOf<LatLng?>(null) }

    var selectingStart by remember { mutableStateOf(true) }
    var showMapDialog by remember { mutableStateOf(false) } // State to control dialog visibility

    var plannedHour by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var selectedCarId by remember { mutableIntStateOf(-1) }
    var carList by remember { mutableStateOf(emptyList<Car>()) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        UserSession.currentUserId?.let {
            carList = db.carDao().getCarsForUser(it)
        }
    }

    if (showMapDialog) {
        MapSelectionDialog(
            onDismiss = { showMapDialog = false },
            onLocationSelected = { latLng, city, address ->
                if (selectingStart) {
                    startLatLng = latLng
                    startLocationName = city
                    startLocationAddress = address
                } else {
                    endLatLng = latLng
                    endLocationName = city
                    endLocationAddress = address
                }
                showMapDialog = false
            },
            initialCameraPosition = if (selectingStart) startLatLng else endLatLng
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Create New Trip", style = MaterialTheme.typography.headlineSmall) }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    selectingStart = true
                    showMapDialog = true // when button clicked
                }) {
                    Text("Select Start on Map")
                }
                Button(onClick = {
                    selectingStart = false
                    showMapDialog = true
                }) {
                    Text("Select End on Map")
                }
            }
        }

        item {
            if (startLatLng != null) {
                Text("Start Location: ${startLocationName} (${startLocationAddress})")
            } else {
                Text("Start Location: Not selected")
            }
        }

        item {
            if (endLatLng != null) {
                Text("End Location: ${endLocationName} (${endLocationAddress})")
            } else {
                Text("End Location: Not selected")
            }
        }

        item {
            OutlinedTextField(
                value = plannedHour,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^([01]?\\d|2[0-3])$"))) plannedHour = it //  0 to 23, in either a single-digit (0-9) or two-digit (00-23) format.
                },
                label = { Text("Planned Hour (0-23)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = plannedHour.isNotEmpty() && (plannedHour.toIntOrNull() == null || plannedHour.toInt() !in 0..23),
                supportingText = {
                    if (plannedHour.isNotEmpty() && (plannedHour.toIntOrNull() == null || plannedHour.toInt() !in 0..23)) {
                        Text("Please enter a valid hour (0–23).")
                    }
                }
            )
        }

        item {
            Text("Capacity (0–4)")
            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Seats") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Text("Select Car")
            if (carList.isEmpty()) {
                Text("No cars available. Add a car in 'My Cars'.", color = MaterialTheme.colorScheme.error)
            }
        }

        items(carList) { car ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCarId = car.id }
            ) {
                RadioButton(selected = car.id == selectedCarId, onClick = { selectedCarId = car.id })
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
                            if (userId == null) {
                                Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val plannedHourInt = plannedHour.toIntOrNull()
                            val capacityInt = capacity.toIntOrNull()

                            if (plannedHourInt == null || plannedHourInt !in 0..23) {
                                Toast.makeText(context, "Invalid hour (0-23).", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            if (capacityInt == null || capacityInt !in 0..4) {
                                Toast.makeText(context, "Invalid capacity (0–4).", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            // init select = -1, czyli nie selected to initselect
                            if (selectedCarId == -1) {
                                Toast.makeText(context, "Please select a car.", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            if (startLatLng == null || endLatLng == null) {
                                Toast.makeText(context, "Please select both start and end locations.", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            // startlocation name, startlocation adress <-- geocoder
                            val startLocation = Location(
                                name = startLocationName,
                                address = startLocationAddress,
                                latitude = startLatLng!!.latitude,
                                longitude = startLatLng!!.longitude
                            )

                            val endLocation = Location(
                                name = endLocationName,
                                address = endLocationAddress,
                                latitude = endLatLng!!.latitude,
                                longitude = endLatLng!!.longitude
                            )

                            val startLocationId = db.locationDao().insert(startLocation).toInt()
                            val endLocationId = db.locationDao().insert(endLocation).toInt()
                            // teoretycznie nie
                            if (startLocationId == endLocationId) {
                                Toast.makeText(context, "same start and end location", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            val trip = Trip(
                                startLocationId = startLocationId,
                                endLocationId = endLocationId,
                                userId = userId.toInt(),
                                carId = selectedCarId,
                                plannedHour = plannedHourInt,
                                tripState = "planned",
                                passengerCapacity = capacityInt,
                                currentPassengers = 0
                            )

                            db.tripDao().insert(trip)

                            Toast.makeText(context, "posted!", Toast.LENGTH_SHORT).show()
                            onBack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error posting trip: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            ) {
                Text("Post Trip")
            }
        }
    }
}

@Composable
fun MapSelectionDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (LatLng, String, String) -> Unit, // city+dadress dynamicznie
    initialCameraPosition: LatLng?
) {
    var selectedLatLng by remember { mutableStateOf(initialCameraPosition) }
    var selectedCityName by remember { mutableStateOf("Loading city...") }
    var selectedAddress by remember { mutableStateOf("Loading address...") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Reverse geocode dla ---> selectedLatLng changes
    LaunchedEffect(selectedLatLng) {
        selectedLatLng?.let { latLng ->
            coroutineScope.launch {
                val geocoder = Geocoder(context, Locale.getDefault())
                try {
                    val addresses = withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    }
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        selectedCityName = address.locality ?: address.subAdminArea ?: "Unknown City"
                        selectedAddress = address.getAddressLine(0) ?: "Unknown Address"
                    } else {
                        selectedCityName = "No city found"
                        selectedAddress = "No address found"
                    }
                } catch (e: IOException) {
                    selectedCityName = "Network error"
                    selectedAddress = "Network error"
                    Toast.makeText(context, "Network error during geocoding: ${e.message}", Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    selectedCityName = "Invalid coordinates"
                    selectedAddress = "Invalid coordinates"
                    Toast.makeText(context, "Invalid coordinates for geocoding: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Location on Map", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Text("City: $selectedCityName", style = MaterialTheme.typography.bodyLarge)
                Text("Address: $selectedAddress", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))


                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(selectedLatLng ?: LatLng(51.107883, 17.038538), 5f)
                }

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLatLng = latLng
                    }
                ) {
                    selectedLatLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Selected Location",
                            snippet = "Lat: ${it.latitude}, Lng: ${it.longitude}"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            selectedLatLng?.let {
                                onLocationSelected(it, selectedCityName, selectedAddress)
                            }
                        },
                        enabled = selectedLatLng != null && selectedCityName != "Loading city..." && selectedCityName != "No city found"
                    ) {
                        Text("Confirm Selection")
                    }
                }
            }
        }
    }
}