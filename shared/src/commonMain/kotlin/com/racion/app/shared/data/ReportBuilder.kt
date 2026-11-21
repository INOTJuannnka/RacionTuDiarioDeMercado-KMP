package com.racion.app.shared.data

import com.racion.app.shared.models.DayTotal
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.Weekday
import com.racion.app.shared.models.WeeklyReport
import kotlinx.datetime.TimeZone

/**
 * Agregación real del informe semanal.
 *
 * A diferencia del proyecto Android original (que mostraba números escritos a mano
 * en PreviewData), el promedio, el mejor día, la racha y las barras se calculan
 * desde las entradas reales del diario.
 *
 * La semana empieza en lunes sin importar la región del dispositivo.
 */
object ReportBuilder {

    fun weeklyReport(
        entries: List<DiaryEntry>,
        goals: NutritionGoals,
        nowMillis: Long = AppFormat.nowEpochMillis(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): WeeklyReport {
        val today = AppFormat.localDate(nowMillis, timeZone)
        val mondayEpochDay = AppFormat.epochDayOf(today) - today.dayOfWeek.ordinal

        val kcalByDay = entries.groupBy {
            AppFormat.epochDayOf(AppFormat.localDate(it.loggedAtEpochMillis, timeZone))
        }.mapValues { (_, dayEntries) -> dayEntries.sumOf { it.totalNutrition.kcal } }

        val days = (0..6).map { offset ->
            val epochDay = mondayEpochDay + offset
            val date = AppFormat.dateOfEpochDay(epochDay)
            val weekday = Weekday.fromIsoOrdinal(date.dayOfWeek.ordinal)
            DayTotal(
                label = weekday.short,
                kcal = kcalByDay[epochDay] ?: 0,
                epochDay = epochDay
            )
        }

        val maxKcal = days.maxOfOrNull { it.kcal } ?: 0
        val bestIndex = if (maxKcal > 0) days.indexOfFirst { it.kcal == maxKcal } else -1
        val markedDays = if (bestIndex >= 0) {
            days.mapIndexed { index, day -> if (index == bestIndex) day.copy(isHighlighted = true) else day }
        } else {
            days
        }

        // El promedio se calcula solo sobre los días CON datos. Dividir entre 7
        // cuando solo hay dos días registrados haría parecer que come la mitad.
        val loggedDays = markedDays.filter { it.kcal > 0 }
        val average = if (loggedDays.isEmpty()) 0 else loggedDays.sumOf { it.kcal } / loggedDays.size

        return WeeklyReport(
            weekRangeLabel = weekRangeLabel(mondayEpochDay, timeZone),
            averageKcalPerDay = average,
            bestDayLabel = if (maxKcal > 0) markedDays[bestIndex].label else null,
            activeStreakDays = activeStreak(entries, nowMillis, timeZone),
            macroSplit = goals.macroSplit,
            days = markedDays,
            goalKcal = goals.kcalPerDay
        )
    }

    /**
     * Días consecutivos con al menos una entrada, contando hacia atrás desde hoy.
     * Si hoy todavía no se registró nada, la racha cuenta desde ayer.
     */
    fun activeStreak(
        entries: List<DiaryEntry>,
        nowMillis: Long = AppFormat.nowEpochMillis(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Int {
        val daysWithEntries = entries
            .map { AppFormat.epochDayOf(AppFormat.localDate(it.loggedAtEpochMillis, timeZone)) }
            .toSet()

        var cursor = AppFormat.epochDayOf(AppFormat.localDate(nowMillis, timeZone))
        if (cursor !in daysWithEntries) cursor -= 1

        var streak = 0
        // Tope de seguridad: 10 años. Un reloj roto no debe producir un bucle infinito.
        while (streak < 3650 && cursor in daysWithEntries) {
            streak += 1
            cursor -= 1
        }
        return streak
    }

    /** "24–30 AGO" o "29 SET–5 OCT" si el rango cruza de mes. */
    private fun weekRangeLabel(
        mondayEpochDay: Long,
        timeZone: TimeZone
    ): String {
        val start = AppFormat.dateOfEpochDay(mondayEpochDay)
        val end = AppFormat.dateOfEpochDay(mondayEpochDay + 6)
        val startMonth = AppFormat.shortMonth(start).uppercase()
        val endMonth = AppFormat.shortMonth(end).uppercase()

        return if (startMonth == endMonth) {
            "${start.dayOfMonth}–${end.dayOfMonth} $endMonth"
        } else {
            "${start.dayOfMonth} $startMonth–${end.dayOfMonth} $endMonth"
        }
    }
}
