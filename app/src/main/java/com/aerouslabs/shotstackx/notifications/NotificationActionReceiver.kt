package com.aerouslabs.shotstackx.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aerouslabs.shotstackx.core.constants.AppConstants
import com.aerouslabs.shotstackx.data.db.AppDatabase
import com.aerouslabs.shotstackx.data.repository.ScreenshotRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val uriString = intent.getStringExtra(AppConstants.EXTRA_SCREENSHOT_URI)
        if (uriString == null) return

        val pendingResult = goAsync()
        val database by lazy { AppDatabase.getDatabase(context) }
        val repository by lazy { ScreenshotRepository(database.screenshotDao()) }
        val notificationManager by lazy { ScreenshotNotificationManager(context) }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    AppConstants.ACTION_ARCHIVE -> {
                        repository.archiveScreenshot(uriString)
                        notificationManager.dismissNotification()
                    }
                    AppConstants.ACTION_KEEP -> {
                        repository.keepScreenshot(uriString)
                        notificationManager.dismissNotification()
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
