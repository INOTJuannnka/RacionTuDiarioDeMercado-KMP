package com.racion.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import com.racion.app.shared.di.AppContainer
import com.racion.app.ui.navigation.RootScreen
import com.racion.app.ui.theme.RacionTheme

/**
 * Punto de entrada de la interfaz compartida.
 *
 * El tema sigue `settings.isDarkMode` (persistido) y toda la navegación vive en un
 * único [Navigator] de Voyager, que arranca en [RootScreen]. Android, iOS y Desktop
 * solo llaman a esta función con su [AppContainer].
 */
@Composable
fun App(container: AppContainer) {
    val state by container.store.state.collectAsState()

    RacionTheme(darkTheme = state.isDarkMode) {
        Navigator(RootScreen(container))
    }
}
