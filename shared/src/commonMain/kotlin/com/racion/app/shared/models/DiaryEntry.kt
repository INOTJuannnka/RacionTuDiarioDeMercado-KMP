package com.racion.app.shared.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

/** A qué comida del día pertenece una entrada. */
@Serializable
enum class MealSlot(
    val label: String,
    val emoji: String
) {
    @SerialName("desayuno")
    DESAYUNO("Desayuno", "☕️"),

    @SerialName("almuerzo")
    ALMUERZO("Almuerzo", "🍲"),

    @SerialName("snack")
    SNACK("Snack", "🍎"),

    @SerialName("cena")
    CENA("Cena", "🌙");

    companion object {
        /** Comida sugerida según la hora local. */
        fun defaultForHour(hour: Int): MealSlot = when (hour) {
            in 5..10 -> DESAYUNO
            in 11..15 -> ALMUERZO
            in 16..18 -> SNACK
            else -> CENA
        }
    }
}

/**
 * Un evento alimentario: "comí N porciones de este producto, en esta comida, a esta hora".
 *
 * [totalNutrition] se guarda ya calculado para que el resumen del día sea una lectura
 * directa y no una sumatoria de N documentos.
 */
@Serializable
data class DiaryEntry(
    val id: String = newEntryId(),
    val product: FoodProduct,
    val servings: Int,
    val mealSlot: MealSlot,
    val loggedAtEpochMillis: Long = Clock.System.now().toEpochMilliseconds(),
    val totalNutrition: Nutrition = product.nutritionForUnits(servings)
) {
    val loggedAt: Instant get() = Instant.fromEpochMilliseconds(loggedAtEpochMillis)

    companion object {
        /** Id estable generado en el cliente, sin dependencias de plataforma. */
        fun newEntryId(): String =
            "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(100_000, 999_999)}"
    }
}

private fun newEntryId(): String = DiaryEntry.newEntryId()
