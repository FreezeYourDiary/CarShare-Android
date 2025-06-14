package com.example.carshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carshare.session.UserSession
import data.database.AppDB
import data.entities.Car // Import Car entity
import data.entities.Trip
import data.entities.TripPassenger
import data.entities.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripViewScreen(
    db: AppDB,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUserId = UserSession.currentUserId

    var userTrips by remember { mutableStateOf<List<Trip>>(emptyList()) }
    val expandedTripIds = remember { mutableStateListOf<Long>() } // For expanding/collapsing cards
    var locationsMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var userDriverTripsIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var userPassengerTripsIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // passengersOnTripMap --- DetailTripView fetches anyways
    // var passengersOnTripMap by remember { mutableStateOf<MutableMap<Long, List<User>>>(mutableMapOf()) }

    var selectedTripForDetails by remember { mutableStateOf<Trip?>(null) }


    LaunchedEffect(currentUserId, selectedTripForDetails) {
        if (currentUserId == null) {
            userTrips = emptyList()
            locationsMap = emptyMap()
            userDriverTripsIds = emptySet()
            userPassengerTripsIds = emptySet()
            // passengersOnTripMap = mutableMapOf()
            Toast.makeText(context, "Please log in to view your trips.", Toast.LENGTH_LONG).show()
            return@LaunchedEffect
        }

        val fetchedUserTrips = db.tripDao().getTripsWithUser(currentUserId.toLong())
        userTrips = fetchedUserTrips

        val locationIds = fetchedUserTrips.flatMap { listOf(it.startLocationId, it.endLocationId) }.toSet()
        val locMap = mutableMapOf<Int, String>()
        locationIds.forEach { id ->
            val location = db.locationDao().getById(id)
            location?.let { locMap[id] = it.name }
        }
        locationsMap = locMap

        userDriverTripsIds = fetchedUserTrips.filter { it.userId == currentUserId.toInt() }.map { it.id }.toSet()
        val passengerTrips = db.tripPassengerDao().getTripsForPassenger(currentUserId.toLong())
        userPassengerTripsIds = passengerTrips.map { it.tripId }.toSet()
    }


    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (currentUserId == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Please log in to view your trips.", style = MaterialTheme.typography.headlineSmall)
                        }
                    } else if (selectedTripForDetails != null) {
                        DetailedTripView(
                            db = db,
                            trip = selectedTripForDetails!!,
                            onTripUpdated = { updatedTrip ->
                                userTrips = userTrips.map { if (it.id == updatedTrip.id) updatedTrip else it }
                                selectedTripForDetails = updatedTrip
                            },
                            onBackToTripsList = { selectedTripForDetails = null }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (userTrips.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "no available trips found",
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                    }
                                }
                            } else {
                                items(userTrips, key = { it.id }) { trip ->
                                    val cardBackgroundColor = when {
                                        userDriverTripsIds.contains(trip.id) -> Color(0xFFE8F5E9)
                                        userPassengerTripsIds.contains(trip.id) -> Color(0xFFE3F2FD)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedTripForDetails = trip
                                            },
                                        elevation = CardDefaults.cardElevation(4.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Trip #${trip.id} - ${trip.tripState.replaceFirstChar { it.uppercase() }}",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "From: ${locationsMap[trip.startLocationId] ?: "Err"}"
                                            )
                                            Text(
                                                text = "To: ${locationsMap[trip.endLocationId] ?: "Err"}"
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Passengers: ${trip.currentPassengers}/${trip.passengerCapacity}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedTripForDetails != null) {
                            selectedTripForDetails = null
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedTripView(
    db: AppDB,
    trip: Trip,
    onTripUpdated: (Trip) -> Unit,
    onBackToTripsList: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUserId = UserSession.currentUserId

    var currentTripState by remember { mutableStateOf(trip) }
    var driverDetails by remember { mutableStateOf<User?>(null) } // driver (userid) obj
    var carDetails by remember { mutableStateOf<Car?>(null) }
    var currentPassengersDetailsList by remember { mutableStateOf<List<User>>(emptyList()) } // paasenger user id  odw.
    var startLocationName by remember { mutableStateOf("Err") }
    var endLocationName by remember { mutableStateOf("Err") }

    // !!! details for current trip
    LaunchedEffect(currentTripState.id, currentUserId) {
        // teoretycznie to samo co w tripviewscreen
        db.tripDao().getById(currentTripState.id)?.let { fetchedTrip ->
            currentTripState = fetchedTrip
        }
        db.userDao().getUserById(currentTripState.userId.toLong())?.let {
            driverDetails = it
        }

        val driverCars = db.carDao().getCarsForUser(currentTripState.userId)
        carDetails = driverCars.find { it.id.toLong() == currentTripState.carId.toLong() }
        db.locationDao().getById(currentTripState.startLocationId)?.let {
            startLocationName = it.name
        }
        db.locationDao().getById(currentTripState.endLocationId)?.let {
            endLocationName = it.name
        }
        val tripPassengersEntities = db.tripPassengerDao().getPassengersForTrip(currentTripState.id)
        val usersForTrip = mutableListOf<User>()
        for (tp in tripPassengersEntities) {
            db.userDao().getUserById(tp.passengerId)?.let { user ->
                usersForTrip.add(user)
            }
        }
        currentPassengersDetailsList = usersForTrip
    }

    val isDriver = currentUserId != null && currentTripState.userId == currentUserId.toInt()
    val isPassenger = currentUserId != null && currentPassengersDetailsList.any { it.id == currentUserId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Trip ID: #${currentTripState.id}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Driver Information:",
            style = MaterialTheme.typography.titleMedium
        )
        driverDetails?.let { driver ->
            Text("Name: ${driver.name}", color = if (isDriver) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            if (isDriver || isPassenger) {
                Text("Phone: ${driver.phone ?: "N/A"}")
            }
        } ?: Text("Driver: Err")
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Car Information:",
            style = MaterialTheme.typography.titleMedium
        )
        carDetails?.let { car ->
            Text("Name: ${car.name}")
            Text("Year: ${car.year}")
            Text("License Plate: ${car.plate}")
        } ?: Text("Car: Loading...")
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Trip Details:",
            style = MaterialTheme.typography.titleMedium
        )
        Text("From: $startLocationName")
        Text("To: $endLocationName")
        Text("Planned hour: ${currentTripState.plannedHour}:00")
        Text("Capacity: ${currentTripState.passengerCapacity}")
        Text("Current Passengers: ${currentTripState.currentPassengers}")
        Text("Trip state: ${currentTripState.tripState.replaceFirstChar { it.uppercase() }}")
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Passengers:",
            style = MaterialTheme.typography.titleSmall
        )
        if (currentPassengersDetailsList.isEmpty()) {
            Text("No passengers yet.")
        } else {
            currentPassengersDetailsList.forEach { passenger ->
                Text(
                    text = " - ${passenger.name} (ID: ${passenger.id})" +
                            (if (isDriver || passenger.id == currentUserId) " - Phone: ${passenger.phone ?: "N/A"}" else ""),
                    color = if (currentUserId != null && passenger.id == currentUserId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isTripPlanned = currentTripState.tripState == "planned"
        val isTripActive = currentTripState.tripState == "active"
        var canJoin = currentUserId != null && !isDriver && !isPassenger && currentTripState.currentPassengers < currentTripState.passengerCapacity && isTripPlanned
        val canLeave = currentUserId != null && isPassenger && isTripPlanned // !? Can only leave planned trips
        val canCancel = isDriver && (isTripPlanned || isTripActive)
        val canEnd = isDriver && isTripActive

        if (isDriver) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (canCancel) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val updatedTrip = currentTripState.copy(tripState = "canceled")
                                db.tripDao().update(updatedTrip)
                                db.tripPassengerDao().getPassengersForTrip(currentTripState.id).forEach { tp ->
                                    db.tripPassengerDao().delete(tp)
                                }
                                onTripUpdated(updatedTrip)
                                Toast.makeText(context, "Trip Canceled!", Toast.LENGTH_SHORT).show()
                                onBackToTripsList()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel")
                    }
                }
                if (canEnd) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val updatedTrip = currentTripState.copy(tripState = "ended")
                                db.tripDao().update(updatedTrip)
                                db.tripPassengerDao().getPassengersForTrip(currentTripState.id).forEach { tp ->
                                    db.tripPassengerDao().delete(tp)
                                }
                                onTripUpdated(updatedTrip)
                                Toast.makeText(context, "Trip Ended!", Toast.LENGTH_SHORT).show()
                                onBackToTripsList()
                            }
                        }
                    ) {
                        Text("End Trip")
                    }
                } else if (isTripPlanned) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val updatedTrip = currentTripState.copy(tripState = "active")
                                db.tripDao().update(updatedTrip)
                                onTripUpdated(updatedTrip)
                                Toast.makeText(context, "Trip Started!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Start")
                    }
                }
            }
        } else if (currentUserId != null) {
            if (canJoin) {
                //  joining from mainactivity
                // +trips state changes handling
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val newTripPassenger = TripPassenger(
                                tripId = currentTripState.id,
                                passengerId = currentUserId.toLong()
                            )
                            db.tripPassengerDao().insert(newTripPassenger)

                            val updatedTrip = currentTripState.copy(currentPassengers = currentTripState.currentPassengers + 1)
                            db.tripDao().update(updatedTrip)
                            onTripUpdated(updatedTrip)
                            Toast.makeText(context, "Joined Trip!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Join Trip")
                }
            } else if (canLeave) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            db.tripPassengerDao().deleteTripPassenger(currentTripState.id,
                                currentUserId.toLong()
                            )

                            val updatedTrip = currentTripState.copy(currentPassengers = currentTripState.currentPassengers - 1)
                            db.tripDao().update(updatedTrip)
                            onTripUpdated(updatedTrip)
                            Toast.makeText(context, "Left Trip!", Toast.LENGTH_SHORT).show()
                            canJoin=true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Leave Trip")
                }
            } else if (currentTripState.tripState != "planned") {
                Text("Trip is ${currentTripState.tripState.replaceFirstChar { it.uppercase() }}.", style = MaterialTheme.typography.bodyLarge)
            } else if (currentTripState.currentPassengers >= currentTripState.passengerCapacity && !isPassenger) {
                Text("Trip is full.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}