package com.racion.app.shared.state

import com.racion.app.shared.models.AppSettings
import com.racion.app.shared.models.AuthSession
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.shared.models.FoodProduct
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.UserProfile

/**
 * Fotografía completa e inmutable del estado de la app.
 *
 * Es la ÚNICA fuente de verdad para diario, metas, perfil y preferencias. Cada
 * pantalla recibe una proyección de este objeto a través de su ViewModel; ninguna
 * vista lee el almacenamiento ni modifica `entries` por su cuenta.
 */
data class AppState(
    val entries: List<DiaryEntry> = emptyList(),
    val goals: NutritionGoals = NutritionGoals.DEFAULT,
    val profile: UserProfile = UserProfile.LOCAL_DEFAULT,
    val settings: AppSettings = AppSettings(),
    /** `null` = nadie inició sesión: se muestra el login. */
    val session: AuthSession? = null,
    /** Productos marcados con estrella en "Agregar alimento". Viven en el dispositivo. */
    val favorites: List<FoodProduct> = emptyList(),
    /**
     * Error de escritura local (disco lleno, cuota). Se muestra como banner y no
     * interrumpe el uso de la app.
     */
    val storageError: String? = null
) {
    val isDarkMode: Boolean get() = settings.isDarkMode
    val onboardingCompleted: Boolean get() = settings.onboardingCompleted
}
