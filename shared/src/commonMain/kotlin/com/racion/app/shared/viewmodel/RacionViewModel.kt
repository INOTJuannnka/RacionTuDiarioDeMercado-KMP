package com.racion.app.shared.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Base de los ViewModels compartidos.
 *
 * Cada pantalla crea el suyo y llama a [onCleared] al salir (desde un
 * `DisposableEffect` de Compose). Así un flujo derivado con `stateIn` no queda vivo
 * después de que la pantalla desaparezca.
 */
abstract class RacionViewModel {

    protected val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    open fun onCleared() {
        scope.cancel()
    }
}
