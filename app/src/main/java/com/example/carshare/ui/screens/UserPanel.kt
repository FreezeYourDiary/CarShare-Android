package com.example.carshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carshare.session.UserSession
import data.database.AppDB
import data.entities.Car
import kotlinx.coroutines.launch

@Composable
fun UserPanel(db: AppDB, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cars = remember { mutableStateListOf<Car>() }

    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }

    var showForm by remember { mutableStateOf(false) } // form on/off on button click

    val userId = UserSession.currentUserId

    LaunchedEffect(userId) {
        if (userId != null) {
            cars.clear()
            cars.addAll(db.carDao().getCarsForUser(userId))
            // tez teoretycznie type mismatch issue dlatego instrukcja warunkowa
        }
//        else {
//            cars.clear()
//            Toast.makeText(context, "Please log in to see and manage your cars.", Toast.LENGTH_SHORT).show()
//        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Manage Your Cars", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (showForm && name.isNotBlank() && year.isNotBlank() && color.isNotBlank() && state.isNotBlank() && plate.isNotBlank() && userId != null) {
                    val carYear = year.toIntOrNull()
                    if (carYear == null || carYear <= 0) {
                        Toast.makeText(context, "Please enter a valid year.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        db.carDao().insert(
                            Car(
                                name = name,
                                year = carYear,
                                color = color,
                                state = state,
                                plate = plate,
                                ownerUserId = userId
                            )
                        )
                        cars.clear()
                        cars.addAll(db.carDao().getCarsForUser(userId))

                        name = ""
                        year = ""
                        color = ""
                        state = ""
                        plate = ""

                        Toast.makeText(context, "Car added successfully!", Toast.LENGTH_SHORT).show()
                        showForm = false
                    }
                } else {
                    showForm = !showForm
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (showForm) "Confirm" else "Add Car")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showForm) {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Car Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("License Plate") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Text("Your Cars:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (cars.isEmpty()) {
            Text("No cars added")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cars) { car ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
//                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Name: ${car.name}")
                            Text("Plate: ${car.plate}")
                            Text("Year: ${car.year}")
                            Text("Color: ${car.color}")
                            Text("State: ${car.state}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}