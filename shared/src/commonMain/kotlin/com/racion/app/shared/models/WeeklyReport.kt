package com.racion.app.shared.models

import kotlinx.datetime.LocalDate

/** Informe semanal calculado a partir de las entradas reales del diario. */
data class WeeklyReport(
    val weekRangeLabel: String,
    val averageKcalPerDay: Int,
    val bestDayLabel: String?,
    val activeStreakDays: Int,
    val macroSplit: MacroSplit,
    val days: List<DayTotal>,
    val goalKcal: Int
) {
    val hasData: Boolean get() = days.any { it.kcal > 0 }

    companion object {
        val EMPTY = WeeklyReport(
            weekRangeLabel = "",
            averageKcalPerDay = 0,
            bestDayLabel = null,
            activeStreakDays = 0,
            macroSplit = MacroSplit(0, 0, 0),
            days = emptyList(),
            goalKcal = NutritionGoals.DEFAULT.kcalPerDay
        )
    }
}

/** Una barra del gráfico semanal. */
data class DayTotal(
    val label: String,
    val kcal: Int,
    val epochDay: Long,
    val isHighlighted: Boolean = false
) {
    val date: LocalDate get() = LocalDate.fromEpochDays(epochDay.toInt())
}
