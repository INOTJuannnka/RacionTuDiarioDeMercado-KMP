package com.racion.app.shared.persistence

import com.racion.app.shared.data.SeedData
import com.racion.app.shared.models.AppSettings
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.UserProfile
import com.russhwolf.settings.MapSettings
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RacionStorageTest {

    private val timeZone = TimeZone.UTC

    @Test
    fun diarySurvivesSaveAndLoad() {
        val settings = MapSettings()
        val storage = RacionStorage(settings)
        val entries = SeedData.demoEntries(timeZone = timeZone)

        assertTrue(storage.saveEntries(entries))

        val loaded = storage.loadEntries()
        assertEquals(entries, loaded)
        assertEquals(entries.first().product.name, loaded?.first()?.product?.name)
    }

    @Test
    fun goalsAndProfileRoundTrip() {
        val storage = RacionStorage(MapSettings())
        val goals = NutritionGoals(kcalPerDay = 2_100, targetWeightKg = 60.5)
        val profile = UserProfile(displayName = "Camila", currentWeightKg = 61.0)

        assertTrue(storage.saveGoals(goals))
        assertTrue(storage.saveProfile(profile))

        assertEquals(goals, storage.loadGoals())
        assertEquals(profile, storage.loadProfile())
    }

    @Test
    fun settingsKeepBooleans() {
        val storage = RacionStorage(MapSettings())
        val settings = AppSettings(
            isDarkMode = true,
            onboardingCompleted = true,
            hasSeededDemoData = true,
            remindersEnabled = false
        )

        assertTrue(storage.saveSettings(settings))
        assertEquals(settings, storage.loadSettings())
    }

    @Test
    fun corruptedJsonDoesNotThrowAndReturnsNull() {
        val settings = MapSettings()
        val storage = RacionStorage(settings)
        settings.putString(RacionStorage.KEY_GOALS, "{ esto no es json")

        assertNull(storage.loadGoals())
    }

    @Test
    fun emptyStorageReturnsNull() {
        val storage = RacionStorage(MapSettings())

        assertNull(storage.loadEntries())
        assertNull(storage.loadProfile())
        assertNull(storage.loadSettings())
    }
}
