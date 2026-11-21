package com.racion.app.shared.viewmodel

import com.racion.app.shared.models.WeeklyReport
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs

/** Informe semanal ya agregado, con las etiquetas que dibuja la vista. */
data class ReportUiState(
    val report: WeeklyReport = WeeklyReport.EMPTY,
    val averageLabel: String = "0",
    val bestDayLabel: String = "—",
    val streakLabel: String = "0",
    val rangeLabel: String = "",
    val goalComparison: String = ""
) {
    val hasData: Boolean get() = report.days.any { it.kcal > 0 }
}

/**
 * Todos los números del informe (promedio, mejor día, racha, barras) salen de las
 * entradas reales del diario a través de `ReportBuilder`.
 */
class ReportViewModel(
    private val store: AppStore
) : RacionViewModel() {

    val state: StateFlow<ReportUiState> = store.state
        .map { buildState() }
        .stateIn(scope, SharingStarted.Eagerly, buildState())

    fun refresh() {
        // El flujo ya reacciona a cambios del diario y de las metas.
    }

    private fun buildState(): ReportUiState {
        val report = store.weeklyReport()
        return ReportUiState(
            report = report,
            averageLabel = report.averageKcalPerDay.toString(),
            bestDayLabel = report.bestDayLabel ?: "—",
            streakLabel = report.activeStreakDays.toString(),
            rangeLabel = report.weekRangeLabel,
            goalComparison = goalComparison(report)
        )
    }

    private fun goalComparison(report: WeeklyReport): String {
        if (report.goalKcal <= 0) return ""
        val diff = report.averageKcalPerDay - report.goalKcal
        return when {
            diff == 0 -> "El promedio coincide con tu meta de ${report.goalKcal} kcal."
            diff < 0 -> "El promedio está ${abs(diff)} kcal por debajo de tu meta."
            else -> "El promedio está $diff kcal por encima de tu meta."
        }
    }
}
