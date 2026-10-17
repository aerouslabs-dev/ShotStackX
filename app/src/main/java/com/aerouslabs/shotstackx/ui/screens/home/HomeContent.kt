package com.aerouslabs.shotstackx.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aerouslabs.shotstackx.data.db.entity.ScreenshotEntity
import com.aerouslabs.shotstackx.ui.screens.home.common.NextCleanupBanner
import com.aerouslabs.shotstackx.ui.screens.home.permissions.PermissionWarningSection
import com.aerouslabs.shotstackx.ui.screens.home.screenshot.ScreenshotThumbnail
import com.aerouslabs.shotstackx.ui.theme.glassSurface
import com.aerouslabs.shotstackx.ui.theme.rememberClayPressInteraction
import com.aerouslabs.shotstackx.ui.theme.clayPressScale
import com.aerouslabs.shotstackx.viewmodel.HomeUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ScreenshotFilter(val title: String) { All("All"), Pending("Pending"), Archived("Archived"), Cleaned("Cleaned") }
private enum class ThumbnailLayout { Grid, List }

@Composable
fun HomeContent(
    innerPadding: PaddingValues,
    uiState: HomeUiState,
    nextCleanupTime: Long?,
    hasNotificationPermission: Boolean,
    hasStoragePermission: Boolean,
    isAllFilesManager: Boolean,
    isBatteryOptDisabled: Boolean,
    onRequestPermissions: () -> Unit,
    onRequestAllFilesAccess: () -> Unit,
    onRequestDisableBatteryOpt: () -> Unit,
    onRunCleanup: () -> Unit,
    onReschedule: (Int, Int) -> Unit,
    onArchive: (String) -> Unit,
    onKeep: (String) -> Unit,
    onDelete: (String) -> Unit,
    onToggleAutoArchive: () -> Unit,
    isJanitorEnabled: Boolean = true,
    isAutoArchiveEnabled: Boolean = false,
    onToggleJanitor: () -> Unit = {},
    onCleanUnarchived: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onHoldComplete: (String, Rect) -> Unit = { _, _ -> },
    onRelease: () -> Unit = {},
    isPreviewOpen: Boolean = false
) {
    var filter by rememberSaveable { mutableStateOf(ScreenshotFilter.All.name) }
    var layout by rememberSaveable { mutableStateOf(ThumbnailLayout.Grid.name) }
    val activeFilter = ScreenshotFilter.valueOf(filter)
    val activeLayout = ThumbnailLayout.valueOf(layout)
    val screenshots = remember(uiState.screenshots, activeFilter) {
        when (activeFilter) {
            ScreenshotFilter.All -> uiState.screenshots
            ScreenshotFilter.Pending -> uiState.screenshots.filter { !it.deleted && !it.archived && !it.kept }
            ScreenshotFilter.Archived -> uiState.screenshots.filter { !it.deleted && it.archived && !it.kept }
            ScreenshotFilter.Cleaned -> uiState.screenshots.filter { it.deleted }
        }.sortedByDescending { it.createdAt }
    }
    val totalBytes = uiState.screenshots.filter { !it.deleted }.sumOf { it.fileSize }
    val removableBytes = uiState.screenshots.filter { !it.deleted && !it.kept }.sumOf { it.fileSize }
    val progress = if (totalBytes > 0L) (removableBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
    val dateFormatter = remember { SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()) }

    val contentModifier = Modifier
        .fillMaxSize()
        .padding(top = innerPadding.calculateTopPadding())

    if (activeLayout == ThumbnailLayout.Grid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 154.dp),
            modifier = contentModifier,
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = innerPadding.calculateBottomPadding() + 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = !isPreviewOpen
        ) {
            item(key = "hero", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                StorageHero(
                    count = uiState.totalCount - uiState.deletedCount,
                    removableBytes = removableBytes,
                    pendingCount = uiState.pendingCount,
                    progress = progress,
                    onSettings = onOpenSettings,
                    onClean = onCleanUnarchived
                )
            }
            if (!hasNotificationPermission || !hasStoragePermission || !isAllFilesManager || !isBatteryOptDisabled) {
                item(key = "permissions", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    PermissionWarningSection(
                        hasNotificationPermission = hasNotificationPermission,
                        hasStoragePermission = hasStoragePermission,
                        isAllFilesManager = isAllFilesManager,
                        isBatteryOptDisabled = isBatteryOptDisabled,
                        onRequestPermissions = onRequestPermissions,
                        onRequestAllFilesAccess = onRequestAllFilesAccess,
                        onRequestDisableBatteryOpt = onRequestDisableBatteryOpt
                    )
                }
            }
            if (nextCleanupTime != null) {
                item(key = "cleanup", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    NextCleanupBanner(timeMillis = nextCleanupTime, onRunNow = onRunCleanup, onReschedule = onReschedule, isActive = isJanitorEnabled)
                }
            }
            item(key = "filter_bar", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                FilterAndLayoutBar(
                    activeFilter = activeFilter,
                    onFilter = { filter = it.name },
                    layout = activeLayout,
                    onLayout = { layout = it.name },
                    counts = mapOf(
                        ScreenshotFilter.All to uiState.screenshots.size,
                        ScreenshotFilter.Pending to uiState.pendingCount,
                        ScreenshotFilter.Archived to uiState.archivedCount,
                        ScreenshotFilter.Cleaned to uiState.deletedCount
                    )
                )
            }
            item(key = "section_label", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                SectionIntro(count = screenshots.size, filter = activeFilter)
            }
            if (screenshots.isEmpty()) {
                item(key = "empty", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { EmptyGlassState(activeFilter) }
            } else {
                gridItems(screenshots, key = { "shot_${it.uri}" }) { shot ->
                    ScreenshotGlassCard(
                        screenshot = shot,
                        grid = true,
                        dateFormatter = dateFormatter,
                        onArchive = { onArchive(shot.uri) },
                        onKeep = { onKeep(shot.uri) },
                        onDelete = { onDelete(shot.uri) },
                        onHoldComplete = onHoldComplete,
                        onRelease = onRelease
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = innerPadding.calculateBottomPadding() + 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = !isPreviewOpen
        ) {
            item(key = "hero") {
                StorageHero(
                    count = uiState.totalCount - uiState.deletedCount,
                    removableBytes = removableBytes,
                    pendingCount = uiState.pendingCount,
                    progress = progress,
                    onSettings = onOpenSettings,
                    onClean = onCleanUnarchived
                )
            }
            if (!hasNotificationPermission || !hasStoragePermission || !isAllFilesManager || !isBatteryOptDisabled) {
                item(key = "permissions") {
                    PermissionWarningSection(
                        hasNotificationPermission = hasNotificationPermission,
                        hasStoragePermission = hasStoragePermission,
                        isAllFilesManager = isAllFilesManager,
                        isBatteryOptDisabled = isBatteryOptDisabled,
                        onRequestPermissions = onRequestPermissions,
                        onRequestAllFilesAccess = onRequestAllFilesAccess,
                        onRequestDisableBatteryOpt = onRequestDisableBatteryOpt
                    )
                }
            }
            if (nextCleanupTime != null) item(key = "cleanup") {
                NextCleanupBanner(timeMillis = nextCleanupTime, onRunNow = onRunCleanup, onReschedule = onReschedule, isActive = isJanitorEnabled)
            }
            item(key = "filter_bar") {
                FilterAndLayoutBar(
                    activeFilter = activeFilter,
                    onFilter = { filter = it.name },
                    layout = activeLayout,
                    onLayout = { layout = it.name },
                    counts = mapOf(ScreenshotFilter.All to uiState.screenshots.size, ScreenshotFilter.Pending to uiState.pendingCount, ScreenshotFilter.Archived to uiState.archivedCount, ScreenshotFilter.Cleaned to uiState.deletedCount)
                )
            }
            item(key = "section_label") { SectionIntro(count = screenshots.size, filter = activeFilter) }
            if (screenshots.isEmpty()) item(key = "empty") { EmptyGlassState(activeFilter) }
            else items(screenshots, key = { "shot_${it.uri}" }) { shot ->
                ScreenshotGlassCard(
                    screenshot = shot,
                    grid = false,
                    dateFormatter = dateFormatter,
                    onArchive = { onArchive(shot.uri) },
                    onKeep = { onKeep(shot.uri) },
                    onDelete = { onDelete(shot.uri) },
                    onHoldComplete = onHoldComplete,
                    onRelease = onRelease
                )
            }
        }
    }
}

@Composable
private fun StorageHero(count: Int, removableBytes: Long, pendingCount: Int, progress: Float, onSettings: () -> Unit, onClean: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val heroText = if (dark) Color(0xFFF6EDE7) else Color(0xFF342B27)
    val heroMuted = if (dark) Color(0xFFD2BEB3) else Color(0xFF806F64)
    val heroBrush = if (dark) Brush.linearGradient(listOf(Color(0xFF30251F), Color(0xFF382820), Color(0xFF2A211B))) else Brush.linearGradient(listOf(Color(0xFFFFFDF9), Color(0xFFFFF7F0), Color(0xFFF8E9DF)))
    val animatedCount by animateIntAsState(targetValue = count, animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow), label = "trackedScreenshotCount")
    val animatedPending by animateIntAsState(targetValue = pendingCount, animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow), label = "pendingScreenshotCount")
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow), label = "storageProgress")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .shadow(22.dp, RoundedCornerShape(32.dp), ambientColor = Color(0x26B88A70), spotColor = Color(0x24956D57))
            .shadow(5.dp, RoundedCornerShape(32.dp), ambientColor = Color.White.copy(alpha = 0.64f), spotColor = Color(0x14B88A70))
            .clip(RoundedCornerShape(32.dp))
            .background(heroBrush)
            .border(1.dp, Color.White.copy(alpha = .88f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("AEROUS LABS  /  SHOTSTACKX", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB9654C), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text("Your screenshots,\nunder control.", style = MaterialTheme.typography.headlineSmall, color = heroText, fontWeight = FontWeight.SemiBold, lineHeight = MaterialTheme.typography.headlineSmall.lineHeight)
            }
            run {
                val press = rememberClayPressInteraction()
                Box(
                    modifier = Modifier.size(44.dp).clayPressScale(press.scale)
                        .background(if (dark) Color(0xFF47342A) else Color(0xFFFFF8F1), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = .92f), CircleShape)
                        .clickable(interactionSource = press.interactionSource, indication = null, onClick = onSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFFE0785D))
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("TRACKED LIBRARY", style = MaterialTheme.typography.labelSmall, color = heroMuted, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(animatedCount.toString(), style = MaterialTheme.typography.displaySmall, color = heroText, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(7.dp))
                    Text("screenshots", modifier = Modifier.padding(bottom = 7.dp), style = MaterialTheme.typography.bodyMedium, color = heroMuted)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("SPACE TO REVIEW", style = MaterialTheme.typography.labelSmall, color = heroMuted)
                AnimatedContent(
                    targetState = formatBytes(removableBytes),
                    transitionSpec = { fadeIn(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) togetherWith fadeOut(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) },
                    label = "removableStorageLabel"
                ) { animatedBytes ->
                    Text(animatedBytes, style = MaterialTheme.typography.titleMedium, color = Color(0xFF9A4D39), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp), modifier = Modifier.fillMaxWidth()) {
            StorageProgressRing(progress = animatedProgress, dark = dark)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text("REVIEWABLE LIBRARY", style = MaterialTheme.typography.labelSmall, color = heroMuted, fontWeight = FontWeight.Bold)
                Text(
                    "${(animatedProgress * 100f).toInt()}% of tracked space is ready to review",
                    style = MaterialTheme.typography.bodyMedium,
                    color = heroText,
                    fontWeight = FontWeight.Medium
                )
                Text("Kept screenshots stay protected", style = MaterialTheme.typography.bodySmall, color = heroMuted)
            }
        }
        Spacer(Modifier.height(15.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Color(0xFFE87A5D), CircleShape))
                Spacer(Modifier.width(7.dp))
                Text("$animatedPending awaiting review", style = MaterialTheme.typography.bodySmall, color = heroMuted)
            }
            run {
                val press = rememberClayPressInteraction()
                Row(
                    modifier = Modifier.clayPressScale(press.scale).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFE87A5D), Color(0xFFE07A5F))))
                        .clickable(interactionSource = press.interactionSource, indication = null, onClick = onClean)
                        .padding(horizontal = 15.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CleaningServices, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Review & clean", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


@Composable
private fun StorageProgressRing(progress: Float, dark: Boolean) {
    val ringColors = listOf(Color(0xFFE87A5D), Color(0xFFF0A47E), Color(0xFFE7BA8D), Color(0xFFE0785D))
    Box(
        modifier = Modifier.size(68.dp).glassSurface(shape = CircleShape, elevated = true),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(5.dp)) {
            val strokeWidth = 7.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            drawArc(
                color = if (dark) Color(0xFF554339) else Color(0xFFE5D8CC),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = style
            )
            drawArc(
                brush = Brush.sweepGradient(ringColors),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = style
            )
        }
        Text(
            text = "${(progress * 100f).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = if (dark) Color(0xFFFFD8C8) else Color(0xFF9A4D39),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilterAndLayoutBar(activeFilter: ScreenshotFilter, onFilter: (ScreenshotFilter) -> Unit, layout: ThumbnailLayout, onLayout: (ThumbnailLayout) -> Unit, counts: Map<ScreenshotFilter, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            ScreenshotFilter.entries.forEach { item ->
                val selected = activeFilter == item
                val fill by animateColorAsState(if (selected) Color(0xFFE87A5D) else Color(0xFFFFFBF6), tween(220), label = "filterFill")
                val textColor by animateColorAsState(if (selected) Color.White else Color(0xFF806F64), tween(220), label = "filterText")
                val press = rememberClayPressInteraction()
                Row(
                    modifier = Modifier.weight(1f).clayPressScale(press.scale)
                        .shadow(if (selected) 10.dp else 4.dp, CircleShape, ambientColor = Color(0x24B88A70), spotColor = Color(0x1F956D57))
                        .clip(CircleShape).background(fill)
                        .border(1.dp, if (selected) Color.White.copy(.34f) else Color.White.copy(.90f), CircleShape)
                        .clickable(interactionSource = press.interactionSource, indication = null) { onFilter(item) }
                        .padding(horizontal = 8.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.title, color = textColor, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (selected) {
                        Spacer(Modifier.width(5.dp))
                        AnimatedContent(
                            targetState = counts[item] ?: 0,
                            transitionSpec = { fadeIn(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) togetherWith fadeOut(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) },
                            label = "filterCount"
                        ) { animatedFilterCount ->
                            Text(animatedFilterCount.toString(), color = Color.White.copy(.92f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Text("VIEW", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9B8475), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Row(Modifier.clip(CircleShape).background(Color(0xFFF0E7DD)).border(1.dp, Color.White.copy(.90f), CircleShape).padding(3.dp)) {
                LayoutOption(selected = layout == ThumbnailLayout.Grid, icon = { Icon(Icons.Default.GridView, null) }, label = "Grid") { onLayout(ThumbnailLayout.Grid) }
                LayoutOption(selected = layout == ThumbnailLayout.List, icon = { Icon(Icons.AutoMirrored.Filled.ViewList, null) }, label = "List") { onLayout(ThumbnailLayout.List) }
            }
        }
    }
}

@Composable
private fun LayoutOption(selected: Boolean, icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    val press = rememberClayPressInteraction()
    Box(
        Modifier.size(width = 42.dp, height = 34.dp).clayPressScale(press.scale)
            .clip(CircleShape).background(if (selected) Color(0xFFFFFDF9) else Color.Transparent)
            .clickable(interactionSource = press.interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Box { icon() }
    }
}

@Composable
private fun SectionIntro(count: Int, filter: ScreenshotFilter) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 1.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column {
            Text(when (filter) { ScreenshotFilter.All -> "Your library"; ScreenshotFilter.Pending -> "Needs attention"; ScreenshotFilter.Archived -> "Archived"; ScreenshotFilter.Cleaned -> "Recently cleaned" }, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Text("A quieter way to stay organized", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val animatedCount by animateIntAsState(targetValue = count, animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow), label = "librarySectionCount")
        Text("$animatedCount items", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScreenshotGlassCard(screenshot: ScreenshotEntity, grid: Boolean, dateFormatter: SimpleDateFormat, onArchive: () -> Unit, onKeep: () -> Unit, onDelete: () -> Unit, onHoldComplete: (String, Rect) -> Unit, onRelease: () -> Unit) {
    val isCleaned = screenshot.deleted
    val status = when { isCleaned -> "Cleaned"; screenshot.kept -> "Kept"; screenshot.archived -> "Archived"; else -> "Pending" }
    val date = remember(screenshot.createdAt) { dateFormatter.format(Date(screenshot.createdAt)) }
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) +
            slideInVertically(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) { it / 12 }
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(28.dp), elevated = true)
            .padding(if (grid) 10.dp else 12.dp)
    ) {
        if (grid) {
            Box(Modifier.fillMaxWidth().aspectRatio(0.94f).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0E8DE))) {
                ScreenshotThumbnail(uriString = screenshot.uri, modifier = Modifier.fillMaxSize(), fillBounds = true, onHoldComplete = onHoldComplete, onRelease = onRelease)
                Box(Modifier.align(Alignment.TopStart).padding(7.dp).clip(CircleShape).background(Color.White.copy(.82f)).padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text(status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF5F4940), fontWeight = FontWeight.SemiBold)
                }
                if (screenshot.kept) Box(Modifier.align(Alignment.TopEnd).padding(7.dp).size(25.dp).background(Color(0xFFE0785D), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            }
            Spacer(Modifier.height(10.dp))
            Text(screenshot.fileName.ifBlank { "Screenshot" }, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(10.dp))
            ScreenshotActions(screenshot, onArchive, onKeep, onDelete, compact = true)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0E8DE))) {
                    ScreenshotThumbnail(uriString = screenshot.uri, modifier = Modifier.fillMaxSize(), fillBounds = true, onHoldComplete = onHoldComplete, onRelease = onRelease)
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(when(status) { "Pending" -> Color(0xFFE87A5D); "Archived" -> Color(0xFF9EAA83); "Cleaned" -> Color(0xFF8FA27C); else -> Color(0xFFD5A15D) }, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(status.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(screenshot.fileName.ifBlank { "Screenshot" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    ScreenshotActions(screenshot, onArchive, onKeep, onDelete, compact = false)
                }
            }
        }
    }
    }
}

@Composable
private fun ScreenshotActions(screenshot: ScreenshotEntity, onArchive: () -> Unit, onKeep: () -> Unit, onDelete: () -> Unit, compact: Boolean) {
    if (screenshot.deleted) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!screenshot.kept && !screenshot.archived) ActionPill("Archive", Modifier.weight(1f), onArchive)
        if (!screenshot.kept) ActionPill("Keep", Modifier.weight(1f), onKeep)
        if (!compact || screenshot.kept || screenshot.archived) ActionPill("Delete", Modifier.weight(1f), onDelete, quiet = true)
    }
}

@Composable
private fun ActionPill(label: String, modifier: Modifier = Modifier, onClick: () -> Unit, quiet: Boolean = false) {
    val press = rememberClayPressInteraction()
    Box(
        modifier.clayPressScale(press.scale).clip(CircleShape)
            .then(
                if (quiet) Modifier.background(Color(0xFFF0E7DD))
                else Modifier.background(Brush.linearGradient(listOf(Color(0xFFE87A5D), Color(0xFFE07A5F))))
            )
            .clickable(interactionSource = press.interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (quiet) Color(0xFF806F64) else Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun EmptyGlassState(filter: ScreenshotFilter) {
    Column(Modifier.fillMaxWidth().padding(vertical = 30.dp).glassSurface(RoundedCornerShape(26.dp), elevated = true).padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).background(Color(0xFFF7DED3), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.ImageNotSupported, null, tint = Color(0xFFE0785D), modifier = Modifier.size(26.dp)) }
        Spacer(Modifier.height(12.dp))
        Text(if (filter == ScreenshotFilter.All) "A little breathing room" else "Nothing here right now", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(if (filter == ScreenshotFilter.All) "New screenshots will appear here when detected." else "Try another filter or capture a new screenshot.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024L * 1024L -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format(Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}
