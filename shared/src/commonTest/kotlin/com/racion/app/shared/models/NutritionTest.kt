package com.racion.app.shared.models

import com.racion.app.shared.data.SeedData
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionTest {

    @Test
    fun scalingRoundsKcalOnlyOnce() {
        val base = Nutrition(kcal = 233, carbsG = 33.0, proteinG = 10.0, fatG = 12.0)

        val scaled = base.scaled(1.8)

        // 233 * 1.8 = 419.4: se redondea una sola vez, al final.
        assertEquals(419, scaled.kcal)
        assertEquals(59.4, scaled.carbsG, 0.0001)
        assertEquals(18.0, scaled.proteinG, 0.0001)
        assertEquals(21.6, scaled.fatG, 0.0001)
    }

    @Test
    fun nutritionForUnitsMatchesSingleRounding() {
        val product = FoodProduct(
            barcode = "TEST-1",
            name = "Arepa de prueba",
            servingGrams = 90.0,
            nutritionPer100g = Nutrition(kcal = 233, carbsG = 33.0, proteinG = 10.0, fatG = 12.0)
        )

        // 233 kcal/100 g * 90 g * 3 porciones = 629.1 -> 629.
        assertEquals(629, product.nutritionForUnits(3).kcal)
        assertEquals(629, product.nutritionForGrams(270.0).kcal)
    }

    @Test
    fun totalAddsEveryMacro() {
        val total = Nutrition.total(
            listOf(
                Nutrition(kcal = 100, carbsG = 10.0, proteinG = 5.0, fatG = 2.0),
                Nutrition(kcal = 250, carbsG = 20.0, proteinG = 8.0, fatG = 9.0)
            )
        )

        assertEquals(350, total.kcal)
        assertEquals(30.0, total.carbsG, 0.0001)
        assertEquals(13.0, total.proteinG, 0.0001)
        assertEquals(11.0, total.fatG, 0.0001)
    }

    @Test
    fun demoProductKeepsSingleRoundingContract() {
        val featured = SeedData.featuredProduct

        // El producto destacado tiene 294 kcal/100 g y porción de 90 g.
        assertEquals(265, featured.nutritionForUnits(1).kcal)
        assertEquals(794, featured.nutritionForUnits(3).kcal)
    }
}
