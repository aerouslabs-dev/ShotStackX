package com.aerouslabs.shotstackx.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aerouslabs.shotstackx.ShotStackXApp
import com.aerouslabs.shotstackx.data.db.AppDatabase
import com.aerouslabs.shotstackx.data.repository.ScreenshotRepository
import com.aerouslabs.shotstackx.notifications.ScreenshotNotificationManager

class ScreenshotCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ScreenshotRepository(database.screenshotDao())

        return try {
            val archived = repository.getArchivedForCleanup()
            if (archived.isNotEmpty()) {
                val nm = ScreenshotNotificationManager(applicationContext)
                val deleted = repository.deleteScreenshotsDirectly(
                    applicationContext,
                    archived.map { it.uri }
                )
                if (deleted.isNotEmpty()) {
                    repository.markAsDeleted(deleted)
                    nm.showAutoCleanupNotification(deleted.size)
                }
                val failed = archived.size - deleted.size
                if (failed > 0) {
                    nm.showCleanupNotification(failed)
                }
            }
            val app = applicationContext as ShotStackXApp
            app.contentObserver?.clearProcessedUris()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
