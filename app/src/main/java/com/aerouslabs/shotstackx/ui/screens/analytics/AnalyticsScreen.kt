package com.aerouslabs.shotstackx.ui.screens.analytics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerouslabs.shotstackx.data.db.entity.DeletionEventEntity
import com.aerouslabs.shotstackx.ui.theme.glassSurface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class AnalyticsRange { WEEK, MONTH }

@Composable
fun AnalyticsScreen(events: List<DeletionEventEntity>, modifier: Modifier = Modifier) {
    var range by remember { mutableStateOf(AnalyticsRange.WEEK) }
    val days = if (range == AnalyticsRange.WEEK) 7 else 30
    val now = System.currentTimeMillis()
    val start = Calendar.getInstance().apply { timeInMillis = now; add(Calendar.DAY_OF_YEAR, -(days - 1)); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    val filtered = events.filter { it.deletedAt in start..now }
    val bucketStarts = (0 until days).map { offset -> Calendar.getInstance().apply { timeInMillis = start; add(Calendar.DAY_OF_YEAR, offset) }.timeInMillis }
    val values = bucketStarts.map { dayStart ->
        val nextDay = Calendar.getInstance().apply { timeInMillis = dayStart; add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
        filtered.filter { it.deletedAt >= dayStart && it.deletedAt < nextDay }.sumOf { it.bytesSaved.coerceAtLeast(0L) }
    }
    val bytes = filtered.sumOf { it.bytesSaved.coerceAtLeast(0L) }
    val count = filtered.size
    val accent = Color(0xFFE0785D)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Storage insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("Real deletion records from this device", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricGlass(Modifier.weight(1f), "Space reclaimed", formatBytes(bytes), Icons.Rounded.Storage, accent)
            MetricGlass(Modifier.weight(1f), "Files removed", count.toString(), Icons.Rounded.DeleteSweep, Color(0xFF9EAA83))
        }
        Column(
            Modifier.fillMaxWidth()
                .glassSurface(shape = RoundedCornerShape(30.dp), elevated = true)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Reclaimed over time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (range == AnalyticsRange.WEEK) "Last 7 days" else "Last 30 days", style = MaterialTheme.typography.labelMedium, color = Color(0xFF806F64))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = range == AnalyticsRange.WEEK, onClick = { range = AnalyticsRange.WEEK }, label = { Text("Week") })
                    FilterChip(selected = range == AnalyticsRange.MONTH, onClick = { range = AnalyticsRange.MONTH }, label = { Text("Month") })
                }
            }
            AnimatedContent(targetState = range, transitionSpec = { fadeIn(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) togetherWith fadeOut(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }, label = "analytics-range") {
                SavingsGraph(values = values, modifier = Modifier.fillMaxWidth())
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (range == AnalyticsRange.WEEK) "7 days" else "30 days", style = MaterialTheme.typography.labelSmall, color = Color(0xFF806F64))
                Text("Peak ${formatBytes(values.maxOrNull() ?: 0L)} / day", style = MaterialTheme.typography.labelSmall, color = Color(0xFF806F64))
            }
        }
        if (events.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(top = 18.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.AutoGraph, null, tint = accent, modifier = Modifier.size(34.dp))
                    Text("Your analytics start with your first recorded deletion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No sample data is shown.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Text("Lifetime logged savings: ${formatBytes(events.sumOf { it.bytesSaved.coerceAtLeast(0L) })}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun MetricGlass(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Column(modifier.glassSurface(shape = RoundedCornerShape(26.dp), elevated = true).padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(13.dp)).background(tint.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        AnimatedContent(
            targetState = value,
            transitionSpec = { fadeIn(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) togetherWith fadeOut(animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) },
            label = "analyticsMetricValue"
        ) { animatedValue ->
            Text(animatedValue, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF806F64))
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L -> String.format(Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    bytes >= 1024L * 1024L -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
    else -> "$bytes B"
}
