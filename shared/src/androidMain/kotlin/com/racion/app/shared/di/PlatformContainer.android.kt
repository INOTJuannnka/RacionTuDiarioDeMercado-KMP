package com.racion.app.shared.di

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Contenedor para Android. La actividad entrega el `Context` de la aplicación (no el
 * de una pantalla) para que las preferencias sobrevivan a la rotación.
 */
fun androidAppContainer(context: Context): AppContainer {
    val preferences = context.getSharedPreferences(
        "racion_settings",
        Context.MODE_PRIVATE
    )
    return AppContainer(settings = SharedPreferencesSettings(preferences))
}
