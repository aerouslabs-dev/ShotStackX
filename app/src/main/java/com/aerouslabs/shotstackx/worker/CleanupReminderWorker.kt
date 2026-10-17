package com.aerouslabs.shotstackx.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aerouslabs.shotstackx.data.db.AppDatabase
import com.aerouslabs.shotstackx.data.repository.ScreenshotRepository
import com.aerouslabs.shotstackx.notifications.ScreenshotNotificationManager

/**
 * Fires ~30 minutes before the scheduled cleanup to give the user a heads-up.
 * It only notifies when there are archived screenshots actually pending deletion,
 * mirroring [ScreenshotCleanupWorker]'s no-op-when-empty behavior.
 */
class CleanupReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = ScreenshotRepository(database.screenshotDao())

            val pending = repository.getArchivedForCleanup()
            if (pending.isNotEmpty()) {
                val nm = ScreenshotNotificationManager(applicationContext)
                nm.showCleanupReminderNotification(pending.size)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
