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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.racion.app.shared.di.AppContainer
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.BackRow
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.StatusChip
import com.racion.app.ui.theme.Theme
import kotlinx.coroutines.delay

/**
 * Pantalla 09 · Descripción y créditos.
 *
 * Concentra la identidad de la app, el equipo, las fuentes de datos y dos
 * preferencias reales: el modo oscuro persistido y la restauración del diario de
 * demostración.
 */
class CreditsScreen(
    private val container: AppContainer
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CreditsContent(container = container, onBack = { navigator.pop() })
    }
}

@Composable
fun CreditsContent(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { container.creditsViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()

    LaunchedEffect(ui.message) {
        if (ui.message != null) {
            delay(2_500)
            vm.clearMessage()
        }
    }

    val team = listOf("Daniel Garcia", "Julian Carvajal")

    ScreenContainer {
        BackRow(title = "Créditos", onBack = onBack)

        Spacer(Modifier.height(18.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Theme.colors.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("🍲", style = AppType.display)
            }
            Column {
                Text("Ración", style = AppType.headlineLarge, color = Theme.colors.onSurface)
                Text(
                    "Tu Diario de Mercado",
                    style = AppType.bodyMedium,
                    color = Theme.colors.onSurfaceVariant
                )
            }
        }

        StatusChip(
            text = "VERSIÓN 1.0 · ENTREGA ACADÉMICA",
            modifier = Modifier.padding(top = 14.dp)
        )

        Text(
            text = "Ración es una app para registrar lo que comés, comparar las calorías y los macros con tus metas y ver un informe semanal. Está pensada para el mercado colombiano: los platos del catálogo local son arepa de choclo, bandeja paisa, sancocho y patacón, entre otros.",
            style = AppType.body,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(Modifier.height(26.dp))

        SectionLabel("EQUIPO")

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            team.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Theme.cardShape)
                        .background(Theme.colors.surfaceContainerLowest)
                        .border(1.dp, Theme.colors.outlineVariant, Theme.cardShape)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Theme.colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials(member),
                            style = AppType.titleMedium,
                            color = Theme.colors.onPrimary
                        )
                    }
                    Column {
                        Text(member, style = AppType.titleLarge, color = Theme.colors.onSurface)
                        Text(
                            text = "Desarrollo multiplataforma · KMP",
                            style = AppType.bodySmall,
                            color = Theme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(26.dp))

        SectionLabel("TECNOLOGÍA")

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TechRow("🧩", "Kotlin Multiplatform + Compose Multiplatform")
            TechRow("🧭", "Navegación compartida con Voyager")
            TechRow("🌐", "Ktor + kotlinx.serialization (Open Food Facts)")
            TechRow("💾", "Persistencia local con multiplatform-settings")
        }

        Spacer(Modifier.height(26.dp))

        SectionLabel("FUENTES DE DATOS")

        Text(
            text = "Los productos envasados se consultan en vivo a Open Food Facts (base colaborativa, licencia ODbL). Si no hay conexión, la app usa un catálogo local con platos típicos colombianos. Las metas y los valores nutricionales son una guía y no reemplazan a un profesional de la salud.",
            style = AppType.bodyMedium,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(26.dp))

        SectionLabel("PREFERENCIAS")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(Theme.cardShape)
                .background(Theme.colors.surfaceContainerLowest)
                .border(1.dp, Theme.colors.outlineVariant, Theme.cardShape)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Modo oscuro", style = AppType.titleMedium, color = Theme.colors.onSurface)
                Text(
                    text = "Se guarda en settings.json y se mantiene al reiniciar",
                    style = AppType.bodySmall,
                    color = Theme.colors.onSurfaceVariant
                )
            }
            Switch(
                checked = ui.isDarkMode,
                onCheckedChange = vm::setDarkMode,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Theme.colors.onPrimary,
                    checkedTrackColor = Theme.colors.primary
                )
            )
        }

        if (ui.message != null) {
            InfoBanner(
                message = ui.message.orEmpty(),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Text(
            text = "Tu diario se guarda en el dispositivo y, si iniciaste sesión con una cuenta, también en Firebase. No se recolecta otra información personal.",
            style = AppType.bodySmall,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 20.dp, bottom = 24.dp)
        )
    }

}

@Composable
private fun TechRow(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Theme.colors.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = AppType.bodySmall)
        }
        Text(text, style = AppType.bodyMedium, color = Theme.colors.onSurface)
    }
}

private fun initials(name: String): String = name
    .split(" ")
    .mapNotNull { it.firstOrNull()?.toString() }
    .take(2)
    .joinToString("")
