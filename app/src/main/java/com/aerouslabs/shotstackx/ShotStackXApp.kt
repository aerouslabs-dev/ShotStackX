package com.aerouslabs.shotstackx

import android.app.Application
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import com.aerouslabs.shotstackx.data.db.AppDatabase
import com.aerouslabs.shotstackx.data.repository.ScreenshotRepository
import com.aerouslabs.shotstackx.data.repository.SettingsRepository
import com.aerouslabs.shotstackx.observer.ScreenshotContentObserver
import com.aerouslabs.shotstackx.service.ScreenshotDetectionService
import com.aerouslabs.shotstackx.worker.CleanupScheduler

class ShotStackXApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: ScreenshotRepository by lazy { ScreenshotRepository(database.screenshotDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    var contentObserver: ScreenshotContentObserver? = null

    override fun onCreate() {
        super.onCreate()
        if (settingsRepository.isJanitorEnabled()) {
            startDetectionService()
        }
        scheduleCleanupWorker()
    }

    fun startDetectionService() {
        val intent = Intent(this, ScreenshotDetectionService::class.java)
        startForegroundService(intent)
    }

    fun stopDetectionService() {
        val intent = Intent(this, ScreenshotDetectionService::class.java)
        stopService(intent)
    }

    private fun scheduleCleanupWorker() {
        val hour = settingsRepository.getCleanupHour()
        val minute = settingsRepository.getCleanupMinute()
        val delayMillis = CleanupScheduler.computeDelayMillis(hour, minute)

        CleanupScheduler.scheduleCleanup(
            this,
            delayMillis,
            ExistingPeriodicWorkPolicy.KEEP
        )
        CleanupScheduler.scheduleReminder(
            this,
            delayMillis,
            ExistingPeriodicWorkPolicy.KEEP
        )
    }
}
