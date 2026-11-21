package com.racion.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.Saver
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigatorSaver
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorSaver
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
@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun App(container: AppContainer) {
    val state by container.store.state.collectAsState()

    RacionTheme(darkTheme = state.isDarkMode) {
        // Las pantallas llevan el AppContainer (no serializable): guardar la pila en el Bundle
        // crasheaba la app al pasar a segundo plano. Al recrearse arranca de nuevo en RootScreen.
        CompositionLocalProvider(LocalNavigatorSaver provides noSaveNavigator) {
            Navigator(RootScreen(container))
        }
    }
}

@OptIn(ExperimentalVoyagerApi::class)
private val noSaveNavigator = NavigatorSaver<Any> { _, _, _, _, _ ->
    Saver(save = { null }, restore = { null })
}
