package com.example.carshare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import kotlinx.coroutines.launch


@Composable
// keyboard types https://developer.android.com/reference/kotlin/androidx/compose/ui/text/input/KeyboardType
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    val context = LocalContext.current // Toast and db
    val db = remember { AppDB.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val isEmailValid by remember(email) { mutableStateOf(email.contains("@")) }
    val isPasswordValid by remember(pass) { mutableStateOf(pass.isNotBlank()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    // when types keep tekst validation
                    if (error != null && it.isNotBlank()) error = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = !isEmailValid && email.isNotBlank(),
                supportingText = {
                    if (!isEmailValid && email.isNotBlank()) {
                        Text("not valid email")
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            OutlinedTextField(
                value = pass,
                onValueChange = {
                    pass = it
                    if (error != null && it.isNotBlank()) error = null
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            OutlinedButton(
                onClick = {
                    if (email.isBlank() || pass.isBlank()) {
                        error = "Blank fields"
                        return@OutlinedButton
                    }
                    if (!isEmailValid) {
                        error = "Not valid email adress"
                        return@OutlinedButton
                    }
                    coroutineScope.launch {
                        val user = db.userDao().login(email, pass)
                        if (user != null) {
                            UserSession.currentUserId = user.id
                            error = null
                            onLoginSuccess()
                        } else {
                            error = "Invalid data"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register here.")
            }
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}