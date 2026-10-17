package com.aerouslabs.shotstackx.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aerouslabs.shotstackx.BuildConfig
import com.aerouslabs.shotstackx.ShotStackXApp
import com.aerouslabs.shotstackx.ui.screens.home.common.AppHeader
import com.aerouslabs.shotstackx.ui.screens.analytics.AnalyticsScreen
import com.aerouslabs.shotstackx.ui.screens.home.common.SettingsSheet
import com.aerouslabs.shotstackx.ui.screens.home.screenshot.ScreenshotPreviewOverlay
import com.aerouslabs.shotstackx.ui.theme.Glass
import com.aerouslabs.shotstackx.viewmodel.HomeEvent
import com.aerouslabs.shotstackx.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    themeMode: String = "system",
    onThemeModeChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val deletionEvents by viewModel.deletionEvents.collectAsState(initial = emptyList())
    var showAnalytics by remember { mutableStateOf(false) }
    val nextCleanupTime by viewModel.nextCleanupTimeMillis.collectAsState()
    val isJanitorEnabled by viewModel.isJanitorEnabled.collectAsState()
    val isAutoArchiveEnabled by viewModel.isAutoArchiveEnabled.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isAllFilesManager by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.os.Environment.isExternalStorageManager()
            } else true
        )
    }

    var isBatteryOptDisabled by remember {
        mutableStateOf(
            (context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager)
                ?.isIgnoringBatteryOptimizations(context.packageName) == true
        )
    }

    // ── Reconciliation ───────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.reconcileDatabase(context)
    }

    // ── Lifecycle Observer (Resume & Permissions) ────────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.reconcileDatabase(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    isAllFilesManager = android.os.Environment.isExternalStorageManager()
                }
                isBatteryOptDisabled =
                    (context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager)
                        ?.isIgnoringBatteryOptimizations(context.packageName) == true
            }
        })
    }

    // ── Permission launcher ──────────────────────────────────────────────────
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onDeletePermissionGranted()
        } else {
            viewModel.onDeletePermissionDenied()
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.RequestDeletePermission -> {
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(event.intentSender).build()
                    )
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission =
                permissions[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
        }
        hasStoragePermission =
            permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: hasStoragePermission
    }

    val batteryOptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isBatteryOptDisabled =
            (context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager)
                ?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Hold-to-preview: the uri currently shown in the full-screen overlay (null = hidden)
    var previewUri by remember { mutableStateOf<String?>(null) }
    // The on-screen rect of the thumbnail that triggered the preview, used to
    // animate the overlay open/close as a Material 3 container transform.
    var previewRect by remember { mutableStateOf<Rect?>(null) }

    // Settings bottom sheet visibility
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.backgroundBrush())
            .background(Glass.glowBrush())
            .statusBarsPadding()
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppHeader(
                    versionName = BuildConfig.VERSION_NAME,
                    versionCode = BuildConfig.VERSION_CODE,
                    onAnalyticsClick = { showAnalytics = !showAnalytics },
                    isAnalytics = showAnalytics
                )
            }
        ) { innerPadding ->
            if (showAnalytics) {
                AnalyticsScreen(
                    events = deletionEvents,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
                )
            } else Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                HomeContent(
                    innerPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                    uiState = uiState,
                    nextCleanupTime = nextCleanupTime,
                    isJanitorEnabled = isJanitorEnabled,
                    isAutoArchiveEnabled = isAutoArchiveEnabled,
                    onToggleJanitor = {
                        viewModel.toggleJanitor()
                        val app = context.applicationContext as ShotStackXApp
                        if (viewModel.isJanitorEnabled.value) {
                            app.startDetectionService()
                        } else {
                            app.stopDetectionService()
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (viewModel.isJanitorEnabled.value) "Janitor On" else "Janitor Off"
                            )
                        }
                    },
                    hasNotificationPermission = hasNotificationPermission,
                    hasStoragePermission = hasStoragePermission,
                    isAllFilesManager = isAllFilesManager,
                    isBatteryOptDisabled = isBatteryOptDisabled,
                    onRequestPermissions = {
                        val list = mutableListOf<String>()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            list.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        if (!hasStoragePermission) {
                            list.add(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                        if (list.isNotEmpty()) {
                            permissionLauncher.launch(list.toTypedArray())
                        }
                    },
                    onRequestAllFilesAccess = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    },
                    onRequestDisableBatteryOpt = {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        ).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        batteryOptLauncher.launch(intent)
                    },
                    onRunCleanup = { viewModel.runCleanupNow(context) },
                    onReschedule = { hour, minute -> viewModel.rescheduleCleanup(hour, minute, context) },
                    onArchive = { viewModel.archiveScreenshot(it) },
                    onKeep = { viewModel.keepScreenshot(it) },
                    onDelete = { viewModel.deleteScreenshot(context, it) },
                    onToggleAutoArchive = {
                        viewModel.toggleAutoArchive()
                        val isEnabled = viewModel.isAutoArchiveEnabled.value
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (isEnabled) "Auto-Archive Enabled" else "Auto-Archive Disabled"
                            )
                        }
                    },
                    onCleanUnarchived = {
                        val pendingUris = uiState.screenshots
                            .filter { !it.deleted && !it.kept && !it.archived }
                            .map { it.uri }
                        if (pendingUris.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Nothing to clean") }
                        } else {
                            pendingUris.forEach { viewModel.archiveScreenshot(it) }
                            viewModel.runCleanupNow(context)
                            scope.launch {
                                snackbarHostState.showSnackbar("Cleaning ${pendingUris.size} unarchived screenshot(s)")
                            }
                        }
                    },
                    onOpenSettings = { showSettings = true },
                    onHoldComplete = { uri, rect -> previewUri = uri; previewRect = rect },
                    onRelease = { previewUri = null },
                    isPreviewOpen = previewUri != null
                )
            }
        }

        ScreenshotPreviewOverlay(
            uriString = previewUri,
            sourceRect = previewRect,
            visible = previewUri != null,
            onDismiss = { previewUri = null }
        )

        if (showSettings) {
            SettingsSheet(
                isJanitorEnabled = isJanitorEnabled,
                isAutoArchiveEnabled = isAutoArchiveEnabled,
                onToggleJanitor = {
                    viewModel.toggleJanitor()
                    val app = context.applicationContext as ShotStackXApp
                    if (viewModel.isJanitorEnabled.value) app.startDetectionService() else app.stopDetectionService()
                },
                onToggleAutoArchive = { viewModel.toggleAutoArchive() },
                onDismiss = { showSettings = false },
                versionName = BuildConfig.VERSION_NAME,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
            )
        }
    }
}
