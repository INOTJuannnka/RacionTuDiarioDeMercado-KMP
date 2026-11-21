package com.racion.app.shared.models

import kotlinx.serialization.Serializable

/** Preferencias locales del dispositivo (no de la cuenta). */
@Serializable
data class AppSettings(
    val isDarkMode: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val hasSeededDemoData: Boolean = false,
    val remindersEnabled: Boolean = true
)
