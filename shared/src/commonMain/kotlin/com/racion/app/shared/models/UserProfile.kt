package com.racion.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Enfoque de entrenamiento elegido en el onboarding. Ajusta las metas sugeridas. */
@Serializable
enum class SportFocus(
    val emoji: String,
    val title: String,
    val detail: String
) {
    @SerialName("futbol")
    FUTBOL("⚽️", "Fútbol", "Carga de carbos en días de partido"),

    @SerialName("running")
    RUNNING("🏃", "Running", "Enfoque en resistencia y recuperación"),

    @SerialName("fuerza")
    FUERZA("🏋️", "Fuerza", "Más proteína, superávit moderado"),

    @SerialName("ciclismo")
    CICLISMO("🚴", "Ciclismo", "Energía sostenida, carbos altos"),

    @SerialName("perdidaPeso")
    PERDIDA_PESO("🔥", "Pérdida de peso", "Déficit controlado y gradual"),

    @SerialName("mantenimiento")
    MANTENIMIENTO("🌿", "Mantenimiento", "Balance general, sin objetivo específico");

    /** `null` significa "mantené los macros actuales". */
    val suggestedMacroSplit: MacroSplit?
        get() = when (this) {
            FUTBOL -> MacroSplit(carbsPct = 55, proteinPct = 20, fatPct = 25)
            RUNNING -> MacroSplit(carbsPct = 50, proteinPct = 20, fatPct = 30)
            FUERZA -> MacroSplit(carbsPct = 35, proteinPct = 30, fatPct = 35)
            CICLISMO -> MacroSplit(carbsPct = 55, proteinPct = 15, fatPct = 30)
            PERDIDA_PESO -> MacroSplit(carbsPct = 40, proteinPct = 30, fatPct = 30)
            MANTENIMIENTO -> null
        }
}

/** Perfil mínimo del usuario. Las metas viven en su propio documento. */
@Serializable
data class UserProfile(
    val userId: String = "local-user",
    val displayName: String = "",
    val sportFocus: SportFocus = SportFocus.MANTENIMIENTO,
    val currentWeightKg: Double = 68.0
) {
    companion object {
        val LOCAL_DEFAULT = UserProfile(
            userId = "local-user",
            displayName = "",
            sportFocus = SportFocus.RUNNING,
            currentWeightKg = 68.0
        )
    }
}
