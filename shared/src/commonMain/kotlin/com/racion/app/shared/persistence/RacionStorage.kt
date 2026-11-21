package com.racion.app.shared.persistence

import com.racion.app.shared.models.AppSettings
import com.racion.app.shared.models.AuthSession
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.UserProfile
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local compartida.
 *
 * Usa `multiplatform-settings` (SharedPreferences en Android, NSUserDefaults en iOS,
 * Preferences en Desktop) y guarda cada documento como una cadena JSON con las
 * mismas claves que el proyecto SwiftUI (`diary.json`, `goals.json`, ...). La UI
 * nunca conoce estas claves: solo habla con `AppStore`.
 *
 * Todos los métodos devuelven `Boolean` o `null` en lugar de lanzar. Un disco lleno
 * o un JSON corrupto no deben tumbar el arranque de la app: el llamador decide qué
 * avisar y sigue con los valores por defecto.
 */
class RacionStorage(
    private val settings: Settings,
    private val json: Json = defaultJson()
) {

    // MARK: - Diario

    fun loadEntries(): List<DiaryEntry>? = read(KEY_DIARY)

    fun saveEntries(entries: List<DiaryEntry>): Boolean = write(KEY_DIARY, entries)

    // MARK: - Metas

    fun loadGoals(): NutritionGoals? = read(KEY_GOALS)

    fun saveGoals(goals: NutritionGoals): Boolean = write(KEY_GOALS, goals)

    // MARK: - Perfil

    fun loadProfile(): UserProfile? = read(KEY_PROFILE)

    fun saveProfile(profile: UserProfile): Boolean = write(KEY_PROFILE, profile)

    // MARK: - Preferencias

    fun loadSettings(): AppSettings? = read(KEY_SETTINGS)

    fun saveSettings(settings: AppSettings): Boolean = write(KEY_SETTINGS, settings)

    // MARK: - Sesión

    fun loadSession(): AuthSession? = read(KEY_SESSION)

    fun saveSession(session: AuthSession?): Boolean =
        if (session == null) runCatching { settings.remove(KEY_SESSION); true }.getOrDefault(false)
        else write(KEY_SESSION, session)

    // MARK: - Interno

    private inline fun <reified T> read(key: String): T? {
        if (!settings.hasKey(key)) return null
        val raw = settings.getString(key, "")
        if (raw.isBlank()) return null
        return runCatching { json.decodeFromString<T>(raw) }.getOrNull()
    }

    private inline fun <reified T> write(key: String, value: T): Boolean =
        runCatching {
            settings.putString(key, json.encodeToString(value))
            true
        }.getOrDefault(false)

    companion object {
        const val KEY_DIARY = "diary.json"
        const val KEY_GOALS = "goals.json"
        const val KEY_PROFILE = "profile.json"
        const val KEY_SETTINGS = "settings.json"
        const val KEY_SESSION = "session.json"

        /**
         * JSON tolerante: la app evoluciona y un campo nuevo no debe invalidar los
         * datos ya guardados por una versión anterior.
         */
        fun defaultJson(): Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }
    }
}
