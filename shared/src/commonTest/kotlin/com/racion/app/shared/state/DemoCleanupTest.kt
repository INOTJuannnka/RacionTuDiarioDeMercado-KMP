package com.racion.app.shared.state

import com.racion.app.shared.data.SeedData
import com.racion.app.shared.models.AppSettings
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.MealSlot
import com.racion.app.shared.persistence.RacionStorage
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DemoCleanupTest {

    @Test
    fun newInstallStartsWithEmptyDiary() {
        assertTrue(AppStore(RacionStorage(MapSettings())).state.value.entries.isEmpty())
    }

    @Test
    fun legacyDemoIsRemovedOnceAndRealEntriesStay() {
        val storage = RacionStorage(MapSettings())
        val real = DiaryEntry(product = SeedData.featuredProduct, servings = 1, mealSlot = MealSlot.CENA)
        storage.saveEntries(SeedData.demoEntries() + real)
        storage.saveSettings(AppSettings(hasSeededDemoData = true))

        val store = AppStore(storage)

        assertEquals(listOf(real), store.state.value.entries)
        assertEquals(listOf(real), storage.loadEntries())
        assertEquals(false, storage.loadSettings()?.hasSeededDemoData)
    }
}
