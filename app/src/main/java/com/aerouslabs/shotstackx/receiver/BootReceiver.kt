package com.aerouslabs.shotstackx.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aerouslabs.shotstackx.ShotStackXApp

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val app = context.applicationContext as ShotStackXApp
            if (app.settingsRepository.isJanitorEnabled()) {
                app.startDetectionService()
            }
            pendingResult.finish()
        }
    }
}
