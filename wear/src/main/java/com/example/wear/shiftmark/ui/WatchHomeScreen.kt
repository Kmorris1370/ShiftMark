package com.example.shiftmark.wear.ui

import android.app.Activity
import android.app.RemoteInput
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import androidx.wear.input.RemoteInputIntentHelper
import com.example.shiftmark.Constants
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

val WatchRed = Color(0xFF8B0000)
private const val REMOTE_INPUT_KEY = "title"

private data class WatchEntry(
    val id: String = UUID.randomUUID().toString(),
    val time: String,
    var title: String
)

private enum class VoiceTarget { NEW, EDIT }

@Composable
fun WatchHomeScreen() {
    val context = LocalContext.current
    val timestamps = remember { mutableStateListOf<WatchEntry>() }
    var selectedIndex by remember { mutableStateOf(-1) }
    var voiceTarget by remember { mutableStateOf(VoiceTarget.NEW) }
    var editIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // Try RemoteInput first (came from picker fallback), then RecognizerIntent.
            val fromPicker = RemoteInput.getResultsFromIntent(result.data)
                ?.getCharSequence(REMOTE_INPUT_KEY)?.toString().orEmpty()
            val fromSpeech = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull().orEmpty()
            val spoken = fromSpeech.ifBlank { fromPicker }
            android.util.Log.d("ShiftMark", "voice: result='$spoken' (speech=$fromSpeech picker=$fromPicker)")
            when (voiceTarget) {
                VoiceTarget.NEW -> if (spoken.isNotBlank()) {
                    addTimestamp(context, timestamps, spoken)
                }
                VoiceTarget.EDIT -> if (spoken.isNotBlank() && editIndex in timestamps.indices) {
                    val updated = timestamps[editIndex].copy(title = spoken)
                    timestamps[editIndex] = updated
                    sendTitleUpdateToPhone(context, updated.id, updated.title)
                    editIndex = -1
                }
            }
        }
    }

    fun launchVoice(target: VoiceTarget, prompt: String) {
        voiceTarget = target
        // 1) Try the speech recognizer Activity directly — that opens straight to voice.
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            voiceLauncher.launch(speechIntent)
            return
        } catch (_: ActivityNotFoundException) {
            android.util.Log.w("ShiftMark", "voice: no speech recognizer; falling back to picker")
        } catch (e: Exception) {
            android.util.Log.e("ShiftMark", "voice: speech intent failed; falling back", e)
        }

        // 2) Fallback to Wear's input picker (keyboard + Samsung Keyboard mic on Galaxy Watch).
        val remoteInputs = listOf(
            RemoteInput.Builder(REMOTE_INPUT_KEY).setLabel(prompt).build()
        )
        val pickerIntent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        RemoteInputIntentHelper.putRemoteInputsExtra(pickerIntent, remoteInputs)
        try {
            voiceLauncher.launch(pickerIntent)
        } catch (e: Exception) {
            android.util.Log.e("ShiftMark", "voice: picker also failed", e)
        }
    }

    Scaffold(timeText = { TimeText() }) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 32.dp, bottom = 16.dp)
            ) {
                // Header: Mark button + last mark info
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(WatchRed)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { addTimestamp(context, timestamps, "") },
                                        onLongPress = { launchVoice(VoiceTarget.NEW, "Title") }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Mark", color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap = mark · Hold = speak",
                            fontSize = 9.sp,
                            color = Color.DarkGray
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = timestamps.firstOrNull()?.let { formatRow(it) } ?: "no marks yet",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(10.dp))
                        if (timestamps.isNotEmpty()) {
                            Text("— history —", fontSize = 9.sp, color = Color.DarkGray)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                itemsIndexed(timestamps) { index, entry ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .pointerInput(entry.id) {
                                detectTapGestures(onTap = { selectedIndex = index })
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = formatRow(entry),
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            if (listState.firstVisibleItemIndex > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                scope.launch { listState.animateScrollToItem(0) }
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("^", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }

    if (selectedIndex in timestamps.indices) {
        val entry = timestamps[selectedIndex]
        EntryActionDialog(
            onDismiss = { selectedIndex = -1 },
            title = { Text(entry.time, color = Color.White, fontSize = 14.sp) },
            content = {
                Text(
                    text = entry.title.ifBlank { "(no title)" },
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactChip(
                    onClick = {
                        editIndex = selectedIndex
                        selectedIndex = -1
                        launchVoice(VoiceTarget.EDIT, "New title")
                    },
                    label = { Text("Edit", fontSize = 10.sp) },
                    colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xFF2A2A2A))
                )
                CompactChip(
                    onClick = {
                        val removed = timestamps.removeAt(selectedIndex)
                        sendDeleteToPhone(context, removed.id)
                        selectedIndex = -1
                    },
                    label = { Text("Delete", fontSize = 10.sp) },
                    colors = ChipDefaults.primaryChipColors(backgroundColor = WatchRed)
                )
            }
        }
    }
}

private fun addTimestamp(
    context: Context,
    timestamps: MutableList<WatchEntry>,
    title: String
) {
    val entry = WatchEntry(time = currentTimeString(), title = title)
    timestamps.add(0, entry)
    vibrate(context)
    sendCreateToPhone(context, entry.id, entry.time, entry.title)
}

private fun currentTimeString(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

private fun formatRow(entry: WatchEntry): String =
    if (entry.title.isBlank()) entry.time else "${entry.time}: ${entry.title}"

@Composable
private fun EntryActionDialog(
    onDismiss: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    actions: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E1E))
                .padding(12.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = { /* swallow */ }) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            title()
            Spacer(Modifier.height(6.dp))
            content()
            Spacer(Modifier.height(10.dp))
            actions()
        }
    }
}

fun vibrate(context: Context) {
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vm = context.getSystemService(android.os.VibratorManager::class.java)
        vm.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
    }
    vibrator.vibrate(
        android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
    )
}

private fun sendCreateToPhone(context: Context, id: String, time: String, title: String) {
    sendToPhone(context, Constants.TIMESTAMP_PATH, "$id|$time|$title")
}

private fun sendTitleUpdateToPhone(context: Context, id: String, title: String) {
    sendToPhone(context, Constants.TIMESTAMP_UPDATE_PATH, "$id|$title")
}

private fun sendDeleteToPhone(context: Context, id: String) {
    sendToPhone(context, Constants.TIMESTAMP_DELETE_PATH, id)
}

private fun sendToPhone(context: Context, path: String, payload: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isEmpty()) {
                android.util.Log.w("ShiftMark", "send[$path]: no connected nodes")
                return@launch
            }
            nodes.forEach { node ->
                val msgId = Wearable.getMessageClient(context)
                    .sendMessage(node.id, path, payload.toByteArray())
                    .await()
                android.util.Log.d(
                    "ShiftMark",
                    "send[$path]: ${node.displayName}, payload='$payload', msgId=$msgId"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ShiftMark", "send[$path] failed", e)
        }
    }
}
