package com.example.shiftmark.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class FaqEntry(val question: String, val answer: String)

private val FAQ = listOf(
    FaqEntry(
        "How do I record a timestamp?",
        "Tap the red Mark button on your watch. To add a title at the moment of recording, " +
            "long-press Mark and speak the title. You can also add a manual timestamp from the " +
            "phone using the + button on the main screen."
    ),
    FaqEntry(
        "What does Auto-delete do?",
        "Once you record your first timestamp, a 24-hour countdown begins. When it ends, " +
            "every timestamp is wiped automatically. The countdown is shown at the top of the " +
            "main screen."
    ),
    FaqEntry(
        "When does my session end?",
        "A session ends in two ways: the auto-delete fires after 24 hours, or you tap " +
            "Delete Data in the menu. Either way the timer is reset and your PIN is cleared, " +
            "ready for the next session."
    ),
    FaqEntry(
        "Why does my PIN reset?",
        "The PIN is per-session. It exists only to protect this shift's notes from a quick " +
            "glance — not to secure long-term records. When the session ends the PIN is cleared " +
            "and you set a new one for the next shift."
    ),
    FaqEntry(
        "Can I edit a timestamp after the fact?",
        "Yes. Tap any row on the main screen to open the detail view. You can edit the title, " +
            "edit notes, or delete the timestamp."
    ),
    FaqEntry(
        "What's the difference between title and notes?",
        "The title is a short label shown directly on the timeline (e.g. \"pushed med\"). " +
            "Notes are longer free-form text shown only inside the detail view."
    ),
    FaqEntry(
        "How do search and filter work?",
        "Search filters the list to entries whose title or notes contain your query. " +
            "Filter narrows by category (all entries, only entries with a title, only entries " +
            "with notes, or only entries that have neither)."
    ),
    FaqEntry(
        "What if my watch and phone disagree on time?",
        "Each device records using its own clock at the moment you press Mark. If your watch " +
            "clock is off, that timestamp will reflect the watch's time, not the phone's."
    ),
    FaqEntry(
        "Will the app sync to a server?",
        "No. All data is stored locally on the phone. Nothing is uploaded."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Manual & FAQ", color = Color.White) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            FAQ.forEach { entry ->
                FaqAccordionItem(entry)
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FaqAccordionItem(entry: FaqEntry) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.question,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = ShiftMarkRed
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = entry.answer,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
