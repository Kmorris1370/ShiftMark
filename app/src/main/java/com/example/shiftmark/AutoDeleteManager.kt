package com.example.shiftmark

import android.content.Context
import android.content.SharedPreferences
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Session-based auto-delete:
 * - The 24-hour countdown starts when the first timestamp of a session is added.
 * - The session ends when the user clears all timestamps OR when the worker fires
 *   and wipes the list automatically.
 */
object AutoDeleteManager {
    private const val PREFS_FILE = "shiftmark_session"
    private const val KEY_SESSION_START_MS = "session_start_ms"
    private const val WORK_NAME = "auto_delete"
    private const val SESSION_DURATION_MS = 24L * 60L * 60L * 1000L

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    /** Called when a timestamp is added. Starts the timer if not already running. */
    @Synchronized
    fun onTimestampAdded(context: Context) {
        if (prefs(context).getLong(KEY_SESSION_START_MS, 0L) != 0L) return
        val startMs = System.currentTimeMillis()
        prefs(context).edit().putLong(KEY_SESSION_START_MS, startMs).apply()
        scheduleWork(context, SESSION_DURATION_MS)
    }

    /** Called when the user manually clears all, OR when the worker fires. */
    @Synchronized
    fun endSession(context: Context) {
        prefs(context).edit().remove(KEY_SESSION_START_MS).apply()
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
    }

    /** Returns ms remaining in the current session, or 0 if no session. */
    fun timeRemainingMs(context: Context): Long {
        val start = prefs(context).getLong(KEY_SESSION_START_MS, 0L)
        if (start == 0L) return 0L
        val elapsed = System.currentTimeMillis() - start
        return (SESSION_DURATION_MS - elapsed).coerceAtLeast(0L)
    }

    fun hasActiveSession(context: Context): Boolean =
        prefs(context).getLong(KEY_SESSION_START_MS, 0L) != 0L

    private fun scheduleWork(context: Context, delayMs: Long) {
        val request = OneTimeWorkRequestBuilder<DeleteWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
