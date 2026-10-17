package com.aerouslabs.shotstackx.ui.screens.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun rememberEntranceScale(key: Any): Float {
    val scale = androidx.compose.runtime.remember(key) { Animatable(0.94f) }
    LaunchedEffect(key) { scale.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)) }
    return scale.value
}

@Composable
fun Modifier.springPressScale(pressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "press-scale"
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}
