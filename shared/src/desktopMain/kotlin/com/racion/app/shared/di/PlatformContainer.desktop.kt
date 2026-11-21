package com.racion.app.shared.di

import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

/**
 * Contenedor para Desktop. `java.util.prefs` viene incluido en la JVM y no requiere
 * un archivo de configuración adicional.
 */
fun desktopAppContainer(): AppContainer =
    AppContainer(
        settings = PreferencesSettings(
            Preferences.userRoot().node("com/racion/app")
        )
    )
