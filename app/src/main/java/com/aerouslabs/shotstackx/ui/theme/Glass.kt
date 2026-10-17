package com.aerouslabs.shotstackx.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/** Shared surfaces and motion tokens for ShotStackX's organic warm-clay UI. */
object Glass {
    @Composable
    fun backgroundBrush(): Brush {
        val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return Brush.linearGradient(
            colors = if (dark) {
                listOf(Color(0xFF1C1714), Color(0xFF28201B), Color(0xFF30231D), Color(0xFF211A16))
            } else {
                listOf(Color(0xFFF7F3ED), Color(0xFFF4EEE5), Color(0xFFEFEAE1), Color(0xFFF8F4EE))
            },
            start = Offset(0f, 0f),
            end = Offset(1200f, 1800f)
        )
    }

    @Composable
    fun glowBrush(): Brush {
        val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return Brush.radialGradient(
            colors = listOf(
                Color(0xFFE87A5D).copy(alpha = if (dark) 0.14f else 0.10f),
                Color(0xFFF1C7AD).copy(alpha = if (dark) 0.04f else 0.16f),
                Color.Transparent
            ),
            center = Offset(900f, 240f),
            radius = 1050f
        )
    }

    @Composable
    fun surfaceColor(elevated: Boolean = false): Color {
        val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return when {
            dark && elevated -> Color(0xFF30251F)
            dark -> Color(0xFF281F1A)
            elevated -> Color(0xFFFFFDF9)
            else -> Color(0xFFFBF8F2)
        }
    }

    @Composable
    fun borderColor(): Color {
        val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return if (dark) Color(0xFF594238).copy(alpha = 0.72f) else Color(0xFFFFFFFF).copy(alpha = 0.90f)
    }

    val brandGradient: Brush
        @Composable get() = Brush.linearGradient(
            colors = listOf(Color(0xFFE87A5D), Color(0xFFE07A5F), Color(0xFFF09A7D))
        )

    val ringGradient: Brush
        @Composable get() = Brush.sweepGradient(
            colors = listOf(Color(0xFFE87A5D), Color(0xFFF0A47E), Color(0xFFE8BC8A), Color(0xFFE07A5F), Color(0xFFE87A5D))
        )
}

/** Cream porcelain tile with stacked ambient shadows and a softly lit upper edge. */
@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(30.dp),
    elevated: Boolean = false
): Modifier {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val ambient = if (dark) Color(0xFF000000).copy(alpha = 0.18f) else Color(0xFFBBA99A).copy(alpha = 0.18f)
    val spot = if (dark) Color(0xFF000000).copy(alpha = 0.26f) else Color(0xFF9D7D69).copy(alpha = 0.13f)
    val fill = Glass.surfaceColor(elevated)
    return this
        .shadow(if (elevated) 18.dp else 10.dp, shape, clip = false, ambientColor = ambient, spotColor = spot)
        .shadow(if (elevated) 5.dp else 2.dp, shape, clip = false, ambientColor = Color.White.copy(alpha = if (dark) 0.015f else 0.72f), spotColor = Color(0xFFB9A99A).copy(alpha = 0.08f))
        .clip(shape)
        .background(Brush.linearGradient(listOf(fill, fill.copy(alpha = if (dark) 0.94f else 0.98f))), shape)
        .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = if (dark) 0.14f else 0.96f), Glass.borderColor(), Color(0xFFE3D6CA).copy(alpha = if (dark) 0.20f else 0.54f))), shape)
}

@Composable
fun Modifier.glassPill(elevated: Boolean = true): Modifier =
    this.glassSurface(shape = CircleShape, elevated = elevated)

/** Share this interaction source with clickable() to get tactile spring feedback. */
class ClayPressInteraction internal constructor(
    val interactionSource: MutableInteractionSource,
    val scale: Float
)

@Composable
fun rememberClayPressInteraction(): ClayPressInteraction {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "clayPressScale"
    )
    return ClayPressInteraction(source, scale)
}

fun Modifier.clayPressScale(scale: Float): Modifier = graphicsLayer {
    scaleX = scale
    scaleY = scale
}
