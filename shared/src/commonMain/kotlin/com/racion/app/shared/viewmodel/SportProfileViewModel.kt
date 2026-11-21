package com.racion.app.shared.viewmodel

import com.racion.app.shared.models.MacroSplit
import com.racion.app.shared.models.SportFocus
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SportProfileUiState(
    val selected: SportFocus = SportFocus.MANTENIMIENTO,
    val previewSplit: MacroSplit? = null,
    val macroPreviewLabel: String = "",
    val savedMessage: String? = null
)

private data class SportProfileLocal(
    val selected: SportFocus,
    val savedMessage: String? = null
)

/**
 * Selección del enfoque deportivo. La lista de opciones es el enum `SportFocus`
 * completo: agregar un enfoque nuevo no puede dejar la pantalla incompleta.
 */
class SportProfileViewModel(
    private val store: AppStore
) : RacionViewModel() {

    private val local = MutableStateFlow(
        SportProfileLocal(selected = store.state.value.profile.sportFocus)
    )

    val state: StateFlow<SportProfileUiState> = local
        .map { draft -> buildState(draft) }
        .stateIn(scope, SharingStarted.Eagerly, buildState(local.value))

    fun select(focus: SportFocus) {
        local.update { it.copy(selected = focus) }
    }

    /** Aplica el enfoque y devuelve `true`; la vista usa eso para cerrarse. */
    fun apply(): Boolean {
        val selected = local.value.selected
        store.applySportFocus(selected)
        local.update { it.copy(savedMessage = "Perfil actualizado") }
        return true
    }

    private fun buildState(draft: SportProfileLocal): SportProfileUiState {
        val split = draft.selected.suggestedMacroSplit
        return SportProfileUiState(
            selected = draft.selected,
            previewSplit = split,
            macroPreviewLabel = if (split == null) {
                "Mantenimiento no cambia tus macros actuales."
            } else {
                "Sugerencia: ${split.carbsPct}% carbos · ${split.proteinPct}% proteína · ${split.fatPct}% grasas"
            },
            savedMessage = draft.savedMessage
        )
    }
}
