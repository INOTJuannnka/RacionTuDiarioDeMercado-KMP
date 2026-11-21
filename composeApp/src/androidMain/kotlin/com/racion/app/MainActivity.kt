package com.racion.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.racion.app.reminders.DailyReminder
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.di.androidAppContainer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Entrada nativa de Android.
 *
 * La actividad no contiene navegación ni lógica de negocio: crea el [AppContainer]
 * con el `Context` de la aplicación y entrega el control a [App].
 */
class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    // Sin permiso de notificaciones el recordatorio no puede salir: se apaga el switch para no mentir.
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) DailyReminder.schedule(this) else container.store.setReminders(false)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = androidAppContainer(applicationContext)
        setContent {
            App(container)
        }
        syncReminder()
    }

    /** Programa o cancela el aviso diario cada vez que cambia el switch de "Recordatorios". */
    private fun syncReminder() = lifecycleScope.launch {
        container.store.state
            .map { it.settings.remindersEnabled }
            .distinctUntilChanged()
            .collect { enabled ->
                when {
                    !enabled -> DailyReminder.cancel(this@MainActivity)
                    DailyReminder.canNotify(this@MainActivity) -> DailyReminder.schedule(this@MainActivity)
                    Build.VERSION.SDK_INT >= 33 -> notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // El contenedor se comparte con toda la app; solo se libera si la actividad
        // raíz se está cerrando de verdad (no en una rotación).
        if (isFinishing) {
            container.close()
        }
    }
}
