package com.racion.app.shared.viewmodel

import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Proyección del diario lista para dibujar. */
data class DiaryUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val onlyToday: Boolean = true,
    val totalKcal: Int = 0,
    val totalServings: Int = 0,
    val editingEntry: DiaryEntry? = null,
    val editingServings: Int = 1,
    val message: String? = null
)

private data class DiaryLocalState(
    val onlyToday: Boolean = true,
    val editingEntry: DiaryEntry? = null,
    val editingServings: Int = 1,
    val message: String? = null
)

/**
 * Historial completo, filtro "solo hoy", edición de porciones y borrado.
 *
 * El ViewModel nunca toca el almacenamiento: delega en [AppStore], que recalcula el
 * total con el método canónico del producto.
 */
class DiaryViewModel(
    private val store: AppStore
) : RacionViewModel() {

    private val local = MutableStateFlow(DiaryLocalState())

    val state: StateFlow<DiaryUiState> = combine(store.state, local) { snapshot, draft ->
        val today = AppFormat.today()
        val visible = (if (draft.onlyToday) {
            snapshot.entries.filter { AppFormat.localDate(it.loggedAtEpochMillis) == today }
        } else {
            snapshot.entries
        }).sortedByDescending { it.loggedAtEpochMillis }

        DiaryUiState(
            entries = visible,
            onlyToday = draft.onlyToday,
            totalKcal = visible.sumOf { it.totalNutrition.kcal },
            totalServings = visible.sumOf { it.servings },
            editingEntry = draft.editingEntry,
            editingServings = draft.editingServings,
            message = draft.message
        )
    }.stateIn(scope, SharingStarted.Eagerly, initialState())

    fun setOnlyToday(value: Boolean) {
        local.update { it.copy(onlyToday = value) }
    }

    fun beginEdit(entry: DiaryEntry) {
        local.update { it.copy(editingEntry = entry, editingServings = entry.servings.coerceAtLeast(1)) }
    }

    fun cancelEdit() {
        local.update { it.copy(editingEntry = null) }
    }

    fun setEditingServings(value: Int) {
        local.update { it.copy(editingServings = value.coerceIn(1, 20)) }
    }

    fun commitEdit() {
        val editing = local.value.editingEntry ?: return
        store.updateServings(editing.id, local.value.editingServings)
        local.update { it.copy(editingEntry = null, message = "Porciones actualizadas") }
    }

    fun delete(entry: DiaryEntry) {
        store.deleteEntry(entry.id)
        local.update { it.copy(message = "Entrada eliminada") }
    }

    fun deleteAll() {
        store.clearDiary()
        local.update { it.copy(message = "Diario vaciado") }
    }

    fun clearMessage() {
        local.update { it.copy(message = null) }
    }

    private fun initialState(): DiaryUiState {
        val entries = store.state.value.entries
            .filter { AppFormat.localDate(it.loggedAtEpochMillis) == AppFormat.today() }
            .sortedByDescending { it.loggedAtEpochMillis }
        return DiaryUiState(
            entries = entries,
            totalKcal = entries.sumOf { it.totalNutrition.kcal },
            totalServings = entries.sumOf { it.servings }
        )
    }
}
