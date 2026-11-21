package com.racion.app.shared.data

import com.racion.app.shared.models.FoodProduct

/**
 * Catálogo offline con los platos típicos del proyecto.
 *
 * Si Open Food Facts no responde (sin internet, límite de peticiones, avión), la
 * búsqueda cae aquí y la pantalla avisa "mostrando catálogo local" en lugar de
 * quedarse vacía o mostrar un error sin salida.
 */
class LocalCatalog(
    private val products: List<FoodProduct> = SeedData.products
) {
    fun search(query: String, category: String? = null): List<FoodProduct> {
        val term = query.trim().lowercase()
        return products.filter { product ->
            val matchesCategory = category.isNullOrBlank() ||
                category == AddFoodCategory.ALL ||
                product.categories.any { it.equals(category, ignoreCase = true) }
            if (!matchesCategory) return@filter false
            if (term.isEmpty()) return@filter true

            product.name.lowercase().contains(term) ||
                product.brand.orEmpty().lowercase().contains(term) ||
                product.categories.any { it.lowercase().contains(term) }
        }
    }

    fun product(barcode: String): FoodProduct? =
        products.firstOrNull { it.barcode == barcode.trim() }

    val all: List<FoodProduct> get() = products
}

/** Categorías de la pantalla "Agregar". La primera es el filtro "Todos". */
object AddFoodCategory {
    const val ALL = "Todos"
    val names: List<String> = listOf(ALL, "Almuerzo", "Frituras", "Sopas", "Bebidas")
}
