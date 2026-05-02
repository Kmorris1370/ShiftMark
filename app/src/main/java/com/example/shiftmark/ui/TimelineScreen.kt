package com.example.shiftmark.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiftmark.AutoDeleteManager
import com.example.shiftmark.Timestamp
import com.example.shiftmark.TimestampViewModel
import kotlinx.coroutines.delay

private enum class FilterMode(val label: String) {
    ALL("All"),
    HAS_TITLE("Has title"),
    HAS_NOTES("Has notes"),
    EMPTY("No title or notes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimestampViewModel,
    onOpenMenu: () -> Unit,
    onOpenTimestamp: (String) -> Unit,
    onAddManual: () -> Unit
) {
    val context = LocalContext.current
    var recentlyDeleted by remember { mutableStateOf<Timestamp?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var remainingMs by remember { mutableStateOf(AutoDeleteManager.timeRemainingMs(context)) }

    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var filterMenuOpen by remember { mutableStateOf(false) }
    var filterMode by remember { mutableStateOf(FilterMode.ALL) }

    LaunchedEffect(viewModel.timestamps.size) {
        while (true) {
            remainingMs = AutoDeleteManager.timeRemainingMs(context)
            delay(1000L)
        }
    }

    DisposableEffect(Unit) {
        val timestampReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.refreshFromRepo()
            }
        }
        val deleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.refreshFromRepo()
            }
        }
        context.registerReceiver(timestampReceiver, IntentFilter("NEW_TIMESTAMP"), Context.RECEIVER_NOT_EXPORTED)
        context.registerReceiver(deleteReceiver, IntentFilter("DELETE_ALL_TIMESTAMPS"), Context.RECEIVER_NOT_EXPORTED)
        onDispose {
            context.unregisterReceiver(timestampReceiver)
            context.unregisterReceiver(deleteReceiver)
        }
    }

    val visible = remember(viewModel.timestamps.toList(), query, filterMode) {
        viewModel.timestamps.filter { t ->
            val q = query.trim()
            val matchesQuery = q.isEmpty() ||
                t.title.contains(q, ignoreCase = true) ||
                t.notes.contains(q, ignoreCase = true)
            val matchesFilter = when (filterMode) {
                FilterMode.ALL -> true
                FilterMode.HAS_TITLE -> t.title.isNotBlank()
                FilterMode.HAS_NOTES -> t.notes.isNotBlank()
                FilterMode.EMPTY -> t.title.isBlank() && t.notes.isBlank()
            }
            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddManual,
                containerColor = ShiftMarkRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add manual timestamp")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search title or notes", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShiftMarkRed,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = ShiftMarkRed
                            )
                        )
                    } else {
                        Column {
                            Text("Time Until Autodelete", color = Color.White, fontSize = 13.sp)
                            Text(
                                text = if (AutoDeleteManager.hasActiveSession(context))
                                    formatHms(remainingMs)
                                else
                                    "—",
                                color = ShiftMarkRed,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1C)
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (searchOpen) {
                            // Closing search clears the query so the full list reappears.
                            query = ""
                            searchOpen = false
                        } else {
                            searchOpen = true
                        }
                    }) {
                        Icon(
                            if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchOpen) "Close search" else "Search",
                            tint = Color.White
                        )
                    }
                    Box {
                        IconButton(onClick = { filterMenuOpen = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Filter",
                                tint = if (filterMode != FilterMode.ALL) ShiftMarkRed else Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = filterMenuOpen,
                            onDismissRequest = { filterMenuOpen = false },
                            modifier = Modifier.background(Color(0xFF1E1E1E))
                        ) {
                            FilterMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            mode.label,
                                            color = if (mode == filterMode) ShiftMarkRed else Color.White
                                        )
                                    },
                                    onClick = {
                                        filterMode = mode
                                        filterMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (viewModel.timestamps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No timestamps yet.\nPress MARK on your watch\nor tap + to add one.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else if (visible.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No timestamps match your search/filter.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn {
                    items(visible, key = { it.id }) { timestamp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onOpenTimestamp(timestamp.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E1E1E)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = timestamp.time,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    modifier = Modifier.width(60.dp)
                                )

                                OutlinedTextField(
                                    value = timestamp.title,
                                    onValueChange = {
                                        viewModel.updateTimestamp(timestamp.id, it, timestamp.notes)
                                    },
                                    placeholder = { Text("title", color = Color.Gray, fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ShiftMarkRed,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true
                                )

                                IconButton(onClick = {
                                    recentlyDeleted = timestamp
                                    viewModel.deleteTimestamp(timestamp)
                                }) {
                                    Text("✕", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(recentlyDeleted) {
                if (recentlyDeleted != null) {
                    val result = snackbarHostState.showSnackbar(
                        message = "Timestamp deleted",
                        actionLabel = "Undo"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                    recentlyDeleted = null
                }
            }
        }
    }
}

private fun formatHms(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return "%d:%02d:%02d".format(h, m, s)
}
