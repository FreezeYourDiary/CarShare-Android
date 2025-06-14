package com.example.carshare

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.carshare.session.UserSession
import com.example.carshare.ui.screens.*
import com.example.carshare.ui.theme.CarShareTheme
import data.database.AppDB
import data.entities.Car
import data.entities.Trip
import data.entities.TripPassenger
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDB.getDatabase(this)
        val userDao = db.userDao()
        val carDao = db.carDao()
        val tripDao = db.tripDao()
        val locDao = db.locationDao()

        lifecycleScope.launch {
            val users = userDao.getAll()
            users.forEach { user ->
                Log.d("DB_STATE", "User: id=${user.id}, name=${user.name}, email=${user.email}, phone=${user.phone}, type=${user.type}, pass=${user.password}")
            }
            val cars = carDao.getAll()
            cars.forEach { car ->
                Log.d("DB_STATE", "Car: id=${car.id}, ownerUserId=${car.ownerUserId}, name=${car.name}, year=${car.year}, plate=${car.plate}, color=${car.color}, state=${car.state}")
            }
            val trips = tripDao.getAll()
            trips.forEach { trip ->
                Log.d(
                    "DB_STATE", "Trip: id=${trip.id}, userId=${trip.userId}, carId=${trip.carId}, " +
                            "startLocationId=${trip.startLocationId}, endLocationId=${trip.endLocationId}, " +
                            "plannedHour=${trip.plannedHour}, state=${trip.tripState}, " +
                            "capacity=${trip.passengerCapacity}, currentPassengers=${trip.currentPassengers}"
                )
            }
            val locs = locDao.getAll()
            locs.forEach { loc ->
                Log.d(
                    "LOCATIONS", "Loc id= ${loc.id}, addr=${loc.address}"
                )
            }
        }

        setContent {
            CarShareTheme {
                val context = LocalContext.current
                val database = remember { AppDB.getDatabase(context) }
                val coroutineScope = rememberCoroutineScope()
                // Screen control
                var showLogin by remember { mutableStateOf(false) }
                var showRegister by remember { mutableStateOf(false) }
                var showPostTrip by remember { mutableStateOf(false) }
                var showUserPanel by remember { mutableStateOf(false) }
                var showUserInfo by remember { mutableStateOf(false) }
                var showCarDetail by remember { mutableStateOf<Car?>(null) }
                var loggedIn by remember { mutableStateOf(UserSession.currentUserId != null) }
                // mytrips - tripviewscreen
                var showMyTripsScreen by remember { mutableStateOf(false) }
                // join trip dialog
                var showJoinTripDialogForTrip by remember { mutableStateOf<Trip?>(null) }


                // ONLY MAIN SCREEEN --- ONLY PLANNED TRIPS
                var tripsMainScreen by remember { mutableStateOf<List<Trip>>(emptyList()) }
                var locationsOnMainScreen by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
                // coloring lists on ms
                var userDriverTrips by remember { mutableStateOf<Set<Long>>(emptySet()) }
                var userPassengerTrips by remember { mutableStateOf<Set<Long>>(emptySet()) }


                LaunchedEffect(loggedIn) {
                    val fetchedPlannedTrips = database.tripDao().getTripsByState("planned")
                    tripsMainScreen = fetchedPlannedTrips

                    val locationIds = fetchedPlannedTrips.flatMap { listOf(it.startLocationId, it.endLocationId) }.toSet()
                    val locMap = mutableMapOf<Int, String>()
                    locationIds.forEach { id ->
                        val location = database.locationDao().getById(id)
                        location?.let { locMap[id] = it.name }
                    }
                    locationsOnMainScreen = locMap

                    val currentUserId = UserSession.currentUserId
                    if (currentUserId != null) {
                        val allUserTrips = database.tripDao().getTripsWithUser(currentUserId.toLong())
                        userDriverTrips = allUserTrips.filter { it.userId == currentUserId.toInt() }.map { it.id }.toSet()
                        val passengerTrips = database.tripPassengerDao().getTripsForPassenger(
                            currentUserId.toLong()
                        )
                        userPassengerTrips = passengerTrips.map { it.tripId }.toSet()
                    } else {
                        userDriverTrips = emptySet()
                        userPassengerTrips = emptySet()
                    }
                }

                LaunchedEffect(UserSession.currentUserId) {
                    // planned trips fetch dla main screen
                    // uproszczono do naprawy .! wyszukac lokalizacje
                    loggedIn = UserSession.currentUserId != null
                }

                var showMenu by remember { mutableStateOf(false) }
                // icons https://developer.android.com/reference/kotlin/androidx/compose/material/icons/Icons.Filled
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.primary,
                            actions = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { showMenu = !showMenu }) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "",
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        if (!loggedIn) {
                                            DropdownMenuItem(
                                                text = { Text("Login") },
                                                onClick = {
                                                    showMenu = false
                                                    showLogin = true
                                                    showRegister = false; showPostTrip = false; showUserPanel = false;
                                                    showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Register") },
                                                onClick = {
                                                    showMenu = false
                                                    showRegister = true
                                                    showLogin = false; showPostTrip = false; showUserPanel = false;
                                                    showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                                }
                                            )
                                        }

                                        if (loggedIn) {
                                            DropdownMenuItem(
                                                text = { Text("cars") },
                                                onClick = {
                                                    showMenu = false
                                                    showUserPanel = true
                                                    showLogin = false; showRegister = false; showPostTrip = false;
                                                    showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("my profile") },
                                                onClick = {
                                                    showMenu = false
                                                    showUserInfo = true
                                                    showLogin = false; showRegister = false; showPostTrip = false;
                                                    showUserPanel = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("logout") },
                                                onClick = {
                                                    showMenu = false
                                                    coroutineScope.launch {
                                                        UserSession.currentUserId = null
                                                        loggedIn = false
                                                        showLogin = false; showRegister = false; showPostTrip = false;
                                                        showUserPanel = false; showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    IconButton(onClick = {
                                        if (loggedIn) {
                                            showPostTrip = true
                                            showLogin = false; showRegister = false; showUserPanel = false;
                                            showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                        } else {
                                            showLogin = true
                                            showPostTrip = false; showRegister = false; showUserPanel = false;
                                            showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                            Toast.makeText(context, "Please log in first.", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Post a trip"
                                        )
                                    }

                                    // was default --> navigato to tripviewscreen
                                    IconButton(onClick = {
                                        if (loggedIn) {
                                            showMyTripsScreen = true
                                            showLogin = false; showRegister = false; showPostTrip = false;
                                            showUserPanel = false; showUserInfo = false; showCarDetail = null; showJoinTripDialogForTrip = null
                                        } else {
                                            showLogin = true
                                            Toast.makeText(context, "Please log in first.", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Menu,
                                            contentDescription = "My Trips"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    floatingActionButton = {}
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            showLogin -> LoginScreen(
                                onLoginSuccess = {
                                    loggedIn = true
                                    showLogin = false
                                    // !EFAULT WHEN LOGGED IN TO START
                                    showPostTrip = false; showRegister = false; showUserPanel = false;
                                    showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                },
                                onNavigateToRegister = {
                                    showLogin = false
                                    showRegister = true
                                }
                            )
                            showRegister -> RegisterScreen(
                                onRegisterSuccess = {
                                    loggedIn = true
                                    showRegister = false
                                    showPostTrip = false; showLogin = false; showUserPanel = false;
                                    showUserInfo = false; showCarDetail = null; showMyTripsScreen = false; showJoinTripDialogForTrip = null
                                },
                                onNavigateToLogin = {
                                    showRegister = false
                                    showLogin = true
                                }
                            )
                            showPostTrip -> PostTripScreen(
                                db = database,
                                onBack = { showPostTrip = false }
                            )
                            showUserPanel -> UserPanel(
                                db = database,
                                onBack = { showUserPanel = false }
                            )
                            showUserInfo -> UserInfo(
                                db = database,
                                onBack = { showUserInfo = false }
                            )
                            showCarDetail != null -> CarInfo(
                                car = showCarDetail!!,
                                onBack = { showCarDetail = null }
                            )
                            showMyTripsScreen -> TripViewScreen(
                                db = database,
                                onBack = { showMyTripsScreen = false }
                            )
                            else -> {
                                Scaffold(
                                    topBar = {
                                        TopAppBar(title = { Text("Available Trips") })
                                    },
                                    content = { innerScaffoldPadding ->
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(innerScaffoldPadding),
                                            contentPadding = PaddingValues(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (tripsMainScreen.isEmpty()) {
                                                item {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillParentMaxSize()
                                                            .padding(16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "No available trips at the moment.",
                                                            style = MaterialTheme.typography.headlineSmall
                                                        )
                                                    }
                                                }
                                            } else {
                                                items(tripsMainScreen, key = { it.id }) { trip ->
                                                    val cardBackgroundColor = when {
                                                        userDriverTrips.contains(trip.id) -> Color(0xffa4ffa4)
                                                        userPassengerTrips.contains(trip.id) -> Color(0xFF91D2FF)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                if (loggedIn) {
                                                                    showJoinTripDialogForTrip = trip
                                                                } else {
                                                                    showLogin = true
                                                                    Toast.makeText(context, "Please log in to join a trip.", Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                        elevation = CardDefaults.cardElevation(4.dp),
                                                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                                                    ) {
                                                        Column(modifier = Modifier.padding(16.dp)) {
                                                            Text(
                                                                text = "Trip #${trip.id}",
                                                                style = MaterialTheme.typography.titleMedium
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "From: ${locationsOnMainScreen[trip.startLocationId]}"
                                                            )
                                                            Text(
                                                                text = "To: ${locationsOnMainScreen[trip.endLocationId]}"
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "Passengers: ${trip.currentPassengers}/${trip.passengerCapacity}",
                                                            )
                                                            Text(
                                                                text = "State: ${trip.tripState.replaceFirstChar { it.uppercase() }}",
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )

                                showJoinTripDialogForTrip?.let { tripToJoin ->
                                    JoinTripDialog(
                                        db = database,
                                        trip = tripToJoin,
                                        onDismiss = { showJoinTripDialogForTrip = null },
                                        onJoinSuccess = {
                                            coroutineScope.launch {
                                                val fetchedPlannedTrips = database.tripDao().getTripsByState("planned")
                                                tripsMainScreen = fetchedPlannedTrips

                                                val currentUserId = UserSession.currentUserId
                                                if (currentUserId != null) {
                                                    val allUserTrips = database.tripDao().getTripsWithUser(
                                                        currentUserId.toLong()
                                                    )
                                                    userDriverTrips = allUserTrips.filter { it.userId == currentUserId.toInt() }.map { it.id }.toSet()
                                                    val passengerTrips = database.tripPassengerDao().getTripsForPassenger(
                                                        currentUserId.toLong()
                                                    )
                                                    userPassengerTrips = passengerTrips.map { it.tripId }.toSet()
                                                }
                                            }
                                            showJoinTripDialogForTrip = null
                                            Toast.makeText(context, "joined Trip #${tripToJoin.id}!", Toast.LENGTH_SHORT).show()
                                        },
                                        onAlreadyJoined = {
                                            showJoinTripDialogForTrip = null
                                            Toast.makeText(context, "!!You have registered and then cancelled", Toast.LENGTH_SHORT).show()
                                        },
                                        onTripFull = {
                                            showJoinTripDialogForTrip = null
                                            Toast.makeText(context, "trip is full.", Toast.LENGTH_SHORT).show()
                                        },
                                        isDriver = userDriverTrips.contains(tripToJoin.id),
                                        isPassenger = userPassengerTrips.contains(tripToJoin.id)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// New Composable for the Join Trip Dialog
@Composable
fun JoinTripDialog(
    db: AppDB,
    trip: Trip,
    onDismiss: () -> Unit,
    onJoinSuccess: () -> Unit,
    onAlreadyJoined: () -> Unit,
    onTripFull: () -> Unit,
    isDriver: Boolean,
    isPassenger: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = UserSession.currentUserId

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Trip #${trip.id}?") },
        text = {
            Column {
                Text("From: ${trip.startLocationId} To: ${trip.endLocationId}")
                Text("Time: ${trip.plannedHour}:00")
                Text("Available seats: ${trip.passengerCapacity - trip.currentPassengers}")
                Spacer(modifier = Modifier.height(8.dp))
                if (isDriver) {
                    Text("You are the driver of this trip.", color = MaterialTheme.colorScheme.primary)
                } else if (isPassenger) {
                    Text("You are already a passenger on this trip.", color = MaterialTheme.colorScheme.primary)
                } else if (trip.currentPassengers >= trip.passengerCapacity) {
                    Text("This trip is full.", color = MaterialTheme.colorScheme.error)
                } else if (trip.tripState != "planned") {
                    Text("This trip is ${trip.tripState}.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            val canJoin = currentUserId != null && !isDriver && !isPassenger &&
                    trip.currentPassengers < trip.passengerCapacity && trip.tripState == "planned"

            Button(
                onClick = {
                    if (canJoin) {
                        coroutineScope.launch {
                            val newTripPassenger = TripPassenger(
                                tripId = trip.id,
                                passengerId = currentUserId!!.toLong()
                            )
                            db.tripPassengerDao().insert(newTripPassenger)

                            val updatedTrip = trip.copy(currentPassengers = trip.currentPassengers + 1)
                            db.tripDao().update(updatedTrip)
                            onJoinSuccess()
                        }
                    } else if (isPassenger) {
                        onAlreadyJoined()
                    } else if (trip.currentPassengers >= trip.passengerCapacity) {
                        onTripFull()
                    }
                },
                enabled = canJoin // Only enable if eligible to join
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}