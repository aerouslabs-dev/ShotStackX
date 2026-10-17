package com.aerouslabs.shotstackx.ui.screens.home.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerouslabs.shotstackx.ui.theme.Glass
import com.aerouslabs.shotstackx.ui.theme.glassPill
import com.aerouslabs.shotstackx.ui.theme.clayPressScale
import com.aerouslabs.shotstackx.ui.theme.rememberClayPressInteraction

/**
 * Floating glassmorphic pill action bar shown on the home dashboard.
 * Three quick actions: run cleanup on unarchived (pending) screenshots,
 * toggle auto-archive, and open settings.
 */
@Composable
fun GlassActionBar(
    autoArchiveEnabled: Boolean,
    onCleanUnarchived: () -> Unit,
    onToggleArchive: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassPillButton(
            label = "Clean Unarchived",
            icon = Icons.Default.CleaningServices,
            onClick = onCleanUnarchived,
            prominent = true,
            modifier = Modifier.weight(1.3f)
        )
        GlassPillButton(
            label = if (autoArchiveEnabled) "Archiving" else "Archive",
            icon = Icons.Default.Archive,
            onClick = onToggleArchive,
            active = autoArchiveEnabled,
            modifier = Modifier.weight(1f)
        )
        GlassPillButton(
            label = "Settings",
            icon = Icons.Default.Tune,
            onClick = onOpenSettings,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GlassPillButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
    active: Boolean = false
) {
    val shape = CircleShape
    val press = rememberClayPressInteraction()
    Row(
        modifier = modifier
            .clayPressScale(press.scale)
            .clip(shape)
            .then(
                if (prominent) {
                    Modifier.background(Glass.brandGradient, shape)
                } else {
                    Modifier.glassPill(elevated = active)
                }
            )
            .clickable(interactionSource = press.interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (prominent) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (prominent) Color.White else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
