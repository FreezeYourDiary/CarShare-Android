package com.example.carshare

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.carshare.ui.theme.CarShareTheme
import data.database.AppDB
import data.entities.*
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.compose.material3.BottomAppBar
import com.example.carshare.session.UserSession
import com.example.carshare.ui.screens.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDB.getDatabase(this)
        val userDao = db.userDao()
        val carDao = db.carDao()

        lifecycleScope.launch {
            val users = userDao.getAll()
            users.forEach { user ->
                Log.d("DB_STATE", "User: id=${user.id}, name=${user.name}, email=${user.email}, phone=${user.phone}, type=${user.type}, pass=${user.password}")
            }
            val cars = carDao.getAll()
            cars.forEach { car ->
                Log.d("DB_STATE", "Car: id=${car.id}, ownerUserId=${car.ownerUserId}, name=${car.name}, year=${car.year}, plate=${car.plate}, color=${car.color}, state=${car.state}")
            }
        }

        // MODIFIER: text specific atributes only relevant when np. text is present
        setContent {
            CarShareTheme {
                val context = LocalContext.current
                val db = remember { AppDB.getDatabase(context) }
                val coroutineScope = rememberCoroutineScope()

                // screen control
                var showLogin by remember { mutableStateOf(false) }
                // changes of its value aktualizuje w ui
                var showRegister by remember { mutableStateOf(false) }
                var showFindTrip by remember { mutableStateOf(false) }
                var showUserPanel by remember { mutableStateOf(false) }
                var showUserInfo by remember { mutableStateOf(false) }
                var showCarDetail by remember { mutableStateOf<Car?>(null) }
                var loggedIn by remember { mutableStateOf(UserSession.currentUserId != null) }

                var showTripView by remember { mutableStateOf(false) }

                // loggedin status update
                LaunchedEffect(Unit) {
                    loggedIn = UserSession.currentUserId != null
                }

                var showMenu by remember { mutableStateOf(false) }
                // icons https://developer.android.com/reference/kotlin/androidx/compose/material/icons/Icons.Filled
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.primary, // Or Color.Blue if you prefer
                            actions = {
                                // row of buttons
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
                                    // tracks (¬ subskrypcja) state in showmenu by remeber
                                    // from docs The function called when the user dismisses the dialog, such as by tapping outside of it.
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
                                                    showRegister = false; showFindTrip = false; showUserPanel = false;
                                                    showUserInfo = false; showCarDetail = null; showTripView = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Register") },
                                                onClick = {
                                                    showMenu = false
                                                    showRegister = true
                                                    showLogin = false; showFindTrip = false; showUserPanel = false;
                                                    showUserInfo = false; showCarDetail = null; showTripView = false
                                                }
                                            )
                                        }

                                        if (loggedIn) {
                                            DropdownMenuItem(
                                                text = { Text("cars") },
                                                onClick = {
                                                    showMenu = false
                                                    showUserPanel = true
                                                    showLogin = false; showRegister = false; showFindTrip = false;
                                                    showUserInfo = false; showCarDetail = null; showTripView = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("my profile") },
                                                onClick = {
                                                    showMenu = false
                                                    showUserInfo = true
                                                    showLogin = false; showRegister = false; showFindTrip = false;
                                                    showUserPanel = false; showCarDetail = null; showTripView = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("logout") },
                                                onClick = {
                                                    showMenu = false
                                                    coroutineScope.launch {
                                                        UserSession.currentUserId = null
                                                        loggedIn = false
                                                        showLogin = true // login? todo redirect to findscreen not login

                                                        showUserPanel = false; showFindTrip = false; showRegister = false;
                                                        showUserInfo = false; showCarDetail = null; showTripView = false
                                                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    // "+" -- w ogole change findtrip to post trip cause
                                    IconButton(onClick = {
                                        if (loggedIn) {
                                            showFindTrip = true
                                            showLogin = false; showRegister = false; showUserPanel = false;
                                            showUserInfo = false; showCarDetail = null; showTripView = false
                                        } else {
                                            showLogin = true
                                            showFindTrip = false; showRegister = false; showUserPanel = false;
                                            showUserInfo = false; showCarDetail = null; showTripView = false
                                            Toast.makeText(context, "Please log in first.", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Text("+")
                                    }

                                    IconButton(onClick = {
                                        if (loggedIn) {
                                            showTripView = true
                                            showLogin = false; showRegister = false; showFindTrip = false;
                                            showUserPanel = false; showUserInfo = false; showCarDetail = null
                                        } else {
                                            showLogin = true
                                            showTripView = false; showRegister = false; showFindTrip = false;
                                            showUserPanel = false; showUserInfo = false; showCarDetail = null
                                            Toast.makeText(context, "Please log in first.", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Menu,
                                            contentDescription = ""
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    floatingActionButton = {} // maybe transfer fab logic to findtripscreen?
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when {
                            showLogin -> LoginScreen(
                                onLoginSuccess = {
                                    loggedIn = true
                                    showLogin = false
                                    showFindTrip = true
                                    showRegister = false; showUserPanel = false; showUserInfo = false; showCarDetail = null; showTripView = false
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
                                    showFindTrip = true
                                    showLogin = false; showUserPanel = false; showUserInfo = false; showCarDetail = null; showTripView = false // Ensure showTripView is false
                                },
                                onNavigateToLogin = {
                                    showRegister = false
                                    showLogin = true
                                }
                            )
                            showFindTrip -> FindTripScreen(onBack = {
                                showFindTrip = false
                            })
                            showUserPanel -> UserPanel(
                                db = db,
                                onBack = { showUserPanel = false }
                            )
                            showUserInfo -> UserInfo(
                                db = db,
                                onBack = { showUserInfo = false }
                            )
                            showCarDetail != null -> CarInfo(
                                car = showCarDetail!!,
                                onBack = { showCarDetail = null }
                            )
                            showTripView -> TripViewScreen(onBack = {
                                showTripView = false
                            })
                            else -> {
                                // Default-> find trip screen, a post trip logic w "+"
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Welcome", style = MaterialTheme.typography.headlineMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}