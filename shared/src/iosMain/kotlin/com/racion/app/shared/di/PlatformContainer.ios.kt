package com.racion.app.shared.di

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

/**
 * Contenedor para iOS. `NSUserDefaults` guarda las mismas claves JSON que Android y
 * Desktop, así que el resto de la app no cambia.
 */
fun iosAppContainer(): AppContainer =
    AppContainer(
        settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    )
