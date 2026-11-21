package com.racion.app.shared.models

import kotlinx.serialization.Serializable

/** Preferencias locales del dispositivo (no de la cuenta). */
@Serializable
data class AppSettings(
    val isDarkMode: Boolean = false,
    val onboardingCompleted: Boolean = false,
    /** `true` solo en instalaciones de versiones que sembraban un diario de ejemplo: marca que hay que limpiarlo. */
    val hasSeededDemoData: Boolean = false,
    val remindersEnabled: Boolean = true
)
