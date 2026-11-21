package com.racion.app.shared.network

import com.racion.app.shared.models.AppResult
import com.racion.app.shared.models.FoodProduct

/**
 * Acceso de lectura al catálogo de alimentos.
 *
 * Todos los métodos son `suspend` y devuelven [AppResult]: ninguno lanza y ninguna
 * excepción de transporte llega a la UI sin traducirse a un `AppError` con mensaje
 * en español.
 */
interface FoodCatalogService {

    /** Búsqueda por texto libre (endpoint legacy `cgi/search.pl`). */
    suspend fun search(query: String): AppResult<List<FoodProduct>>

    /** Búsqueda por código de barras (`/api/v2/product/{code}.json`). */
    suspend fun product(barcode: String): AppResult<FoodProduct>
}
