package com.aerouslabs.shotstackx

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import com.aerouslabs.shotstackx.core.constants.AppConstants
import com.aerouslabs.shotstackx.notifications.ScreenshotNotificationManager
import com.aerouslabs.shotstackx.ui.screens.home.HomeScreen
import com.aerouslabs.shotstackx.ui.theme.ShotStackXTheme
import com.aerouslabs.shotstackx.viewmodel.HomeViewModel
import com.aerouslabs.shotstackx.viewmodel.HomeViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        val app = application as ShotStackXApp
        HomeViewModelFactory(
            app.repository,
            app.settingsRepository,
            androidx.work.WorkManager.getInstance(app),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Material You splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Optimize for high refresh rate (120Hz)
        val modes = display?.supportedModes
        val maxRefreshRateMode = modes?.maxByOrNull { it.refreshRate }
        if (maxRefreshRateMode != null && (maxRefreshRateMode.refreshRate > 60f)) {
            val layoutParams = window.attributes
            layoutParams.preferredDisplayModeId = maxRefreshRateMode.modeId
            window.attributes = layoutParams
        }

        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val preferences = getSharedPreferences("shotstackx_appearance", MODE_PRIVATE)
            var themeMode by remember { mutableStateOf(preferences.getString("theme_mode", "system") ?: "system") }
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            ShotStackXTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        themeMode = themeMode,
                        onThemeModeChange = { mode ->
                            themeMode = mode
                            preferences.edit().putString("theme_mode", mode).apply()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            AppConstants.ACTION_DELETE -> {
                val uri = intent.getStringExtra(AppConstants.EXTRA_SCREENSHOT_URI)
                if (uri != null) {
                    viewModel.deleteScreenshot(this, uri)
                    val notificationManager = ScreenshotNotificationManager(this)
                    notificationManager.dismissNotification()
                }
            }
            AppConstants.ACTION_CLEANUP_OLD -> {
                viewModel.runCleanupNow(this)
                val notificationManager = ScreenshotNotificationManager(this)
                notificationManager.dismissCleanupNotification()
            }
        }
    }
}