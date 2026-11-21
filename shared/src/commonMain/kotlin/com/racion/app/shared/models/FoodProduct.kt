package com.racion.app.shared.models

import kotlinx.serialization.Serializable

/**
 * Un alimento tal como lo entiende la app.
 *
 * La nutrición se guarda SIEMPRE por 100 g. Es la única normalización que funciona
 * para productos cuya porción es "1 unidad", "1 rebanada", "250 ml" o que no
 * declaran porción.
 */
@Serializable
data class FoodProduct(
    val barcode: String,
    val name: String,
    val brand: String? = null,
    val quantityLabel: String? = null,
    val servingGrams: Double? = null,
    val nutritionPer100g: Nutrition,
    val imageURL: String? = null,
    val nutriscoreGrade: String? = null,
    val categories: List<String> = emptyList(),
    val ingredientsText: String? = null,
    val emoji: String = "🍽"
) {
    /** La identidad estable es el código de barras. */
    val id: String get() = barcode

    fun nutritionForGrams(grams: Double): Nutrition =
        nutritionPer100g.scaled(grams / 100.0)

    /**
     * Nutrición para [units] porciones, redondeando una sola vez.
     * Es el método canónico que usan Confirmar y el guardado.
     */
    fun nutritionForUnits(units: Int): Nutrition =
        nutritionForGrams((servingGrams ?: DEFAULT_SERVING_GRAMS) * units)

    /** Marca si existe; si no, categorías; si no, un texto honesto. */
    val originLabel: String
        get() {
            val cleanBrand = brand?.trim().orEmpty()
            if (cleanBrand.isNotEmpty()) return cleanBrand
            if (categories.isNotEmpty()) return categories.joinToString(" · ")
            return "Sin origen declarado"
        }

    companion object {
        /**
         * Gramos asumidos para una "unidad" cuando el fabricante no declara porción.
         * 100 g es la única elección honesta porque es la base de [nutritionPer100g].
         */
        const val DEFAULT_SERVING_GRAMS: Double = 100.0

        /**
         * Producto cargado a mano cuando el código no está en Open Food Facts. La persona
         * ve la etiqueta "por porción": se guarda como porción de 100 g con esos valores,
         * así 1 porción en Confirmar muestra exactamente lo que escribió.
         * Devuelve `null` si falta el nombre o algún número no es válido.
         */
        fun manual(barcode: String, name: String, kcal: String, carbs: String, protein: String, fat: String): FoodProduct? {
            val cleanName = name.trim().takeIf { it.isNotEmpty() } ?: return null
            fun number(raw: String) = raw.trim().replace(',', '.').ifEmpty { "0" }.toDoubleOrNull()?.takeIf { it >= 0 }
            return FoodProduct(
                barcode = barcode.trim(),
                name = cleanName,
                brand = "Cargado a mano",
                servingGrams = DEFAULT_SERVING_GRAMS,
                nutritionPer100g = Nutrition(
                    kcal = number(kcal)?.toInt() ?: return null,
                    carbsG = number(carbs) ?: return null,
                    proteinG = number(protein) ?: return null,
                    fatG = number(fat) ?: return null
                )
            )
        }
    }
}
