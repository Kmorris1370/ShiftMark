package com.example.shiftmark

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters

class DeleteWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // clearAll() also calls AutoDeleteManager.endSession() — single source of truth.
        TimestampRepository.clearAll(applicationContext)
        val intent = Intent("DELETE_ALL_TIMESTAMPS").setPackage(applicationContext.packageName)
        applicationContext.sendBroadcast(intent)
        return Result.success()
    }
}
