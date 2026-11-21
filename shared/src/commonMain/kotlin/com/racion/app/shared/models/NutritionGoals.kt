package com.racion.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Días de la semana con su etiqueta de una letra en español. */
@Serializable
enum class Weekday(
    val short: String,
    val long: String
) {
    @SerialName("monday")
    MONDAY("L", "Lunes"),

    @SerialName("tuesday")
    TUESDAY("M", "Martes"),

    @SerialName("wednesday")
    WEDNESDAY("X", "Miércoles"),

    @SerialName("thursday")
    THURSDAY("J", "Jueves"),

    @SerialName("friday")
    FRIDAY("V", "Viernes"),

    @SerialName("saturday")
    SATURDAY("S", "Sábado"),

    @SerialName("sunday")
    SUNDAY("D", "Domingo");

    companion object {
        /** El ordinal ISO de kotlinx-datetime va de lunes (0) a domingo (6). */
        fun fromIsoOrdinal(ordinal: Int): Weekday =
            entries[ordinal.coerceIn(0, entries.lastIndex)]
    }
}

/**
 * Distribución de macros en porcentajes enteros.
 * No se valida que sumen 100: los redondeos de una fuente externa producen 101.
 */
@Serializable
data class MacroSplit(
    val carbsPct: Int,
    val proteinPct: Int,
    val fatPct: Int
) {
    companion object {
        val DEFAULT = MacroSplit(carbsPct = 45, proteinPct = 25, fatPct = 30)
    }
}

/**
 * Objetivos del usuario, editables desde "Metas".
 * Los límites de los controles viven aquí, no en la pantalla.
 */
@Serializable
data class NutritionGoals(
    val targetWeightKg: Double = 63.0,
    val currentWeightKg: Double = 68.0,
    val kcalPerDay: Int = 1900,
    val activeDays: Set<Weekday> = Weekday.entries.toSet(),
    val macroSplit: MacroSplit = MacroSplit.DEFAULT
) {
    companion object {
        val WEIGHT_RANGE: ClosedFloatingPointRange<Double> = 55.0..80.0
        val KCAL_RANGE: IntRange = 1400..2600
        val DEFAULT = NutritionGoals()
    }
}
