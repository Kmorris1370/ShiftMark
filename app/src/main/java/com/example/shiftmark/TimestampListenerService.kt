package com.example.shiftmark

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class TimestampListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("ShiftMark", "TimestampListenerService created")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("ShiftMark", "recv: path=${messageEvent.path} from=${messageEvent.sourceNodeId}")
        val raw = String(messageEvent.data)

        when (messageEvent.path) {
            Constants.TIMESTAMP_PATH -> handleCreate(raw)
            Constants.TIMESTAMP_UPDATE_PATH -> handleUpdate(raw)
            Constants.TIMESTAMP_DELETE_PATH -> handleDelete(raw)
            else -> Log.w("ShiftMark", "recv: unknown path ${messageEvent.path}")
        }
    }

    /** Payload: id|time|title  (title may be empty; legacy format time|title also accepted). */
    private fun handleCreate(raw: String) {
        val parts = raw.split("|")
        val (id, time, title) = when (parts.size) {
            3 -> Triple(parts[0], parts[1], parts[2])
            2 -> Triple(java.util.UUID.randomUUID().toString(), parts[0], parts[1])
            1 -> Triple(java.util.UUID.randomUUID().toString(), parts[0], "")
            else -> {
                Log.w("ShiftMark", "recv: malformed create payload '$raw'")
                return
            }
        }
        TimestampRepository.addTimestampWithId(applicationContext, id, time, title)
        Log.d("ShiftMark", "recv: created id=$id time=$time title='$title'")
        broadcastChange()
    }

    /** Payload: id|title */
    private fun handleUpdate(raw: String) {
        val parts = raw.split("|", limit = 2)
        if (parts.size != 2) {
            Log.w("ShiftMark", "recv: malformed update payload '$raw'")
            return
        }
        val (id, title) = parts
        val existing = TimestampRepository.getById(applicationContext, id)
        if (existing == null) {
            Log.w("ShiftMark", "recv: update for unknown id=$id")
            return
        }
        TimestampRepository.update(applicationContext, id, title, existing.notes)
        Log.d("ShiftMark", "recv: updated id=$id title='$title'")
        broadcastChange()
    }

    /** Payload: id */
    private fun handleDelete(raw: String) {
        val id = raw.trim()
        if (id.isEmpty()) return
        TimestampRepository.delete(applicationContext, id)
        Log.d("ShiftMark", "recv: deleted id=$id")
        broadcastChange()
    }

    private fun broadcastChange() {
        val intent = Intent("NEW_TIMESTAMP").setPackage(packageName)
        sendBroadcast(intent)
    }
}
