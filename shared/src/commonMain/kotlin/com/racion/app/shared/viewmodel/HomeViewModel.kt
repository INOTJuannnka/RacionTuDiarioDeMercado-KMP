package com.racion.app.shared.viewmodel

import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.models.DailySummary
import com.racion.app.shared.models.UserProfile
import com.racion.app.shared.state.AppState
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs
import kotlin.math.roundToInt

/** Una fila de comida ya formateada; la vista solo la dibuja. */
data class MealRowUi(
    val id: String,
    val emoji: String,
    val name: String,
    val meta: String,
    val kcal: Int
)

/** Todo lo que la pantalla "Inicio" necesita, ya agregado. */
data class HomeUiState(
    val summary: DailySummary = DailySummary.EMPTY,
    val profile: UserProfile = UserProfile.LOCAL_DEFAULT,
    val greeting: String = "Hola!",
    val caloriesLeftLabel: String = "",
    val isOverGoal: Boolean = false,
    val carbsLabel: String = "0 g",
    val proteinLabel: String = "0 g",
    val fatLabel: String = "0 g",
    val rows: List<MealRowUi> = emptyList()
)

/**
 * Prepara el `DailySummary` de hoy. La vista no suma calorías ni filtra entradas:
 * solo dibuja lo que este ViewModel expone.
 */
class HomeViewModel(
    private val store: AppStore
) : RacionViewModel() {

    val state: StateFlow<HomeUiState> = store.state
        .map(::buildState)
        .stateIn(scope, SharingStarted.Eagerly, buildState(store.state.value))

    fun refresh() {
        // El flujo ya reacciona a cada cambio del diario. Se mantiene el método
        // para que la pantalla pueda pedir un refresco explícito sin conocer el flujo.
    }

    private fun buildState(snapshot: AppState): HomeUiState {
        val summary = store.summary()
        // Primer nombre de la cuenta (Google) o la parte del correo antes de "@"; el invitado no tiene nombre.
        val session = snapshot.session
        val name = session?.displayName?.trim()?.substringBefore(' ')?.takeIf { it.isNotEmpty() }
            ?: session?.email?.substringBefore('@').orEmpty()
        val remaining = summary.remainingKcal

        return HomeUiState(
            summary = summary,
            profile = snapshot.profile,
            greeting = name.ifEmpty { "Hola!" },
            caloriesLeftLabel = if (remaining < 0) {
                "${abs(remaining)} kcal por encima de la meta"
            } else {
                "de ${summary.goalKcal} kcal · quedan $remaining"
            },
            isOverGoal = remaining < 0,
            carbsLabel = "${summary.consumed.carbsG.roundToInt()} g",
            proteinLabel = "${summary.consumed.proteinG.roundToInt()} g",
            fatLabel = "${summary.consumed.fatG.roundToInt()} g",
            rows = summary.entries.map { entry ->
                MealRowUi(
                    id = entry.id,
                    emoji = entry.product.emoji,
                    name = entry.product.name,
                    meta = AppFormat.mealRowMeta(entry),
                    kcal = entry.totalNutrition.kcal
                )
            }
        )
    }
}
