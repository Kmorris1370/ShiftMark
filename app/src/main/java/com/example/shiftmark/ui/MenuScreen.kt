package com.example.shiftmark.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftmark.SecureStorage
import com.example.shiftmark.TimestampViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: TimestampViewModel,
    onBack: () -> Unit,
    onManual: () -> Unit,
    onDataDeleted: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Menu", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1C)
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShiftMarkRed
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuButton(text = "Manual", onClick = onManual)
            MenuButton(
                text = "Delete Data",
                onClick = { showDeleteDialog = true },
                color = Color(0xFF3A0000)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Delete All Data", color = Color.White) },
            text = {
                Text(
                    "All timestamps will be deleted and your entry PIN will be reset.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    SecureStorage.clearPin(context)
                    showDeleteDialog = false
                    onDataDeleted()
                }) {
                    Text("Yes", color = ShiftMarkRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    color: Color = Color(0xFF1E1E1E)
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text, color = Color.White, fontSize = 15.sp)
    }
}
