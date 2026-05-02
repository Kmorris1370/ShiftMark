package com.example.shiftmark

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

class TimestampViewModel(app: Application) : AndroidViewModel(app) {
    val timestamps = mutableStateListOf<Timestamp>()
    private val deletedTimestamps = mutableListOf<Timestamp>()

    init {
        timestamps.addAll(TimestampRepository.getAll(getApplication()))
    }

    fun refreshFromRepo() {
        val fresh = TimestampRepository.getAll(getApplication())
        timestamps.clear()
        timestamps.addAll(fresh)
    }

    fun addTimestamp(time: String) {
        val t = TimestampRepository.addTimestamp(getApplication(), time)
        timestamps.add(0, t)
    }

    fun addManualTimestamp(time: String, title: String, notes: String) {
        val t = TimestampRepository.addTimestamp(getApplication(), time, title)
        if (notes.isNotBlank()) {
            TimestampRepository.update(getApplication(), t.id, title, notes)
        }
        refreshFromRepo()
    }

    fun deleteTimestamp(timestamp: Timestamp) {
        TimestampRepository.delete(getApplication(), timestamp.id)
        deletedTimestamps.add(timestamp)
        timestamps.remove(timestamp)
    }

    fun undoDelete() {
        if (deletedTimestamps.isNotEmpty()) {
            val last = deletedTimestamps.removeLast()
            TimestampRepository.insert(getApplication(), last, atTop = true)
            timestamps.add(0, last)
        }
    }

    fun updateTimestamp(id: String, title: String, notes: String) {
        TimestampRepository.update(getApplication(), id, title, notes)
        val index = timestamps.indexOfFirst { it.id == id }
        if (index != -1) {
            timestamps[index] = timestamps[index].copy(title = title, notes = notes)
        }
    }

    fun clearAll() {
        deletedTimestamps.addAll(timestamps)
        TimestampRepository.clearAll(getApplication())
        timestamps.clear()
    }
}
