package com.racion.app.shared.data

import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.models.MealSlot
import com.racion.app.shared.models.Nutrition
import com.racion.app.shared.models.NutritionGoals
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReportBuilderTest {

    private val timeZone = TimeZone.UTC

    /** Martes 10 de noviembre de 2026 a mediodía. */
    private val now = LocalDateTime(2026, 11, 10, 12, 0)
        .toInstant(timeZone)
        .toEpochMilliseconds()

    private val product = FoodProduct(
        barcode = "TEST-REPORT",
        name = "Producto de prueba",
        servingGrams = 100.0,
        nutritionPer100g = Nutrition(kcal = 1000)
    )

    private fun entryAt(day: Int, month: Int, hour: Int, servings: Int = 1): DiaryEntry =
        DiaryEntry(
            product = product,
            servings = servings,
            mealSlot = MealSlot.ALMUERZO,
            loggedAtEpochMillis = LocalDateTime(2026, month, day, hour, 0)
                .toInstant(timeZone)
                .toEpochMilliseconds()
        )

    @Test
    fun reportAlwaysHasSevenDaysStartingOnMonday() {
        val report = ReportBuilder.weeklyReport(
            entries = emptyList(),
            goals = NutritionGoals.DEFAULT,
            nowMillis = now,
            timeZone = timeZone
        )

        assertEquals(7, report.days.size)
        assertEquals("L", report.days.first().label)
        assertEquals("D", report.days.last().label)
        assertEquals(listOf("L", "M", "X", "J", "V", "S", "D"), report.days.map { it.label })
    }

    @Test
    fun averageIgnoresDaysWithoutEntries() {
        val report = ReportBuilder.weeklyReport(
            entries = listOf(
                entryAt(day = 9, month = 11, hour = 8),
                entryAt(day = 10, month = 11, hour = 9)
            ),
            goals = NutritionGoals.DEFAULT,
            nowMillis = now,
            timeZone = timeZone
        )

        assertEquals(2000, report.days.sumOf { it.kcal })
        // 2000 / 2 días con datos, no / 7.
        assertEquals(1000, report.averageKcalPerDay)
    }

    @Test
    fun bestDayIsTheHighestBarAndOnlyOneIsHighlighted() {
        val report = ReportBuilder.weeklyReport(
            entries = listOf(
                entryAt(day = 9, month = 11, hour = 8),
                entryAt(day = 10, month = 11, hour = 9, servings = 2)
            ),
            goals = NutritionGoals.DEFAULT,
            nowMillis = now,
            timeZone = timeZone
        )

        assertEquals("M", report.bestDayLabel)
        assertEquals(1, report.days.count { it.isHighlighted })
        assertEquals(2000, report.days.first { it.isHighlighted }.kcal)
    }

    @Test
    fun streakCountsConsecutiveDaysBackwardsFromToday() {
        val report = ReportBuilder.weeklyReport(
            entries = listOf(
                entryAt(day = 8, month = 11, hour = 8),
                entryAt(day = 9, month = 11, hour = 8),
                entryAt(day = 10, month = 11, hour = 8)
            ),
            goals = NutritionGoals.DEFAULT,
            nowMillis = now,
            timeZone = timeZone
        )

        assertEquals(3, report.activeStreakDays)
    }

    @Test
    fun emptyWeekHasNoBestDayButStillHasARange() {
        val report = ReportBuilder.weeklyReport(
            entries = emptyList(),
            goals = NutritionGoals.DEFAULT,
            nowMillis = now,
            timeZone = timeZone
        )

        assertEquals(0, report.averageKcalPerDay)
        assertEquals(null, report.bestDayLabel)
        assertEquals(0, report.activeStreakDays)
        assertTrue(report.weekRangeLabel.isNotBlank())
        assertNotNull(report.weekRangeLabel)
    }
}
