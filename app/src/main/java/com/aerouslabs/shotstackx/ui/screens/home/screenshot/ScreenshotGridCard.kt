package com.aerouslabs.shotstackx.ui.screens.home.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.aerouslabs.shotstackx.data.db.entity.ScreenshotEntity
import com.aerouslabs.shotstackx.ui.theme.glassSurface
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ScreenshotGridCard(
    screenshot: ScreenshotEntity,
    dateFormatter: SimpleDateFormat,
    onArchive: () -> Unit,
    onKeep: () -> Unit,
    onDelete: () -> Unit,
    onHoldComplete: (String, Rect) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().glassSurface(shape = RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            ScreenshotThumbnail(
                uriString = screenshot.uri,
                modifier = Modifier.fillMaxWidth(),
                onHoldComplete = onHoldComplete,
                onRelease = onRelease,
                thumbnailSize = 132.dp
            )
            Text(
                text = screenshot.fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val created = remember(screenshot.createdAt) { dateFormatter.format(Date(screenshot.createdAt)) }
            Text(
                text = created,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = when { screenshot.kept -> "Kept"; screenshot.archived -> "Archived"; else -> "Pending" },
                style = MaterialTheme.typography.labelSmall,
                color = when { screenshot.kept -> MaterialTheme.colorScheme.primary; screenshot.archived -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.secondary }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!screenshot.archived && !screenshot.kept) {
                    IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive screenshot", tint = MaterialTheme.colorScheme.tertiary)
                    }
                }
                if (!screenshot.kept) {
                    IconButton(onClick = onKeep, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Keep screenshot", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete screenshot", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
