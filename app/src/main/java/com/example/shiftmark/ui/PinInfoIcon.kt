package com.example.shiftmark.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private const val PIN_INFO_TEXT =
    "This will be your PIN for this session. " +
        "It will reset once all timestamps have been deleted."

@Composable
fun PinInfoIcon(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Gray)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("PIN Info", color = Color.White) },
            text = { Text(PIN_INFO_TEXT, color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", color = ShiftMarkRed)
                }
            }
        )
    }
}
