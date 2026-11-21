package com.racion.app.shared.network

import com.racion.app.shared.data.SeedData
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.SportFocus
import com.racion.app.shared.models.UserProfile
import com.racion.app.shared.models.Weekday
import com.racion.app.shared.persistence.RacionStorage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class FirestoreValuesTest {
    private val json = RacionStorage.defaultJson()

    @Test
    fun entriesSurviveRoundTripThroughFirestoreValues() {
        val entries = SeedData.demoEntries()
        val element = json.encodeToJsonElement(ListSerializer(DiaryEntry.serializer()), entries)
        val asFields = toFirestoreFields(JsonObject(mapOf("entries" to element)))

        val back = fromFirestoreFields(asFields).getValue("entries")
        assertEquals(entries, json.decodeFromJsonElement(ListSerializer(DiaryEntry.serializer()), back))
    }

    @Test
    fun integersGoAsTextAndDecimalsAsDouble() {
        val fields = toFirestoreFields(json.parseToJsonElement("""{"kcal":539,"fat":30.9,"name":"x","ok":true}""").jsonObject)
        assertEquals("""{"integerValue":"539"}""", fields.getValue("kcal").toString())
        assertEquals("""{"doubleValue":30.9}""", fields.getValue("fat").toString())
        assertEquals("""{"stringValue":"x"}""", fields.getValue("name").toString())
        assertEquals("""{"booleanValue":true}""", fields.getValue("ok").toString())
    }

    @Test
    fun legacySwiftUiGoalsAndProfileDecode() {
        // Formato real guardado por la versión SwiftUI (nombres de enum en mayúsculas).
        val goalsFields = json.parseToJsonElement(
            """{"targetWeightKg":{"doubleValue":67.5},"currentWeightKg":{"doubleValue":68},"kcalPerDay":{"integerValue":"2160"},
            "macroSplit":{"mapValue":{"fields":{"carbsPct":{"integerValue":"44"},"fatPct":{"integerValue":"29"},"proteinPct":{"integerValue":"27"}}}},
            "activeDays":{"arrayValue":{"values":[{"stringValue":"WEDNESDAY"},{"stringValue":"MONDAY"}]}}}"""
        ).jsonObject
        val goals = json.decodeFromJsonElement(NutritionGoals.serializer(), fromFirestoreFields(goalsFields))
        assertEquals(setOf(Weekday.WEDNESDAY, Weekday.MONDAY), goals.activeDays)
        assertEquals(2160, goals.kcalPerDay)

        val profileFields = json.parseToJsonElement(
            """{"currentWeightKg":{"doubleValue":68},"sportFocus":{"stringValue":"MANTENIMIENTO"},"displayName":{"stringValue":""}}"""
        ).jsonObject
        val profile = json.decodeFromJsonElement(UserProfile.serializer(), fromFirestoreFields(profileFields))
        assertEquals(SportFocus.MANTENIMIENTO, profile.sportFocus)
    }
}
