package com.racion.app.shared.network.dto

import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.models.Nutrition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

/**
 * DTO espejo del JSON de Open Food Facts.
 *
 * Esta es la ÚNICA capa que sabe que la API escribe `energy-kcal_100g` con guion y
 * `carbohydrates_100g` en plural. El dominio (`FoodProduct`, `Nutrition`) no se
 * contamina con nombres de campos remotos.
 *
 * Todos los campos son opcionales: la base es colaborativa y hay productos sin
 * marca, sin porción o sin tabla nutricional.
 */

/** Envelope de `GET /api/v2/product/{barcode}.json`. */
@Serializable
data class OffProductResponse(
    val status: Int? = null,
    val product: OffProduct? = null
)

/** Envelope de `search.openfoodfacts.org/search`. */
@Serializable
data class OffSearchResponse(
    val count: Int? = null,
    val hits: List<OffProduct>? = null
)

/** Un producto tal como lo entrega cualquiera de los dos endpoints. */
@Serializable
data class OffProduct(
    val code: String? = null,
    @SerialName("product_name") val productName: String? = null,
    /** Texto "A, B" en `/api/v2/product`; lista `["A","B"]` en el buscador. */
    val brands: JsonElement? = null,
    val quantity: String? = null,
    @SerialName("serving_quantity") val servingQuantity: Double? = null,
    val categories: String? = null,
    @SerialName("ingredients_text") val ingredientsText: String? = null,
    @SerialName("nutrition_grades") val nutritionGrades: String? = null,
    @SerialName("image_front_url") val imageFrontUrl: String? = null,
    val nutriments: OffNutriments? = null
)

/**
 * Sub-objeto `nutriments`, siempre por 100 g.
 *
 * La asimetría de la API es real: energía con guion (`energy-kcal_100g`), macros en
 * plural y con guion bajo (`carbohydrates_100g`, `proteins_100g`) y grasa en
 * singular (`fat_100g`).
 */
@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null,
    @SerialName("sodium_100g") val sodium100g: Double? = null
)

/**
 * Convierte el DTO a dominio. Devuelve `null` cuando no hay un nombre utilizable:
 * una fila sin nombre no se puede mostrar en una lista.
 */
fun OffProduct.toFoodProduct(): FoodProduct? {
    val cleanName = productName?.trim().orEmpty()
    if (cleanName.isEmpty()) return null

    val parsedCategories = categories.orEmpty()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val brandText = when (val raw = brands) {
        is JsonArray -> raw.firstOrNull()?.jsonPrimitive?.contentOrNull
        is JsonPrimitive -> raw.contentOrNull
        else -> null
    }
    val firstBrand = brandText.orEmpty()
        .split(',')
        .firstOrNull()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    return FoodProduct(
        barcode = code?.trim()?.takeIf { it.isNotEmpty() }
            ?: "OFF-${cleanName.lowercase().hashCode()}",
        name = cleanName,
        brand = firstBrand,
        quantityLabel = quantity?.trim()?.takeIf { it.isNotEmpty() },
        servingGrams = servingQuantity?.takeIf { it > 0.0 },
        nutritionPer100g = nutrition(),
        imageURL = imageFrontUrl?.trim()?.takeIf { it.isNotEmpty() },
        nutriscoreGrade = nutritionGrades?.trim()?.lowercase()?.takeIf { it.isNotEmpty() },
        categories = parsedCategories,
        ingredientsText = ingredientsText?.trim()?.takeIf { it.isNotEmpty() }
    )
}

/**
 * Un producto sin `nutriments` se mapea a cero en lugar de descartarse: igual se
 * puede registrar a mano y el usuario ve que la fuente no declaró datos.
 */
private fun OffProduct.nutrition(): Nutrition {
    val values = nutriments ?: return Nutrition.ZERO
    return Nutrition(
        kcal = (values.energyKcal100g ?: 0.0).roundToInt(),
        carbsG = values.carbohydrates100g ?: 0.0,
        proteinG = values.proteins100g ?: 0.0,
        fatG = values.fat100g ?: 0.0,
        sugarsG = values.sugars100g ?: 0.0,
        fiberG = values.fiber100g ?: 0.0,
        sodiumG = values.sodium100g ?: 0.0
    )
}
