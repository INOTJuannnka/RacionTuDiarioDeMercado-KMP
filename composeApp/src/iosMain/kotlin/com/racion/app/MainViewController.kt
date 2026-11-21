package com.racion.app

import androidx.compose.ui.window.ComposeUIViewController
import com.racion.app.shared.di.iosAppContainer
import platform.UIKit.UIViewController

/**
 * Entrada nativa de iOS.
 *
 * El host SwiftUI solo envuelve este `UIViewController`; la navegación y las
 * pantallas siguen siendo Compose Multiplatform.
 */
fun MainViewController(): UIViewController {
    val container = iosAppContainer()
    return ComposeUIViewController {
        App(container)
    }
}
