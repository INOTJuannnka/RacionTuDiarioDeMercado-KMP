package com.racion.app.shared.state

import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.data.ReportBuilder
import com.racion.app.shared.data.SeedData
import com.racion.app.shared.models.AppSettings
import com.racion.app.shared.models.AuthSession
import com.racion.app.shared.models.DailySummary
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.Nutrition
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.SportFocus
import com.racion.app.shared.models.UserProfile
import com.racion.app.shared.models.WeeklyReport
import com.racion.app.shared.persistence.RacionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Almacén raíz de la aplicación (el `AppModel` del proyecto SwiftUI).
 *
 * Carga los datos una sola vez, siembra la demo la primera vez y expone todas las
 * escrituras como métodos. Es la única clase que conoce [RacionStorage]; los
 * ViewModels se limitan a leer [state] y a llamar estas intenciones.
 */
class AppStore(
    private val storage: RacionStorage
) {

    private val _state: MutableStateFlow<AppState>
    val state: StateFlow<AppState>

    init {
        val savedSettings = storage.loadSettings() ?: AppSettings()
        val savedGoals = storage.loadGoals() ?: NutritionGoals.DEFAULT
        val savedProfile = storage.loadProfile() ?: UserProfile.LOCAL_DEFAULT
        var savedEntries = storage.loadEntries().orEmpty()

        // La demo se siembra una única vez. A partir de ahí las entradas son datos
        // reales del usuario: se pueden borrar y sobreviven a reiniciar la app.
        val shouldSeed = !savedSettings.hasSeededDemoData
        if (shouldSeed) {
            savedEntries = SeedData.demoEntries()
        }
        val finalSettings = if (shouldSeed) {
            savedSettings.copy(hasSeededDemoData = true)
        } else {
            savedSettings
        }

        _state = MutableStateFlow(
            AppState(
                entries = savedEntries.sortedBy { it.loggedAtEpochMillis },
                goals = savedGoals,
                profile = savedProfile,
                settings = finalSettings,
                session = storage.loadSession()
            )
        )
        state = _state.asStateFlow()

        if (shouldSeed) {
            val entriesSaved = storage.saveEntries(savedEntries)
            val settingsSaved = storage.saveSettings(finalSettings)
            if (!entriesSaved || !settingsSaved) {
                reportStorageError("No se pudieron guardar los datos iniciales.")
            }
        }
    }

    // MARK: - Lecturas derivadas

    fun entriesOn(
        day: LocalDate,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): List<DiaryEntry> = _state.value.entries
        .filter { AppFormat.localDate(it.loggedAtEpochMillis, timeZone) == day }
        .sortedBy { it.loggedAtEpochMillis }

    /** Resumen listo para "Inicio". La vista no suma nada. */
    fun summary(
        day: LocalDate = AppFormat.today(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): DailySummary {
        val dayEntries = entriesOn(day, timeZone)
        return DailySummary(
            dateLabel = AppFormat.dayLabel(day),
            consumed = Nutrition.total(dayEntries.map { it.totalNutrition }),
            goalKcal = _state.value.goals.kcalPerDay,
            entries = dayEntries
        )
    }

    /** Informe semanal calculado desde las entradas reales. */
    fun weeklyReport(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): WeeklyReport = ReportBuilder.weeklyReport(
        entries = _state.value.entries,
        goals = _state.value.goals,
        timeZone = timeZone
    )

    val onboardingCompleted: Boolean get() = _state.value.onboardingCompleted

    // MARK: - Escrituras del diario

    fun addEntry(entry: DiaryEntry) {
        updateEntries(_state.value.entries + entry)
    }

    fun deleteEntry(id: String) {
        updateEntries(_state.value.entries.filterNot { it.id == id })
    }

    /**
     * Cambia las porciones de una entrada y **recalcula** el total con el método
     * canónico del producto, para que el número mostrado y el guardado coincidan.
     */
    fun updateServings(entryID: String, servings: Int) {
        val safeServings = servings.coerceAtLeast(1)
        val updated = _state.value.entries.map { entry ->
            if (entry.id != entryID) {
                entry
            } else {
                entry.copy(
                    servings = safeServings,
                    totalNutrition = entry.product.nutritionForUnits(safeServings)
                )
            }
        }
        updateEntries(updated)
    }

    fun clearDiary() {
        updateEntries(emptyList())
    }

    /**
     * Restaura el diario de demostración sin tocar metas, perfil ni preferencias.
     * Es la forma de recuperar la demo después de haberla vaciado.
     */
    fun resetDemoData() {
        val seeded = SeedData.demoEntries()
        _state.update {
            it.copy(
                entries = seeded.sortedBy { entry -> entry.loggedAtEpochMillis },
                settings = it.settings.copy(hasSeededDemoData = true)
            )
        }
        persistDiary()
        persistSettings()
    }

    // MARK: - Metas y perfil

    fun saveGoals(newGoals: NutritionGoals) {
        _state.update {
            it.copy(
                goals = newGoals,
                // Mantener el peso actual del perfil alineado con las metas.
                profile = it.profile.copy(currentWeightKg = newGoals.currentWeightKg)
            )
        }
        persistMeta()
    }

    fun saveProfile(newProfile: UserProfile) {
        _state.update { it.copy(profile = newProfile) }
        persistMeta()
    }

    /**
     * Elige el enfoque deportivo y aplica el reparto de macros sugerido.
     * "Mantenimiento" no toca los macros: significa "sin objetivo específico".
     */
    fun applySportFocus(focus: SportFocus) {
        _state.update { current ->
            current.copy(
                profile = current.profile.copy(sportFocus = focus),
                goals = focus.suggestedMacroSplit?.let { split ->
                    current.goals.copy(macroSplit = split)
                } ?: current.goals
            )
        }
        persistMeta()
    }

    fun completeOnboarding() {
        _state.update { it.copy(settings = it.settings.copy(onboardingCompleted = true)) }
        persistSettings()
    }

    fun setDarkMode(enabled: Boolean) {
        _state.update { it.copy(settings = it.settings.copy(isDarkMode = enabled)) }
        persistSettings()
    }

    fun setReminders(enabled: Boolean) {
        _state.update { it.copy(settings = it.settings.copy(remindersEnabled = enabled)) }
        persistSettings()
    }

    // MARK: - Sesión

    fun setSession(session: AuthSession?) {
        _state.update { it.copy(session = session) }
        if (!storage.saveSession(session)) {
            reportStorageError("No se pudo guardar la sesión.")
        }
    }

    fun signOut() = setSession(null)

    fun clearStorageError() {
        _state.update { it.copy(storageError = null) }
    }

    // MARK: - Interno

    private fun updateEntries(newEntries: List<DiaryEntry>) {
        _state.update {
            it.copy(entries = newEntries.sortedBy { entry -> entry.loggedAtEpochMillis })
        }
        persistDiary()
    }

    private fun persistDiary() {
        if (!storage.saveEntries(_state.value.entries)) {
            reportStorageError("No se pudo guardar el diario en el dispositivo.")
        }
    }

    private fun persistMeta() {
        val current = _state.value
        val goalsSaved = storage.saveGoals(current.goals)
        val profileSaved = storage.saveProfile(current.profile)
        if (!goalsSaved || !profileSaved) {
            reportStorageError("No se pudieron guardar tus metas y tu perfil.")
        }
    }

    private fun persistSettings() {
        if (!storage.saveSettings(_state.value.settings)) {
            reportStorageError("No se pudieron guardar las preferencias.")
        }
    }

    private fun reportStorageError(message: String) {
        _state.update { it.copy(storageError = message) }
    }
}
