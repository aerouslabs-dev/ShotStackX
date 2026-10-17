package com.aerouslabs.shotstackx.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SavingsGraph(values: List<Long>, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(172.dp)) {
        val left = 10f
        val right = size.width - 10f
        val top = 14f
        val bottom = size.height - 14f
        val plotHeight = bottom - top
        val plotWidth = right - left
        val grid = Color(0xFFBBA99A).copy(alpha = .18f)
        repeat(4) { i ->
            val y = top + plotHeight * i / 3f
            drawLine(grid, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
        }
        if (values.isEmpty() || values.all { it <= 0L }) return@Canvas
        val maxValue = (values.maxOrNull() ?: 1L).coerceAtLeast(1L).toFloat()
        val points = values.mapIndexed { index, value ->
            val x = if (values.size <= 1) left + plotWidth / 2 else left + plotWidth * index / (values.size - 1)
            val y = bottom - (value.toFloat() / maxValue) * plotHeight
            Offset(x, y)
        }
        val line = Path().apply {
            points.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) }
        }
        val fill = Path().apply {
            moveTo(points.first().x, bottom)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, bottom)
            close()
        }
        drawPath(fill, Color(0xFFE87A5D).copy(alpha = .16f))
        drawPath(line, Color(0xFFE0785D), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        points.forEach { drawCircle(Color.White, radius = 5.dp.toPx(), center = it); drawCircle(Color(0xFFE0785D), radius = 3.dp.toPx(), center = it) }
    }
}
