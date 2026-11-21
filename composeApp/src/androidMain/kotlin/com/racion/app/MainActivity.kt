package com.racion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.di.androidAppContainer

/**
 * Entrada nativa de Android.
 *
 * La actividad no contiene navegación ni lógica de negocio: crea el [AppContainer]
 * con el `Context` de la aplicación y entrega el control a [App].
 */
class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = androidAppContainer(applicationContext)
        setContent {
            App(container)
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
