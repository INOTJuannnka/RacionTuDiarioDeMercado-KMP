package com.racion.app.shared.viewmodel

import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.models.MealSlot
import com.racion.app.shared.models.Nutrition
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

/** Porciones y comida elegidas antes de guardar. */
data class ConfirmUiState(
    val servings: Int = 1,
    val mealSlot: MealSlot = MealSlot.ALMUERZO,
    val total: Nutrition = Nutrition.ZERO,
    val gramsLabel: String = "",
    val originLabel: String = "",
    val ingredients: String = "",
    val nutriscoreLabel: String? = null
)

private data class ConfirmLocal(
    val servings: Int = 1,
    val mealSlot: MealSlot = MealSlot.ALMUERZO
)

/**
 * Aritmética de "Confirmar".
 *
 * El número que se muestra y el que se guarda salen del MISMO método
 * (`FoodProduct.nutritionForUnits`), así no puede repetirse el bug de doble
 * redondeo del proyecto original.
 */
class ConfirmViewModel(
    val product: FoodProduct,
    private val store: AppStore
) : RacionViewModel() {

    private val local = MutableStateFlow(ConfirmLocal())

    val state: StateFlow<ConfirmUiState> = local
        .map { draft -> buildState(draft) }
        .stateIn(scope, SharingStarted.Eagerly, buildState(local.value))

    val mealSlots: List<MealSlot> get() = MealSlot.entries

    fun setServings(value: Int) {
        local.update { it.copy(servings = value.coerceIn(1, 20)) }
    }

    fun selectMealSlot(slot: MealSlot) {
        local.update { it.copy(mealSlot = slot) }
    }

    /** Agrega la entrada al diario y la devuelve. */
    fun add(): DiaryEntry {
        val draft = local.value
        val entry = DiaryEntry(
            product = product,
            servings = draft.servings,
            mealSlot = draft.mealSlot
        )
        store.addEntry(entry)
        return entry
    }

    private fun buildState(draft: ConfirmLocal): ConfirmUiState {
        val grams = (product.servingGrams ?: FoodProduct.DEFAULT_SERVING_GRAMS) * draft.servings
        return ConfirmUiState(
            servings = draft.servings,
            mealSlot = draft.mealSlot,
            total = product.nutritionForUnits(draft.servings),
            gramsLabel = "≈ ${grams.roundToInt()} g",
            originLabel = product.originLabel,
            ingredients = product.ingredientsText ?: "Sin lista de ingredientes declarada.",
            nutriscoreLabel = product.nutriscoreGrade
                ?.takeIf { it.isNotBlank() }
                ?.let { "NUTRI-SCORE ${it.uppercase()}" }
        )
    }
}
