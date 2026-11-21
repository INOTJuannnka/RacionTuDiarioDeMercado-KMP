package com.racion.app.shared.data

import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.models.MealSlot
import com.racion.app.shared.models.Nutrition
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Datos de demostración.
 *
 * Se siembran la primera vez que se abre la app y a partir de ahí son datos reales
 * del usuario: se pueden borrar, editar y sobreviven a reiniciar la app.
 */
object SeedData {

    val featuredProduct = FoodProduct(
        barcode = "LOCAL-AREPAQUESO",
        name = "Arepa de choclo con queso",
        brand = "Típico · Región andina",
        quantityLabel = "1 unidad (90 g)",
        servingGrams = 90.0,
        nutritionPer100g = Nutrition(kcal = 294, carbsG = 31.0, proteinG = 10.0, fatG = 12.0),
        nutriscoreGrade = "d",
        categories = listOf("Almuerzo", "Bebidas"),
        ingredientsText = "Masa de maíz tierno asada, rellena con queso campesino.",
        emoji = "🌽"
    )

    private val arepaChoclo = FoodProduct(
        barcode = "LOCAL-AREPACHOCLO",
        name = "Arepa de choclo",
        brand = "Típico · Región andina",
        quantityLabel = "1 unidad (90 g)",
        servingGrams = 90.0,
        nutritionPer100g = Nutrition(kcal = 233, carbsG = 33.0, proteinG = 10.0, fatG = 12.0),
        categories = listOf("Almuerzo", "Bebidas"),
        emoji = "🌽"
    )

    private val empanada = FoodProduct(
        barcode = "LOCAL-EMPANADACARNE",
        name = "Empanada de carne",
        brand = "Típico · Tolima",
        quantityLabel = "1 unidad (120 g)",
        servingGrams = 120.0,
        nutritionPer100g = Nutrition(kcal = 192, carbsG = 22.0, proteinG = 9.0, fatG = 9.0),
        categories = listOf("Frituras"),
        emoji = "🥟"
    )

    private val bandejaPaisa = FoodProduct(
        barcode = "LOCAL-BANDEJAPAISA",
        name = "Bandeja paisa",
        brand = "Típico · Antioquia",
        quantityLabel = "1 porción (450 g)",
        servingGrams = 450.0,
        nutritionPer100g = Nutrition(kcal = 191, carbsG = 16.0, proteinG = 12.0, fatG = 12.0),
        categories = listOf("Almuerzo"),
        emoji = "🍛"
    )

    private val patacon = FoodProduct(
        barcode = "LOCAL-PATACONHOGAO",
        name = "Patacón con hogao",
        brand = "Típico · Pacífico",
        quantityLabel = "1 porción (220 g)",
        servingGrams = 220.0,
        nutritionPer100g = Nutrition(kcal = 141, carbsG = 21.0, proteinG = 3.0, fatG = 5.0),
        categories = listOf("Frituras"),
        emoji = "🍠"
    )

    private val avena = FoodProduct(
        barcode = "7801234567890",
        name = "Avena Alpina 200 ml",
        brand = "Alpina",
        quantityLabel = "200 ml",
        servingGrams = 200.0,
        nutritionPer100g = Nutrition(
            kcal = 70,
            carbsG = 10.0,
            proteinG = 2.5,
            fatG = 1.8,
            sugarsG = 8.0,
            fiberG = 0.8,
            sodiumG = 0.06
        ),
        nutriscoreGrade = "c",
        categories = listOf("Bebidas", "Lácteos"),
        emoji = "🍞"
    )

    private val sancocho = FoodProduct(
        barcode = "LOCAL-SANCOCHOGALLINA",
        name = "Sancocho de gallina",
        brand = "Típico · Región andina",
        quantityLabel = "1 plato (400 g)",
        servingGrams = 400.0,
        nutritionPer100g = Nutrition(kcal = 120, carbsG = 12.0, proteinG = 12.0, fatG = 3.0),
        categories = listOf("Sopas"),
        emoji = "🍲"
    )

    private val jugoMango = FoodProduct(
        barcode = "LOCAL-JUGOMANGOBICHE",
        name = "Jugo de mango biche",
        brand = "Típico · Región andina",
        quantityLabel = "1 vaso (330 ml)",
        servingGrams = 330.0,
        nutritionPer100g = Nutrition(
            kcal = 39,
            carbsG = 9.0,
            proteinG = 0.4,
            fatG = 0.1,
            sugarsG = 8.5
        ),
        categories = listOf("Bebidas"),
        emoji = "🥭"
    )

    /** Catálogo offline completo. */
    val products: List<FoodProduct> = listOf(
        featuredProduct, arepaChoclo, empanada, bandejaPaisa,
        patacon, avena, sancocho, jugoMango
    )

    /**
     * Hoy quedan las tres comidas del diseño original y los seis días previos se
     * rellenan para que el Informe semanal tenga datos reales que graficar.
     */
    fun demoEntries(
        nowMillis: Long = AppFormat.nowEpochMillis(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): List<DiaryEntry> {
        val todayEpochDay = AppFormat.epochDayOf(AppFormat.localDate(nowMillis, timeZone))

        fun stamp(daysAgo: Int, hour: Int, minute: Int): Long {
            val date = AppFormat.dateOfEpochDay(todayEpochDay - daysAgo)
            return LocalDateTime(date, LocalTime(hour, minute)).toInstant(timeZone).toEpochMilliseconds()
        }

        val entries = buildList {
            // Hoy
            add(DiaryEntry(product = arepaChoclo, servings = 1, mealSlot = MealSlot.DESAYUNO, loggedAtEpochMillis = stamp(0, 7, 20)))
            add(DiaryEntry(product = sancocho, servings = 1, mealSlot = MealSlot.ALMUERZO, loggedAtEpochMillis = stamp(0, 12, 45)))
            add(DiaryEntry(product = jugoMango, servings = 1, mealSlot = MealSlot.SNACK, loggedAtEpochMillis = stamp(0, 15, 10)))

            // Historial de la semana
            add(DiaryEntry(product = avena, servings = 1, mealSlot = MealSlot.DESAYUNO, loggedAtEpochMillis = stamp(1, 7, 5)))
            add(DiaryEntry(product = bandejaPaisa, servings = 1, mealSlot = MealSlot.ALMUERZO, loggedAtEpochMillis = stamp(1, 13, 0)))
            add(DiaryEntry(product = sancocho, servings = 1, mealSlot = MealSlot.ALMUERZO, loggedAtEpochMillis = stamp(2, 12, 30)))
            add(DiaryEntry(product = jugoMango, servings = 1, mealSlot = MealSlot.SNACK, loggedAtEpochMillis = stamp(2, 16, 0)))
            add(DiaryEntry(product = arepaChoclo, servings = 1, mealSlot = MealSlot.DESAYUNO, loggedAtEpochMillis = stamp(3, 7, 40)))
            add(DiaryEntry(product = empanada, servings = 2, mealSlot = MealSlot.SNACK, loggedAtEpochMillis = stamp(3, 17, 15)))
            add(DiaryEntry(product = patacon, servings = 1, mealSlot = MealSlot.ALMUERZO, loggedAtEpochMillis = stamp(4, 12, 50)))
            add(DiaryEntry(product = bandejaPaisa, servings = 1, mealSlot = MealSlot.ALMUERZO, loggedAtEpochMillis = stamp(5, 13, 10)))
            add(DiaryEntry(product = jugoMango, servings = 1, mealSlot = MealSlot.SNACK, loggedAtEpochMillis = stamp(5, 16, 20)))
            add(DiaryEntry(product = arepaChoclo, servings = 2, mealSlot = MealSlot.DESAYUNO, loggedAtEpochMillis = stamp(6, 8, 0)))
        }

        return entries.sortedBy { it.loggedAtEpochMillis }
    }

    /**
     * Versiones anteriores sembraban [demoEntries] en cada instalación. Se reconoce una
     * entrada de ejemplo por producto, porciones, comida y hora exacta (p. ej. sancocho
     * 1 porción, almuerzo, 12:45), que es lo único que la distingue de un registro real.
     */
    fun isLegacyDemo(entry: DiaryEntry, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean =
        demoSignature(entry, timeZone) in demoEntries(timeZone = timeZone).map { demoSignature(it, timeZone) }

    private fun demoSignature(entry: DiaryEntry, timeZone: TimeZone) =
        "${entry.product.barcode}|${entry.servings}|${entry.mealSlot}|" +
            AppFormat.localDateTime(entry.loggedAtEpochMillis, timeZone).time
}
