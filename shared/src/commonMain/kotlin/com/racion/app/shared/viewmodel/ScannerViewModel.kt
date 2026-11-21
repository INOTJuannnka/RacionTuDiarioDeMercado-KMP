package com.racion.app.shared.viewmodel

import com.racion.app.shared.data.LocalCatalog
import com.racion.app.shared.data.SeedData
import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.network.FoodCatalogService
import com.racion.app.shared.state.AppStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del escáner: código, consulta en curso, mensajes y producto resuelto. */
data class ScannerUiState(
    val barcode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notice: String? = null,
    val foundProduct: FoodProduct? = null,
    val recentScans: List<DiaryEntry> = emptyList()
)

/**
 * Consulta cada código de barras en Open Food Facts y cae al catálogo local cuando
 * no hay red. La pantalla nunca se queda sin respuesta.
 *
 * La cámara física no forma parte de este módulo compartido: la entrada manual y la
 * lectura simulada usan exactamente el mismo camino de resolución.
 */
class ScannerViewModel(
    private val store: AppStore,
    private val catalog: FoodCatalogService,
    private val localCatalog: LocalCatalog
) : RacionViewModel() {

    private val _state = MutableStateFlow(ScannerUiState(recentScans = recentScans()))
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    init {
        scope.launch {
            store.state.collect {
                _state.update { current -> current.copy(recentScans = recentScans()) }
            }
        }
    }

    fun setBarcode(value: String) {
        _state.update { it.copy(barcode = value) }
    }

    /** Resuelve el código escrito en el campo de texto. */
    fun lookup() {
        val code = _state.value.barcode
        scope.launch { resolve(code) }
    }

    /** Lectura simulada para escritorio/simulador: consulta el producto estrella. */
    fun simulateScan() {
        val code = SeedData.featuredProduct.barcode
        _state.update { it.copy(barcode = code) }
        scope.launch { resolve(code, simulated = true) }
    }

    /** Punto de entrada para una cámara nativa que lea un código. */
    fun handleDetectedCode(code: String) {
        _state.update { it.copy(barcode = code) }
        scope.launch { resolve(code) }
    }

    /**
     * Entrega el producto una sola vez y limpia el estado para que volver atrás no
     * dispare una segunda navegación.
     */
    fun consumeFoundProduct(): FoodProduct? {
        val product = _state.value.foundProduct
        if (product != null) {
            _state.update { it.copy(foundProduct = null) }
        }
        return product
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, notice = null) }
    }

    private suspend fun resolve(raw: String, simulated: Boolean = false) {
        val clean = raw.trim()
        if (clean.isEmpty()) {
            _state.update { it.copy(errorMessage = "Ingresá un código de barras.") }
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                notice = if (simulated) "Lectura simulada · $clean" else null
            )
        }

        when (val result = catalog.product(clean)) {
            is AppResult.Success -> {
                _state.update {
                    it.copy(barcode = "", isLoading = false, foundProduct = result.value)
                }
            }

            is AppResult.Failure -> {
                val local = localCatalog.product(clean)
                if (local != null) {
                    _state.update {
                        it.copy(
                            barcode = "",
                            isLoading = false,
                            foundProduct = local,
                            notice = "Sin conexión: encontrado en el catálogo local."
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.userMessage,
                            notice = "Probá con otro código o revisá la conexión."
                        )
                    }
                }
            }
        }
    }

    /** Últimos productos registrados, sin repetir producto. */
    private fun recentScans(limit: Int = 4): List<DiaryEntry> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<DiaryEntry>()
        for (entry in store.state.value.entries.sortedByDescending { it.loggedAtEpochMillis }) {
            if (!seen.add(entry.product.barcode)) continue
            result += entry
            if (result.size == limit) break
        }
        return result
    }
}
