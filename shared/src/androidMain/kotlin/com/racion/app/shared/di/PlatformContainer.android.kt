package com.racion.app.shared.di

import android.content.Context
import com.racion.app.shared.persistence.RacionStorage
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Contenedor para Android. La actividad entrega el `Context` de la aplicación (no el
 * de una pantalla) para que las preferencias sobrevivan a la rotación.
 */
fun androidAppContainer(context: Context): AppContainer = AppContainer(settings = androidSettings(context))

/** Lectura directa del almacenamiento, sin levantar el contenedor (p. ej. desde un BroadcastReceiver). */
fun androidStorage(context: Context): RacionStorage = RacionStorage(androidSettings(context))

private fun androidSettings(context: Context) = SharedPreferencesSettings(
    context.getSharedPreferences("racion_settings", Context.MODE_PRIVATE)
)
