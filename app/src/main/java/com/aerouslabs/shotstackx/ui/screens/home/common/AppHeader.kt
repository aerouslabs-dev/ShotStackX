package com.aerouslabs.shotstackx.ui.screens.home.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aerouslabs.shotstackx.ui.theme.clayPressScale
import com.aerouslabs.shotstackx.ui.theme.rememberClayPressInteraction

/** Branded warm-clay header, using the screenshot-bin mark as ShotStackX's identity. */
@Composable
fun AppHeader(
    versionName: String,
    versionCode: Int,
    onAnalyticsClick: () -> Unit,
    isAnalytics: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFFDF9), Color(0xFFF4E6DA))))
                    .border(1.dp, Color.White.copy(alpha = .94f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "ShotStackX screenshot cleaner",
                    tint = Color(0xFFE0785D),
                    modifier = Modifier.size(27.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "ShotStackX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "AEROUS LABS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.clip(CircleShape)
                    .background(Color(0xFFFFFBF6))
                    .border(1.dp, Color.White.copy(alpha = .94f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "v$versionName · $versionCode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            val press = rememberClayPressInteraction()
            Box(
                modifier = Modifier.size(42.dp).clayPressScale(press.scale)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE87A5D), Color(0xFFE07A5F))))
                    .clickable(interactionSource = press.interactionSource, indication = null, onClick = onAnalyticsClick),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isAnalytics,
                    transitionSpec = { fadeIn(spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) togetherWith fadeOut(spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) },
                    label = "headerAnalyticsIcon"
                ) { analytics ->
                    Icon(
                        imageVector = if (analytics) Icons.Default.Home else Icons.Default.Insights,
                        contentDescription = if (analytics) "Open home" else "Open analytics",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}
