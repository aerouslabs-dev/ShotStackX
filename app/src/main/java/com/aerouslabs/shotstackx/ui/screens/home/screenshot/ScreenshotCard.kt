package com.aerouslabs.shotstackx.ui.screens.home.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aerouslabs.shotstackx.data.db.entity.ScreenshotEntity
import com.aerouslabs.shotstackx.ui.theme.glassSurface
import com.aerouslabs.shotstackx.ui.theme.rememberClayPressInteraction
import com.aerouslabs.shotstackx.ui.theme.clayPressScale
import java.text.SimpleDateFormat
import java.util.Date

/** Lightweight glass card retained for kept-section callers and list fallbacks. */
@Composable
fun ScreenshotCard(
    screenshot: ScreenshotEntity,
    dateFormatter: SimpleDateFormat,
    onArchive: () -> Unit,
    onKeep: () -> Unit,
    onDelete: () -> Unit,
    onHoldComplete: (String, Rect) -> Unit = { _, _ -> },
    onRelease: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val date = remember(screenshot.createdAt) { dateFormatter.format(Date(screenshot.createdAt)) }
    val status = when { screenshot.deleted -> "Cleaned"; screenshot.kept -> "Kept"; screenshot.archived -> "Archived"; else -> "Pending" }
    Column(
        modifier = modifier.fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(28.dp), elevated = true)
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                ScreenshotThumbnail(uriString = screenshot.uri, modifier = Modifier.fillMaxSize(), fillBounds = true, onHoldComplete = onHoldComplete, onRelease = onRelease)
                Box(Modifier.align(Alignment.TopStart).padding(6.dp).clip(CircleShape).background(Color.White.copy(.84f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF5F4940), fontWeight = FontWeight.SemiBold)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(screenshot.fileName.ifBlank { "Screenshot" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                if (!screenshot.deleted) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!screenshot.kept && !screenshot.archived) MiniAction("Archive", Icons.Default.Archive, onArchive)
                        if (!screenshot.kept) MiniAction("Keep", Icons.Default.Bookmark, onKeep)
                        MiniAction("Delete", Icons.Default.DeleteOutline, onDelete, quiet = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, quiet: Boolean = false) {
    val press = rememberClayPressInteraction()
    Row(
        Modifier.clayPressScale(press.scale).clip(CircleShape)
            .then(if (quiet) Modifier.background(Color(0xFFF0E7DD)) else Modifier.background(Brush.linearGradient(listOf(Color(0xFFE87A5D), Color(0xFFE07A5F)))))
            .clickable(interactionSource = press.interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (quiet) Color(0xFF806F64) else Color.White, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (quiet) Color(0xFF806F64) else Color.White, fontWeight = FontWeight.SemiBold)
    }
}
