package com.racion.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.racion.app.shared.models.DayTotal
import kotlin.math.max

/** Donut de distribución de macros. */
@Composable
fun DonutChart(
    carbsPct: Int,
    proteinPct: Int,
    fatPct: Int,
    centerValue: String,
    centerLabel: String,
    modifier: Modifier = Modifier
) {
    val colors = Theme.colors
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val side = minOf(size.width, size.height)
            val strokeWidth = side * 0.16f
            val diameter = side - strokeWidth
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Cada porcentaje se recorta de forma independiente: `MacroSplit` no
            // garantiza sumar 100 y un barrido negativo dibujaría hacia atrás.
            val segments = listOf(
                carbsPct to colors.primary,
                proteinPct to colors.secondary,
                fatPct to colors.tertiary
            )

            var startAngle = -90f
            for ((pct, color) in segments) {
                val clamped = pct.coerceIn(0, 100)
                if (clamped == 0) continue
                val sweep = 360f * clamped / 100f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerValue, style = AppType.titleLarge, color = Theme.colors.onSurface)
            Text(centerLabel, style = AppType.labelSmall, color = Theme.colors.onSurfaceVariant)
        }
    }
}

/**
 * Gráfico de barras de calorías por día.
 *
 * La altura es proporcional al día más alto (o a la meta, si es mayor), para que la
 * meta siempre quede por encima de las barras.
 */
@Composable
fun WeeklyBarChart(
    days: List<DayTotal>,
    goalKcal: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val available = (maxHeight - 24.dp).coerceAtLeast(10.dp)
        val top = max(
            days.maxOfOrNull { it.kcal } ?: 0,
            max(goalKcal, 1)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                val fraction = if (top == 0) 0f else day.kcal.toFloat() / top.toFloat()
                val barHeight = (available * fraction).coerceAtLeast(6.dp)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .height(barHeight)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (day.isHighlighted) Theme.colors.primary
                                else Theme.colors.surfaceContainerHighest
                            )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = day.label,
                        style = AppType.labelSmall,
                        color = Theme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Barra de progreso del día (kcal consumidas vs. meta). */
@Composable
fun CalorieProgressBar(
    progress: Double,
    isOverGoal: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(Theme.colors.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0.0, 1.0).toFloat())
                .fillMaxHeight()
                .clip(CircleShape)
                .background(if (isOverGoal) Theme.colors.error else Theme.colors.primary)
        )
    }
}

/** Punto + texto para la leyenda del donut. */
@Composable
fun LegendRow(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text, style = AppType.bodyMedium, color = Theme.colors.onSurface)
    }
}
