package com.racion.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.SportFocus
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.PrimaryButton
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.Theme

/**
 * Onboarding de dos pasos: aviso legal y elección del enfoque deportivo.
 *
 * Reemplaza el par de rutas AVISO → PERFIL_DEPORTIVO del grafo original y se
 * muestra a pantalla completa mientras `settings.onboardingCompleted` sea `false`.
 */
@Composable
fun OnboardingOverlay(container: AppContainer) {
    var step by remember { mutableStateOf(0) }
    var showPolicy by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(SportFocus.MANTENIMIENTO) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme.colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        if (step == 0) {
            DisclaimerStep(
                onAccept = { step = 1 },
                onViewPolicy = { showPolicy = true }
            )
        } else {
            SportStep(
                selected = selected,
                onSelect = { selected = it },
                onStart = {
                    container.store.applySportFocus(selected)
                    container.store.completeOnboarding()
                }
            )
        }

        if (showPolicy) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Theme.colors.surface)
            ) {
                CreditsContent(container = container, onBack = { showPolicy = false })
            }
        }
    }
}

@Composable
private fun DisclaimerStep(
    onAccept: () -> Unit,
    onViewPolicy: () -> Unit
) {
    ScreenContainer(bottomPadding = 40.dp) {
        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(Theme.colors.tertiary),
            contentAlignment = Alignment.Center
        ) {
            Text("🌿", style = AppType.display)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Ración acompaña, no reemplaza",
            style = AppType.headlineLarge,
            color = Theme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "La información nutricional es una guía general. Antes de fijar metas de peso, déficit calórico o rutinas intensas, hablá con un médico, nutricionista o profesional certificado.",
            style = AppType.body,
            color = Theme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(30.dp))

        PrimaryButton(title = "Entendido, continuar", onClick = onAccept)

        Text(
            text = "Ver política de uso y fuentes de datos",
            style = AppType.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Theme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onViewPolicy)
                .padding(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
        )

        Text(
            text = "Ración · Tu Diario de Mercado",
            style = AppType.labelSmall,
            color = Theme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 20.dp)
        )
    }
}

@Composable
private fun SportStep(
    selected: SportFocus,
    onSelect: (SportFocus) -> Unit,
    onStart: () -> Unit
) {
    ScreenContainer(bottomPadding = 32.dp) {
        Text(
            text = "¿Para qué entrenas?",
            style = AppType.display,
            color = Theme.colors.onSurface
        )
        Text(
            text = "Ajustamos tus metas de calorías y macros según tu actividad principal.",
            style = AppType.body,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(Modifier.height(18.dp))

        SportFocusPicker(selected = selected, onSelect = onSelect)

        Spacer(Modifier.height(20.dp))

        PrimaryButton(title = "Empezar", onClick = onStart)
    }
}
