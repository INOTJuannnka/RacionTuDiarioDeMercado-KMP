package com.racion.app.shared.viewmodel

import com.racion.app.shared.data.AddFoodCategory
import com.racion.app.shared.data.LocalCatalog
import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.network.FoodCatalogService
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado completo de la búsqueda de alimentos. */
data class AddFoodUiState(
    val query: String = "",
    val selectedCategory: String = AddFoodCategory.ALL,
    val results: List<FoodProduct> = emptyList(),
    val isLoading: Boolean = false,
    val notice: String? = null,
    val errorMessage: String? = null,
    val favorites: List<FoodProduct> = emptyList(),
    /** Últimos productos registrados en el diario, sin repetir ni incluir favoritos. */
    val recents: List<FoodProduct> = emptyList()
) {
    /** Favoritos y recientes se muestran solo con el buscador vacío y sin filtro. */
    val showShortcuts: Boolean get() = query.isBlank() && selectedCategory == AddFoodCategory.ALL

    fun isFavorite(product: FoodProduct): Boolean = favorites.any { it.barcode == product.barcode }

    val categories: List<String> get() = AddFoodCategory.names
    val isEmpty: Boolean get() = !isLoading && results.isEmpty()
}

/**
 * Búsqueda con debounce de 350 ms y respaldo automático al catálogo local.
 *
 * Open Food Facts limita las peticiones por minuto, así que buscar en cada tecla
 * agotaría la cuota. `onQueryChange` cancela el trabajo anterior y solo la última
 * pausa llega a la red.
 */
class AddFoodViewModel(
    private val catalog: FoodCatalogService,
    private val localCatalog: LocalCatalog,
    private val store: AppStore
) : RacionViewModel() {

    private val _state = MutableStateFlow(
        AddFoodUiState(results = localCatalog.all)
    )
    val state: StateFlow<AddFoodUiState> = _state.asStateFlow()

    private var baseResults: List<FoodProduct> = localCatalog.all
    private var searchJob: Job? = null

    init {
        scope.launch {
            store.state.collect { app ->
                val favoriteCodes = app.favorites.map { it.barcode }.toSet()
                val recents = app.entries
                    .sortedByDescending { it.loggedAtEpochMillis }
                    .map { it.product }
                    .distinctBy { it.barcode }
                    .filterNot { it.barcode in favoriteCodes }
                    .take(RECENTS_LIMIT)
                _state.update { it.copy(favorites = app.favorites, recents = recents) }
            }
        }
    }

    fun toggleFavorite(product: FoodProduct) = store.toggleFavorite(product)

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }

        searchJob?.cancel()
        searchJob = scope.launch {
            val term = value.trim()
            if (term.isNotEmpty()) delay(DEBOUNCE_MS)
            reload(term)
        }
    }

    fun submit() {
        searchJob?.cancel()
        searchJob = scope.launch { reload(_state.value.query.trim()) }
    }

    /** Cambia el filtro y re-filtra en memoria (sin gastar una petición). */
    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        applyCategoryFilter()
    }

    fun clearMessages() {
        _state.update { it.copy(notice = null, errorMessage = null) }
    }

    private suspend fun reload(term: String) {
        _state.update { it.copy(notice = null, errorMessage = null) }

        // Sin texto no se consulta la red: se muestra el catálogo local completo.
        if (term.isEmpty()) {
            baseResults = localCatalog.search("")
            _state.update { it.copy(isLoading = false) }
            applyCategoryFilter()
            return
        }

        _state.update { it.copy(isLoading = true) }
        val result = catalog.search(term)
        _state.update { it.copy(isLoading = false) }

        when (result) {
            is AppResult.Success -> {
                val list = result.value
                if (list.isNotEmpty()) {
                    baseResults = list
                    _state.update { it.copy(notice = "Resultados en vivo de Open Food Facts.") }
                } else {
                    baseResults = localCatalog.search(term)
                    _state.update {
                        it.copy(notice = "Sin resultados en línea: mostrando el catálogo local.")
                    }
                }
            }

            is AppResult.Failure -> {
                // El fallo de red no deja la pantalla vacía: se cae al catálogo curado.
                baseResults = localCatalog.search(term)
                _state.update {
                    it.copy(
                        errorMessage = result.error.userMessage,
                        notice = "Mostrando el catálogo local."
                    )
                }
            }
        }
        applyCategoryFilter()
    }

    private fun applyCategoryFilter() {
        val category = _state.value.selectedCategory
        val filtered = if (category == AddFoodCategory.ALL) {
            baseResults
        } else {
            baseResults.filter { product ->
                product.categories.any { it.equals(category, ignoreCase = true) }
            }
        }
        _state.update { it.copy(results = filtered) }
    }

    companion object {
        const val DEBOUNCE_MS: Long = 350
        const val RECENTS_LIMIT = 6
    }
}
