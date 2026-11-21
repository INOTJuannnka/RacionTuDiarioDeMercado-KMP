package com.racion.app.shared.models

/** Todo lo que la pantalla "Inicio" necesita de un día, ya agregado. */
data class DailySummary(
    val dateLabel: String,
    val consumed: Nutrition,
    val goalKcal: Int,
    val entries: List<DiaryEntry>
) {
    /** Puede ser negativo: el exceso se muestra, no se recorta a cero. */
    val remainingKcal: Int get() = goalKcal - consumed.kcal

    /** Progreso 0...1 (recortado solo para dibujar la barra). */
    val progress: Double
        get() = if (goalKcal <= 0) 0.0 else (consumed.kcal.toDouble() / goalKcal).coerceIn(0.0, 1.0)

    /** Entradas agrupadas por comida, en el orden natural del día. */
    val entriesBySlot: List<MealGroup>
        get() = MealSlot.entries.mapNotNull { slot ->
            val filtered = entries.filter { it.mealSlot == slot }
            if (filtered.isEmpty()) null else MealGroup(slot, filtered)
        }

    companion object {
        val EMPTY = DailySummary(
            dateLabel = "",
            consumed = Nutrition.ZERO,
            goalKcal = NutritionGoals.DEFAULT.kcalPerDay,
            entries = emptyList()
        )
    }
}

data class MealGroup(
    val slot: MealSlot,
    val entries: List<DiaryEntry>
)
