package com.example.shiftmark.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    // Hardcoded PIN for now — Section 5 will replace this with secure storage
    val correctPin = "1234"

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(text = "ShiftMark", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.Companion.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("Enter PIN") },
            visualTransformation = PasswordVisualTransformation(),
            isError = error
        )

        if (error) {
            Text("Incorrect PIN", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.Companion.height(16.dp))

        Button(onClick = {
            if (pin == correctPin) {
                onLoginSuccess()
            } else {
                error = true
            }
        }) {
            Text("Login")
        }
    }
}