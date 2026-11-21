package com.racion.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.racion.app.shared.di.AppContainer
import com.racion.app.ui.screens.AddFoodScreen
import com.racion.app.ui.screens.ConfirmScreen
import com.racion.app.ui.screens.CreditsScreen
import com.racion.app.ui.screens.DiaryContent
import com.racion.app.ui.screens.GoalsContent
import com.racion.app.ui.screens.HomeContent
import com.racion.app.ui.screens.LoginScreen
import com.racion.app.ui.screens.OnboardingOverlay
import com.racion.app.ui.screens.ReportScreen
import com.racion.app.ui.screens.ScannerContent
import com.racion.app.ui.screens.SportProfileScreen
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.Theme

/**
 * Raíz de la navegación compartida.
 *
 * Un único `Navigator` de Voyager contiene esta pantalla. Las cuatro pestañas viven
 * aquí (estado local) y las pantallas de detalle se apilan encima con
 * `navigator.push(...)`. No hay ningún `NavHost` nativo ni navegación en SwiftUI o
 * Android: todo el flujo se decide en `commonMain`.
 */
class RootScreen(
    private val container: AppContainer
) : Screen {

    @Composable
    override fun Content() {
        RootTabs(container)
    }
}

private enum class RootTab(val label: String, val emoji: String) {
    HOME("Inicio", "🏠"),
    DIARY("Diario", "📖"),
    SCANNER("Escanear", "📷"),
    GOALS("Perfil", "🎯")
}

@Composable
private fun RootTabs(container: AppContainer) {
    var tab by remember { mutableStateOf(RootTab.HOME) }
    val state by container.store.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    if (state.session == null) {
        LoginScreen(container)
        return
    }

    // El onboarding es una pantalla completa, no una capa sobre las pestañas: así
    // no se puede interactuar con el diario antes de aceptar el aviso.
    if (!state.onboardingCompleted) {
        OnboardingOverlay(container)
        return
    }

    Scaffold(
        containerColor = Theme.colors.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(containerColor = Theme.colors.surfaceContainerLowest) {
                RootTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Text(item.emoji, style = AppType.body) },
                        label = { Text(item.label, style = AppType.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Theme.colors.primary,
                            selectedTextColor = Theme.colors.primary,
                            indicatorColor = Theme.colors.primaryContainer,
                            unselectedIconColor = Theme.colors.onSurfaceVariant,
                            unselectedTextColor = Theme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                RootTab.HOME -> HomeContent(
                    container = container,
                    onOpenReport = { navigator.push(ReportScreen(container)) },
                    onAddFood = { navigator.push(AddFoodScreen(container)) }
                )

                RootTab.DIARY -> DiaryContent(
                    container = container,
                    onAddFood = { navigator.push(AddFoodScreen(container)) }
                )

                RootTab.SCANNER -> ScannerContent(
                    container = container,
                    onProductFound = { product -> navigator.push(ConfirmScreen(container, product)) }
                )

                RootTab.GOALS -> GoalsContent(
                    container = container,
                    onOpenSportProfile = { navigator.push(SportProfileScreen(container)) },
                    onOpenCredits = { navigator.push(CreditsScreen(container)) }
                )
            }
        }
    }
}
