package com.racion.app.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.racion.app.shared.di.AppContainer
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.CalorieProgressBar
import com.racion.app.ui.theme.EmptyState
import com.racion.app.ui.theme.MacroStat
import com.racion.app.ui.theme.MealRow
import com.racion.app.ui.theme.OrangeFab
import com.racion.app.ui.theme.RacionDivider
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.Theme

/**
 * Pantalla 01 · Inicio.
 *
 * La vista no suma nada: `HomeViewModel` entrega un `DailySummary` ya agregado y
 * cada cambio del diario actualiza el `StateFlow` automáticamente.
 */
@Composable
fun HomeContent(
    container: AppContainer,
    onOpenReport: () -> Unit,
    onAddFood: () -> Unit
) {
    val vm = remember { container.homeViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenContainer(bottomPadding = 100.dp) {
            Text(
                text = ui.greeting,
                style = AppType.display,
                color = Theme.colors.onSurface
            )
            Text(
                text = ui.summary.dateLabel.uppercase(),
                style = AppType.label,
                color = Theme.colors.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenReport),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = ui.summary.consumed.kcal.toString(),
                    style = AppType.displayNumber,
                    color = Theme.colors.onSurface
                )
                Text(
                    text = ui.caloriesLeftLabel,
                    style = AppType.bodyMedium,
                    color = if (ui.isOverGoal) Theme.colors.error else Theme.colors.onSurfaceVariant
                )
                CalorieProgressBar(
                    progress = ui.summary.progress,
                    isOverGoal = ui.isOverGoal,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = "Ver informe semanal →",
                    style = AppType.labelSmall,
                    color = Theme.colors.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MacroStat(
                    label = "Carbos",
                    value = ui.carbsLabel,
                    color = Theme.colors.primary,
                    modifier = Modifier.weight(1f)
                )
                MacroStat(
                    label = "Proteína",
                    value = ui.proteinLabel,
                    color = Theme.colors.secondary,
                    modifier = Modifier.weight(1f)
                )
                MacroStat(
                    label = "Grasas",
                    value = ui.fatLabel,
                    color = Theme.colors.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lo de hoy",
                    style = AppType.headlineMedium,
                    color = Theme.colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (ui.summary.entries.isNotEmpty()) {
                    Text(
                        text = "${ui.summary.entries.size} registros",
                        style = AppType.labelSmall,
                        color = Theme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (ui.rows.isEmpty()) {
                EmptyState(
                    emoji = "🍽",
                    title = "Todavía no registraste nada",
                    message = "Tocá el botón + para buscar un producto o escanear su código de barras."
                )
            } else {
                ui.rows.forEachIndexed { index, row ->
                    MealRow(
                        emoji = row.emoji,
                        name = row.name,
                        meta = row.meta,
                        kcal = row.kcal,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    if (index != ui.rows.lastIndex) RacionDivider()
                }
            }
        }

        OrangeFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 22.dp),
            onClick = onAddFood
        )
    }
}
