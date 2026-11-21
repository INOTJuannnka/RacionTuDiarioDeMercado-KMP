package com.racion.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/** Días de la semana con su etiqueta de una letra en español. */
@Serializable
// La versión SwiftUI guardó en Firestore el nombre de la constante ("MONDAY"): se acepta al leer.
@OptIn(ExperimentalSerializationApi::class)
enum class Weekday(
    val short: String,
    val long: String
) {
    @SerialName("monday")
    @JsonNames("MONDAY")
    MONDAY("L", "Lunes"),

    @SerialName("tuesday")
    @JsonNames("TUESDAY")
    TUESDAY("M", "Martes"),

    @SerialName("wednesday")
    @JsonNames("WEDNESDAY")
    WEDNESDAY("X", "Miércoles"),

    @SerialName("thursday")
    @JsonNames("THURSDAY")
    THURSDAY("J", "Jueves"),

    @SerialName("friday")
    @JsonNames("FRIDAY")
    FRIDAY("V", "Viernes"),

    @SerialName("saturday")
    @JsonNames("SATURDAY")
    SATURDAY("S", "Sábado"),

    @SerialName("sunday")
    @JsonNames("SUNDAY")
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
