package com.example.carshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import kotlinx.coroutines.launch

enum class CarColor(val displayName: String) {
    BLACK("Black"),
    GREY("Grey"),
    WHITE("White"),
    GREEN("Green"),
    DARK_BLUE("Dark Blue"),
    DARK_BROWN("Dark Brown"),
    METALLIC("Metallic")
}
enum class CarModel(val displayName: String) {
    SEDAN("Sedan"),
    SUV("SUV"),
    CONVERTIBLE("Convertible"),
    PICKUP("Pickup"),
    SPORTS("Sports"),
}
enum class State(val displayName: String) {
    Poor("Poor"),
    Good("Good"),
}
enum class Producent(val displayName: String) {
    TOYOTA("Toyota"),
    HONDA("Honda"),
    FORD("Ford"),
    CHEVROLET("Chevrolet"),
    BMW("BMW"),
    MERCEDES("Mercedes"),
    AUDI("Audi"),
    HYUNDAI("Hyundai"),
    KIA("Kia"),
    NISSAN("Nissan")
}

@Composable
fun UserPanel(db: AppDB, onBack: () -> Unit) {
    var producentInput by remember { mutableStateOf("") }
    var selectedProducent by remember { mutableStateOf<Producent?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cars = remember { mutableStateListOf<Car>() }

    var selectedModel by remember { mutableStateOf(CarModel.SEDAN) }
    var selectedColor by remember { mutableStateOf(CarColor.BLACK) }
    var selectedState by remember { mutableStateOf(State.Good) }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }


    var modelMenuExpanded by remember { mutableStateOf(false) }
    var colorMenuExpanded by remember { mutableStateOf(false) }
    var stateMenuExpanded by remember { mutableStateOf(false) }
    var producentMenuExpanded by remember { mutableStateOf(false) }

    var showForm by remember { mutableStateOf(false) } // form on/off on button click

    val userId = UserSession.currentUserId

    LaunchedEffect(userId) {
        if (userId != null) {
            cars.clear()
            cars.addAll(db.carDao().getCarsForUser(userId))
            // tez teoretycznie type mismatch issue dlatego instrukcja warunkowa
        } else {
            cars.clear()
            // nie wystapi
            // Toast.makeText(context, "log in to see and manage cars.", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Text("Manage Your Cars", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showForm) {

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = producentInput,
                        onValueChange = {
                            producentInput = it
                            producentMenuExpanded = true
                            selectedProducent = null
                        },
                        label = { Text("Producent") },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = producentMenuExpanded && producentInput.isNotBlank(),
                        onDismissRequest = { producentMenuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Producent.values()
                            .filter { it.displayName.contains(producentInput, ignoreCase = true) }
                            .forEach { producent ->
                                DropdownMenuItem(
                                    text = { Text(producent.displayName) },
                                    onClick = {
                                        selectedProducent = producent
                                        producentInput = producent.displayName
                                        producentMenuExpanded = false
                                    }
                                )
                            }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item { // Model Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { modelMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Model: ${selectedModel.displayName}")
                    }
                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        CarModel.values().forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model.displayName) },
                                onClick = {
                                    selectedModel = model
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(50)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { // Color Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { colorMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Color: ${selectedColor.displayName}")
                    }
                    DropdownMenu(
                        expanded = colorMenuExpanded,
                        onDismissRequest = { colorMenuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        CarColor.values().forEach { color ->
                            DropdownMenuItem(
                                text = { Text(color.displayName) },
                                onClick = {
                                    selectedColor = color
                                    colorMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { stateMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("State: ${selectedState.displayName}")
                    }
                    DropdownMenu(
                        expanded = stateMenuExpanded,
                        onDismissRequest = { stateMenuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        State.values().forEach { state ->
                            DropdownMenuItem(
                                text = { Text(state.displayName) },
                                onClick = {
                                    selectedState = state
                                    stateMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("License Plate") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item { // add/confirm car
            Button(
                onClick = {
                    // val name = "{producentInput.trim()} + {selectedModel.displayName}" //?
                    if (
                        showForm &&
                        producentInput.isNotBlank() &&
                        selectedProducent != null &&
                        year.isNotBlank() &&
                        selectedColor != null &&
                        selectedState != null &&
                        plate.isNotBlank() &&
                        userId != null
                    ) {
                        val carYear = year.toIntOrNull()
                        if (carYear == null || carYear <= 1900 || carYear > 2025) {
                            Toast.makeText(context, "Please enter a valid year between 1901 and 2025.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            db.carDao().insert(
                                Car(
                                    name = "${producentInput.trim()} ${selectedModel.displayName}",
                                    // name = name,
                                    year = carYear,
                                    color = selectedColor.displayName,
                                    state = selectedState.displayName,
                                    plate = plate,
                                    ownerUserId = userId
                                )
                            )
                            cars.clear()
                            cars.addAll(db.carDao().getCarsForUser(userId))
                            // defaults
                            producentInput = ""
                            selectedProducent = null
                            selectedModel = CarModel.SEDAN
                            selectedColor = CarColor.BLACK
                            selectedState = State.Good
                            year = ""
                            plate = ""

                            Toast.makeText(context, "Car added successfully!", Toast.LENGTH_SHORT).show()
                            showForm = false
                        }
                    } else {
                        showForm = !showForm
                        if (showForm) {
                            producentInput = ""
                            selectedProducent = null
                            selectedModel = CarModel.SEDAN
                            selectedColor = CarColor.BLACK
                            selectedState = State.Good
                            year = ""
                            plate = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showForm) "Confirm" else "Add Car")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Your Cars:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (cars.isEmpty()) {
            item {
                Text("No cars added")
            }
        } else {
            items(cars) { car ->
                var showEditDialog by remember { mutableStateOf(false) }

                if (showEditDialog) {
                    var editName by remember { mutableStateOf(car.name) }
                    var editPlate by remember { mutableStateOf(car.plate) }
                    var editYear by remember { mutableStateOf(car.year.toString()) }
                    var editColor by remember { mutableStateOf(CarColor.valueOf(car.color.uppercase().replace(" ", "_"))) } //  string to enum
                    var editState by remember { mutableStateOf(State.valueOf(car.state)) }
                    var editColorMenuExpanded by remember { mutableStateOf(false) }
                    var editStateMenuExpanded by remember { mutableStateOf(false) }


                    AlertDialog(
                        onDismissRequest = { showEditDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val updatedCar = car.copy(
                                    name = editName,
                                    plate = editPlate,
                                    year = editYear.toIntOrNull() ?: car.year,
                                    color = editColor.displayName,
                                    state = editState.displayName
                                )
                                coroutineScope.launch {
                                    db.carDao().update(updatedCar)
                                    cars.clear()
                                    if (userId != null) {
                                        cars.addAll(db.carDao().getCarsForUser(userId))
                                    }
                                }
                                showEditDialog = false
                            }) {
                                Text("Update")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Edit Car") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    label = { Text("Name (Producent & Model)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editPlate,
                                    onValueChange = { editPlate = it },
                                    label = { Text("License Plate") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editYear,
                                    onValueChange = { editYear = it },
                                    label = { Text("Year") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { editColorMenuExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Color: ${editColor.displayName}")
                                    }
                                    DropdownMenu(
                                        expanded = editColorMenuExpanded,
                                        onDismissRequest = { editColorMenuExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        CarColor.values().forEach { color ->
                                            DropdownMenuItem(
                                                text = { Text(color.displayName) },
                                                onClick = {
                                                    editColor = color
                                                    editColorMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
//                                Card(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 4.dp)
//                                ) {
//                                    Column(modifier = Modifier.padding(16.dp)) {
//                                        Text("Name: ${car.name}")
//                                        Text("Plate: ${car.plate}")
//                                        Text("Year: ${car.year}")
//                                        Text("Color: ${car.color}")
//                                        Text("State: ${car.state}")
//
//                                        Spacer(modifier = Modifier.height(8.dp))
//                                        Row(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            horizontalArrangement = Arrangement.SpaceBetween
//                                        ) {
//                                            OutlinedButton(onClick = {
//                                                coroutineScope.launch {
//                                                    db.carDao().delete(car)
//                                                    cars.remove(car)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { editStateMenuExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("State: ${editState.displayName}")
                                    }
                                    DropdownMenu(
                                        expanded = editStateMenuExpanded,
                                        onDismissRequest = { editStateMenuExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        State.values().forEach { state ->
                                            DropdownMenuItem(
                                                text = { Text(state.displayName) },
                                                onClick = {
                                                    editState = state
                                                    editStateMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Name: ${car.name}")
                        Text("Plate: ${car.plate}")
                        Text("Year: ${car.year}")
                        Text("Color: ${car.color}")
                        Text("State: ${car.state}")

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = {
                                coroutineScope.launch {
                                    db.carDao().delete(car)
                                    cars.remove(car)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Car",
                                )
                            }

                            OutlinedButton(onClick = {
                                showEditDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Car",
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}