package com.example.shiftmark

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object TimestampRepository {

    private const val PREFS_FILE = "shiftmark_timestamps"
    private const val KEY_LIST = "timestamps_json"

    @Volatile private var cachedPrefs: SharedPreferences? = null

    private fun prefs(context: Context): SharedPreferences {
        return cachedPrefs ?: synchronized(this) {
            cachedPrefs ?: context.applicationContext
                .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                .also { cachedPrefs = it }
        }
    }

    @Synchronized
    fun getAll(context: Context): MutableList<Timestamp> {
        val raw = prefs(context).getString(KEY_LIST, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            MutableList(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Timestamp(
                    id = o.getString("id"),
                    time = o.getString("time"),
                    title = o.optString("title", ""),
                    notes = o.optString("notes", "")
                )
            }
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    @Synchronized
    private fun saveAll(context: Context, list: List<Timestamp>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("time", t.time)
                put("title", t.title)
                put("notes", t.notes)
            })
        }
        prefs(context).edit().putString(KEY_LIST, arr.toString()).apply()
    }

    @Synchronized
    fun addTimestamp(context: Context, time: String, title: String = ""): Timestamp {
        return addTimestampWithId(context, UUID.randomUUID().toString(), time, title)
    }

    @Synchronized
    fun addTimestampWithId(context: Context, id: String, time: String, title: String = ""): Timestamp {
        val t = Timestamp(id = id, time = time, title = title)
        val list = getAll(context)
        // De-duplicate: ignore re-deliveries of an already-known id.
        if (list.any { it.id == id }) return list.first { it.id == id }
        list.add(0, t)
        saveAll(context, list)
        AutoDeleteManager.onTimestampAdded(context)
        return t
    }

    @Synchronized
    fun getById(context: Context, id: String): Timestamp? =
        getAll(context).firstOrNull { it.id == id }

    @Synchronized
    fun update(context: Context, id: String, title: String, notes: String) {
        val list = getAll(context)
        val i = list.indexOfFirst { it.id == id }
        if (i != -1) {
            list[i] = list[i].copy(title = title, notes = notes)
            saveAll(context, list)
        }
    }

    @Synchronized
    fun delete(context: Context, id: String) {
        val list = getAll(context)
        if (list.removeAll { it.id == id }) saveAll(context, list)
    }

    @Synchronized
    fun insert(context: Context, timestamp: Timestamp, atTop: Boolean = true) {
        val list = getAll(context)
        if (atTop) list.add(0, timestamp) else list.add(timestamp)
        saveAll(context, list)
    }

    @Synchronized
    fun clearAll(context: Context) {
        saveAll(context, emptyList())
        AutoDeleteManager.endSession(context)
    }
}
