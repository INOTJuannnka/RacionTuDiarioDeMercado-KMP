package com.racion.app.shared.models

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * Valor nutricional inmutable.
 *
 * Todo va en gramos salvo [kcal], que son kilocalorías. Esta es la única clase que
 * hace aritmética de nutrientes, de modo que el Inicio, el Diario y el Informe no
 * pueden contradecirse entre sí.
 */
@Serializable
data class Nutrition(
    val kcal: Int = 0,
    val carbsG: Double = 0.0,
    val proteinG: Double = 0.0,
    val fatG: Double = 0.0,
    val sugarsG: Double = 0.0,
    val fiberG: Double = 0.0,
    val sodiumG: Double = 0.0
) {
    /**
     * Multiplica todos los valores por [factor].
     *
     * Las kcal se redondean UNA sola vez, al final. Redondear la porción y después
     * multiplicar produce el bug clásico de "530 kcal mostradas / 529 guardadas".
     */
    fun scaled(factor: Double): Nutrition {
        if (factor == 1.0) return this
        return Nutrition(
            kcal = (kcal * factor).roundToInt(),
            carbsG = carbsG * factor,
            proteinG = proteinG * factor,
            fatG = fatG * factor,
            sugarsG = sugarsG * factor,
            fiberG = fiberG * factor,
            sodiumG = sodiumG * factor
        )
    }

    operator fun plus(other: Nutrition): Nutrition = Nutrition(
        kcal = kcal + other.kcal,
        carbsG = carbsG + other.carbsG,
        proteinG = proteinG + other.proteinG,
        fatG = fatG + other.fatG,
        sugarsG = sugarsG + other.sugarsG,
        fiberG = fiberG + other.fiberG,
        sodiumG = sodiumG + other.sodiumG
    )

    companion object {
        val ZERO = Nutrition()

        fun total(values: Iterable<Nutrition>): Nutrition =
            values.fold(ZERO) { acc, item -> acc + item }
    }
}
