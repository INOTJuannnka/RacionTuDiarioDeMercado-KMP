package com.racion.app.shared.state

import com.racion.app.shared.data.SeedData
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.persistence.RacionStorage
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FavoritesAndManualTest {

    @Test
    fun favoriteTogglesAndSurvivesRestart() {
        val storage = RacionStorage(MapSettings())
        val store = AppStore(storage)
        val product = SeedData.featuredProduct

        store.toggleFavorite(product)
        assertEquals(listOf(product), AppStore(storage).state.value.favorites)

        store.toggleFavorite(product)
        assertTrue(AppStore(storage).state.value.favorites.isEmpty())
    }

    @Test
    fun manualProductShowsTypedValuesForOneServing() {
        val product = FoodProduct.manual("770123", " Bocadillo ", "120", "28,5", "1", "0")!!
        assertEquals("Bocadillo", product.name)
        val one = product.nutritionForUnits(1)
        assertEquals(120, one.kcal)
        assertEquals(28.5, one.carbsG)
    }

    @Test
    fun manualProductRejectsMissingNameOrBadNumbers() {
        assertNull(FoodProduct.manual("1", " ", "100", "", "", ""))
        assertNull(FoodProduct.manual("1", "Pan", "cien", "", "", ""))
        assertNull(FoodProduct.manual("1", "Pan", "-5", "", "", ""))
    }
}
