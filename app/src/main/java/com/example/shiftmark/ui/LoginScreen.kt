package com.example.shiftmark.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftmark.SecureStorage

val DarkBackground = Color(0xFF121212)
val ShiftMarkRed = Color(0xFF8B0000)
val LightRed = Color(0xFFCF6679)

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ShiftMark", fontSize = 32.sp, color = ShiftMarkRed)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Nurse Shift Logger", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            PinInfoIcon()
        }

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("Enter PIN", color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            isError = error,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ShiftMarkRed,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = ShiftMarkRed,
                errorBorderColor = LightRed
            )
        )

        if (error) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Incorrect PIN", color = LightRed, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (SecureStorage.verifyPin(context, pin)) {
                    onLoginSuccess()
                } else {
                    error = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ShiftMarkRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login", color = Color.White, fontSize = 16.sp)
        }
    }
}