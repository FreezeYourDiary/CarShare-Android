package com.example.carshare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
    val context = LocalContext.current     //  Toast and db
    val db = remember { AppDB.getDatabase(context) }
    // db calls
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize() // Fills the maximum available size
            .padding(24.dp), // Adds 16dp padding around the column
        horizontalAlignment = Alignment.CenterHorizontally, // Centers content horizontally
        verticalArrangement = Arrangement.Center // Centers content vertically
    ) {
        Text("Login") // mozna apply style
        Spacer(modifier = Modifier.height(20.dp))
//        if (!email.contains("@")) {
//            error = "missing "@"."
//            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
//            return@Button
//        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email) // email same as text?
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            // + password on/off visibility
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                if (email.isBlank() || pass.isBlank()) {
                    error = "Email an password are required"
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    return@OutlinedButton // early exit
                }
                coroutineScope.launch {
                    val user = db.userDao().login(email, pass)
                    if (user != null) {
                        // userid remember ewentualnie ? check with https://stackoverflow.com/questions/73115009/when-i-change-viewmodel-var-composable-doesnt-update-in-kotlin-compose???
                        UserSession.currentUserId = user.id
                        error = null
                        onLoginSuccess()
                    } else {
                        error = "Invalid email or password"
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register here.")
        }
//        error?.let {
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(it, color = MaterialTheme.colorScheme.error)
//        }
    }
}