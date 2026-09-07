package com.didiprogrammer.almacaprina.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Gráfico de línea minimalista (sin librería externa) para curvas de evolución
 * (peso, producción de leche). Reutilizable en toda la app.
 */
@Composable
fun SimpleLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        if (values.size < 2) return@Canvas
        val minY = values.min()
        val maxY = values.max()
        val range = (maxY - minY).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (values.size - 1)
        val path = Path()
        val pointOffsets = values.mapIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minY) / range) * size.height
            androidx.compose.ui.geometry.Offset(x, y)
        }
        pointOffsets.forEachIndexed { index, offset ->
            if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 5f))
        pointOffsets.forEach { offset -> drawCircle(color = pointColor, radius = 6f, center = offset) }
    }
}
