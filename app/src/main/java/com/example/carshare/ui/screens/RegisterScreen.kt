package com.example.carshare.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import data.entities.User
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDB.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("passenger") }
    var isDriver by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val isEmailValid by remember(email) { mutableStateOf(email.contains("@")) }
    // ^\d{9,}$ -- starts with  9 digits
    val isPhoneValid by remember(phone) { mutableStateOf(phone.matches(Regex("^\\d{9,}$"))) }
    val isNameValid by remember(name) { mutableStateOf(name.isNotBlank()) }
    val isPasswordValid by remember(password) { mutableStateOf(password.isNotBlank()) }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (error != null && it.isNotBlank()) error = null
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = !isNameValid && name.isNotBlank()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        // val emailHasError = !isEmailValid && email.isNotBlank()
        item {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (error != null && it.isNotBlank()) error = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = !isEmailValid && email.isNotBlank(),
                supportingText = {
                   // if (emailHasError) {
                        if (!isEmailValid && email.isNotBlank()) {
                            Text("Email must contain '@'")
                        }
                    }
                //}
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    if (error != null && it.isNotBlank()) error = null
                },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = !isPhoneValid && phone.isNotBlank(),
                supportingText = {
                    if (!isPhoneValid && phone.isNotBlank()) {
                        Text("Phone must be at least 9 digits, no spaces or special characters.")
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (error != null && it.isNotBlank()) error = null
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = !isPasswordValid && password.isNotBlank()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = isDriver,
                        onCheckedChange = {
                            isDriver = it
                            userType = if (it) "driver" else "passenger"
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("I am also a driver")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // val allFieldsFilled = listOf(name, email, phone, password).all { it.isNotBlank() }
            Button(
                // if (!allFieldsFilled) {
                    //     error = "All fields required."
                    //     return@Button
                    // }
                onClick = {
                    if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                        error = "All fields required."
                        return@Button
                    }
                    if (!isEmailValid) {
                        error = "Not valid email."
                        return@Button
                    }
                    if (!isPhoneValid) {
                        error = "Please enter a valid phone number (9 digits no characters)."
                        return@Button
                    }
                    coroutineScope.launch {
                        val existingUser = db.userDao().findByEmail(email)
                        if (existingUser != null) {
                            error = "User with this email already exists."
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        } else {
                            val newUser = User(
                                name = name,
                                email = email,
                                phone = phone,
                                password = password,
                                type = userType
                            )
                            val userId = db.userDao().insert(newUser).toInt()
                            UserSession.currentUserId = userId
                            error = null
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            // Log.v("+user", "${newUser}")
                            onRegisterSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login here.")
            }
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}