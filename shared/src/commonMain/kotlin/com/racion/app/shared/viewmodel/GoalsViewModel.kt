package com.racion.app.shared.viewmodel

import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.models.MacroSplit
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.Weekday
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

/** Borrador editable de metas + valores derivados del perfil. */
data class GoalsUiState(
    val targetWeightKg: Double = 63.0,
    val kcalPerDay: Double = 1900.0,
    val activeDays: Set<Weekday> = Weekday.entries.toSet(),
    val remindersEnabled: Boolean = true,
    val macroSplit: MacroSplit = MacroSplit.DEFAULT,
    val currentWeightKg: Double = 68.0,
    val sportFocusTitle: String = "",
    val savedMessage: String? = null
) {
    val kcalLabel: String get() = "${kcalPerDay.roundToInt()} kcal"
    val weightLabel: String get() = AppFormat.kilograms(targetWeightKg)
}

private data class GoalsDraft(
    val targetWeightKg: Double,
    val kcalPerDay: Double,
    val activeDays: Set<Weekday>,
    val remindersEnabled: Boolean,
    val savedMessage: String? = null
)

/**
 * Pantalla "Metas": peso objetivo, meta calórica, días activos, macros y
 * recordatorios. `save()` delega en [AppStore]; la vista no conoce archivos.
 */
class GoalsViewModel(
    private val store: AppStore
) : RacionViewModel() {

    private val local = MutableStateFlow(
        GoalsDraft(
            targetWeightKg = store.state.value.goals.targetWeightKg,
            kcalPerDay = store.state.value.goals.kcalPerDay.toDouble(),
            activeDays = store.state.value.goals.activeDays,
            remindersEnabled = store.state.value.settings.remindersEnabled
        )
    )

    val state: StateFlow<GoalsUiState> = combine(store.state, local) { snapshot, draft ->
        GoalsUiState(
            targetWeightKg = draft.targetWeightKg,
            kcalPerDay = draft.kcalPerDay,
            activeDays = draft.activeDays,
            remindersEnabled = draft.remindersEnabled,
            macroSplit = snapshot.goals.macroSplit,
            currentWeightKg = snapshot.goals.currentWeightKg,
            sportFocusTitle = snapshot.profile.sportFocus.title,
            savedMessage = draft.savedMessage
        )
    }.stateIn(scope, SharingStarted.Eagerly, initialState())

    fun setTargetWeight(value: Double) {
        local.update { it.copy(targetWeightKg = value) }
    }

    fun setKcalPerDay(value: Double) {
        local.update { it.copy(kcalPerDay = value) }
    }

    fun toggleDay(day: Weekday) {
        local.update { draft ->
            val next = draft.activeDays.toMutableSet()
            if (!next.add(day)) next.remove(day)
            draft.copy(activeDays = next)
        }
    }

    fun setReminders(enabled: Boolean) {
        local.update { it.copy(remindersEnabled = enabled) }
    }

    /** Refresca solo lo que viene de afuera, sin pisar los sliders en uso. */
    fun refreshDerived() {
        local.update {
            it.copy(remindersEnabled = store.state.value.settings.remindersEnabled)
        }
    }

    fun save() {
        val draft = local.value
        val current = store.state.value.goals
        store.saveGoals(
            NutritionGoals(
                targetWeightKg = draft.targetWeightKg,
                currentWeightKg = current.currentWeightKg,
                kcalPerDay = draft.kcalPerDay.roundToInt(),
                activeDays = draft.activeDays,
                macroSplit = current.macroSplit
            )
        )
        store.setReminders(draft.remindersEnabled)
        local.update { it.copy(savedMessage = "Metas guardadas") }
    }

    fun clearSavedMessage() {
        local.update { it.copy(savedMessage = null) }
    }

    private fun initialState(): GoalsUiState {
        val snapshot = store.state.value
        return GoalsUiState(
            targetWeightKg = snapshot.goals.targetWeightKg,
            kcalPerDay = snapshot.goals.kcalPerDay.toDouble(),
            activeDays = snapshot.goals.activeDays,
            remindersEnabled = snapshot.settings.remindersEnabled,
            macroSplit = snapshot.goals.macroSplit,
            currentWeightKg = snapshot.goals.currentWeightKg,
            sportFocusTitle = snapshot.profile.sportFocus.title
        )
    }
}
