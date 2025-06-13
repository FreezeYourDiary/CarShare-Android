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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.carshare.session.UserSession
import com.example.carshare.ui.screens.*
import com.example.carshare.ui.theme.CarShareTheme
import data.dao.LocationDao
import data.database.AppDB
import data.entities.Car
import data.entities.Trip
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
                val database = remember { AppDB.getDatabase(context) } // weird db issue fix??
                val coroutineScope = rememberCoroutineScope()

                // screen control
                var showLogin by remember { mutableStateOf(false) }
                // changes of its value aktualizuje w ui
                var showRegister by remember { mutableStateOf(false) }
                var showPostTrip by remember { mutableStateOf(false) } // find to post -- find main screen
                var showUserPanel by remember { mutableStateOf(false) }
                var showUserInfo by remember { mutableStateOf(false) }
                var showCarDetail by remember { mutableStateOf<Car?>(null) }
                var loggedIn by remember { mutableStateOf(UserSession.currentUserId != null) }

                var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }
                val expandedTripIds = remember { mutableStateListOf<Long>() }
                var locationsMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

                // data loading block
                LaunchedEffect(Unit) {
                    // planned trips fetch dla main screen
                    // uproszczono do naprawy .! wyszukac lokalizacje
                    trips = database.tripDao().getTripsByState("planned")

                    // location ids from trips
                    val locationIds = trips.flatMap { listOf(it.startLocationId, it.endLocationId) }.toSet()
                    val locMap = mutableMapOf<Int, String>()
                    locationIds.forEach { id ->
                        val location = database.locationDao().getById(id)
                        location?.let { locMap[id] = it.name }
                    }
                    locationsMap = locMap
                }
                LaunchedEffect(UserSession.currentUserId) {
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
                                                    showUserInfo = false; showCarDetail = null
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Register") },
                                                onClick = {
                                                    showMenu = false
                                                    showRegister = true
                                                    showLogin = false; showPostTrip = false; showUserPanel = false;
                                                    showUserInfo = false; showCarDetail = null
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
                                                    showUserInfo = false; showCarDetail = null
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("my profile") },
                                                onClick = {
                                                    showMenu = false
                                                    showUserInfo = true
                                                    showLogin = false; showRegister = false; showPostTrip = false;
                                                    showUserPanel = false; showCarDetail = null
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("logout") },
                                                onClick = {
                                                    showMenu = false
                                                    coroutineScope.launch {
                                                        UserSession.currentUserId = null
                                                        loggedIn = false
                                                        // on main screen
                                                        showLogin = false; showRegister = false; showPostTrip = false;
                                                        showUserPanel = false; showUserInfo = false; showCarDetail = null
                                                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    // "+" -- w ogole change findtrip to post trip cause
                                    IconButton(onClick = {
                                        if (loggedIn) {
                                            showPostTrip = true
                                            showLogin = false; showRegister = false; showUserPanel = false;
                                            showUserInfo = false; showCarDetail = null
                                        } else {
                                            showLogin = true
                                            showPostTrip = false; showRegister = false; showUserPanel = false;
                                            showUserInfo = false; showCarDetail = null
                                            Toast.makeText(context, "Please log in first.", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Post a trip"
                                        )
                                    }

                                    // default
                                    IconButton(onClick = {
                                        showLogin = false; showRegister = false; showPostTrip = false;
                                        showUserPanel = false; showUserInfo = false; showCarDetail = null
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Menu,
                                            contentDescription = "View trips"
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
                                    showPostTrip = false; showRegister = false; showUserPanel = false;
                                    showUserInfo = false; showCarDetail = null
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
                                    showUserInfo = false; showCarDetail = null
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
                            // default screen, change column na lazy pozniej
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
                                            items(trips, key = { it.id }) { trip ->
                                                val isExpanded = expandedTripIds.contains(trip.id)
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            if (isExpanded) {
                                                                expandedTripIds.remove(trip.id)
                                                            } else {
                                                                expandedTripIds.add(trip.id)
                                                            }
                                                        },
                                                    elevation = CardDefaults.cardElevation(4.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp)) {
                                                        Text(
                                                            text = "Trip #${trip.id}",
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "From: ${locationsMap[trip.startLocationId] ?: "Loading..."}"
                                                        )
                                                        Text(
                                                            text = "To: ${locationsMap[trip.endLocationId] ?: "Loading..."}"
                                                        )

                                                        if (isExpanded) {
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text("Planned hour: ${trip.plannedHour}")
                                                            Text("Capacity: ${trip.passengerCapacity}")
                                                            Text("Current Passengers: ${trip.currentPassengers}")
                                                            Text("Trip state: ${trip.tripState}")
                                                            Text("Car ID: ${trip.carId}")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}