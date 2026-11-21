package com.racion.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.SportFocus
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.BackRow
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.PrimaryButton
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.Theme

/**
 * Pantalla 06 · Perfil deportivo.
 *
 * Las opciones salen del enum `SportFocus` completo: agregar un enfoque nuevo no
 * puede dejar la pantalla incompleta. El elegido ajusta los macros sugeridos.
 */
class SportProfileScreen(
    private val container: AppContainer
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SportProfileContent(container = container, onBack = { navigator.pop() })
    }
}

@Composable
fun SportProfileContent(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { container.sportProfileViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()

    ScreenContainer {
        BackRow(title = "Perfil deportivo", onBack = onBack)

        Spacer(Modifier.height(16.dp))

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

        SportFocusPicker(
            selected = ui.selected,
            onSelect = vm::select
        )

        InfoBanner(
            message = ui.macroPreviewLabel,
            modifier = Modifier.padding(top = 16.dp)
        )

        PrimaryButton(
            title = "Guardar perfil",
            modifier = Modifier.padding(top = 20.dp),
            onClick = {
                if (vm.apply()) onBack()
            }
        )

        Text(
            text = "Podés cambiarlo cuando quieras: el perfil no bloquea el diario.",
            style = AppType.bodySmall,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * Grilla de enfoques reutilizada por esta pantalla y por el onboarding.
 * Se arma con filas de dos porque vive dentro de un `Column` con scroll vertical.
 */
@Composable
fun SportFocusPicker(
    selected: SportFocus,
    onSelect: (SportFocus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SportFocus.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                row.forEach { focus ->
                    FocusCard(
                        focus = focus,
                        isSelected = focus == selected,
                        onClick = { onSelect(focus) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FocusCard(
    focus: SportFocus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isSelected) Theme.colors.primaryContainer
                else Theme.colors.surfaceContainerLowest
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Theme.colors.primary else Theme.colors.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Theme.colors.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(focus.emoji, style = AppType.titleMedium)
        }

        Text(
            text = focus.title,
            style = AppType.titleLarge,
            color = Theme.colors.onSurface,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            text = focus.detail,
            style = AppType.labelSmall,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
