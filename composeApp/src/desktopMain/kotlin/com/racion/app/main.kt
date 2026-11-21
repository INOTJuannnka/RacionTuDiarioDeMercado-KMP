package com.racion.app

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.racion.app.shared.di.desktopAppContainer

/**
 * Entrada opcional de escritorio. Sirve para verificar la UI y la navegación
 * compartidas sin emulador.
 */
fun main() = application {
    val container = remember { desktopAppContainer() }

    Window(
        onCloseRequest = {
            container.close()
            exitApplication()
        },
        title = "Ración · Tu Diario de Mercado",
        state = rememberWindowState(width = 430.dp, height = 900.dp)
    ) {
        App(container)
    }
}
