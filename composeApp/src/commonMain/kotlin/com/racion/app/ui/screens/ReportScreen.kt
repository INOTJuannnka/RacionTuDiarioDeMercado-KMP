package com.racion.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.racion.app.shared.di.AppContainer
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.BackRow
import com.racion.app.ui.theme.DonutChart
import com.racion.app.ui.theme.EmptyState
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.LegendRow
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.SummaryPill
import com.racion.app.ui.theme.SurfaceCard
import com.racion.app.ui.theme.Theme
import com.racion.app.ui.theme.WeeklyBarChart

/**
 * Pantalla 07 · Informe semanal.
 *
 * Todos los números (promedio, mejor día, racha, donut y barras) salen de
 * `ReportBuilder` a través de `ReportViewModel`; la vista no calcula nada.
 */
class ReportScreen(
    private val container: AppContainer
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ReportContent(container = container, onBack = { navigator.pop() })
    }
}

@Composable
fun ReportContent(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { container.reportViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()
    val report = ui.report

    ScreenContainer {
        BackRow(title = "Informe", onBack = onBack)

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resumen semanal",
                style = AppType.headlineLarge,
                color = Theme.colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = ui.rangeLabel.uppercase(),
                style = AppType.labelSmall,
                color = Theme.colors.onSurfaceVariant
            )
        }

        if (!ui.hasData) {
            EmptyState(
                emoji = "📊",
                title = "Semana sin datos",
                message = "Registrá al menos una comida para ver el promedio y las barras."
            )
        } else {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryPill(
                    value = ui.averageLabel,
                    label = "Promedio/día",
                    background = Theme.colors.inverseSurface,
                    foreground = Theme.colors.inverseOnSurface,
                    modifier = Modifier.weight(1f)
                )
                SummaryPill(
                    value = ui.bestDayLabel,
                    label = "Mejor día",
                    background = Theme.colors.primaryContainer,
                    foreground = Theme.colors.primary,
                    modifier = Modifier.weight(1f)
                )
                SummaryPill(
                    value = ui.streakLabel,
                    label = "Racha activa",
                    background = Theme.colors.tertiaryContainer,
                    foreground = Theme.colors.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                DonutChart(
                    carbsPct = report.macroSplit.carbsPct,
                    proteinPct = report.macroSplit.proteinPct,
                    fatPct = report.macroSplit.fatPct,
                    centerValue = ui.averageLabel,
                    centerLabel = "kcal/día",
                    modifier = Modifier.size(140.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendRow("Carbos ${report.macroSplit.carbsPct}%", Theme.colors.primary)
                    LegendRow("Proteína ${report.macroSplit.proteinPct}%", Theme.colors.secondary)
                    LegendRow("Grasas ${report.macroSplit.fatPct}%", Theme.colors.tertiary)
                }
            }

            Spacer(Modifier.height(28.dp))

            SectionLabel("CALORÍAS POR DÍA")

            SurfaceCard(modifier = Modifier.padding(top = 12.dp)) {
                WeeklyBarChart(
                    days = report.days,
                    goalKcal = report.goalKcal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meta diaria: ${report.goalKcal} kcal",
                        style = AppType.labelSmall,
                        color = Theme.colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Mejor día resaltado",
                        style = AppType.labelSmall,
                        color = Theme.colors.primary
                    )
                }
            }

            if (ui.goalComparison.isNotBlank()) {
                InfoBanner(
                    message = ui.goalComparison,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            Text(
                text = "La semana empieza el lunes y el promedio se calcula solo sobre los días con registros, para no castigar los días que todavía no completaste.",
                style = AppType.bodySmall,
                color = Theme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
