package com.racion.app.shared.viewmodel

import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CreditsUiState(
    val isDarkMode: Boolean = false,
    val message: String? = null
)

private data class CreditsLocal(
    val message: String? = null
)

/**
 * Pantalla de descripción y créditos. Concentra el modo oscuro persistido y la
 * restauración del diario de demostración.
 */
class CreditsViewModel(
    private val store: AppStore
) : RacionViewModel() {

    private val local = MutableStateFlow(CreditsLocal())

    val state: StateFlow<CreditsUiState> = combine(store.state, local) { snapshot, draft ->
        CreditsUiState(
            isDarkMode = snapshot.settings.isDarkMode,
            message = draft.message
        )
    }.stateIn(scope, SharingStarted.Eagerly, CreditsUiState(store.state.value.isDarkMode))

    fun setDarkMode(enabled: Boolean) {
        store.setDarkMode(enabled)
    }

    fun resetDemoData() {
        store.resetDemoData()
        local.update { it.copy(message = "Diario de ejemplo restaurado.") }
    }

    fun clearMessage() {
        local.update { it.copy(message = null) }
    }
}
