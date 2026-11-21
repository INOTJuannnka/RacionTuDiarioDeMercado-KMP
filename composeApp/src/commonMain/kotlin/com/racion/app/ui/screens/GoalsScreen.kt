package com.racion.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.AuthProvider
import com.racion.app.shared.models.NutritionGoals
import com.racion.app.shared.models.Weekday
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.PercentCard
import com.racion.app.ui.theme.PrimaryButton
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.SurfaceCard
import com.racion.app.ui.theme.Theme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Pantalla 06 · Metas y perfil. Dos sliders, días activos, macros (derivados del
 * perfil deportivo), recordatorios y accesos a perfil deportivo y créditos.
 */
@Composable
fun GoalsContent(
    container: AppContainer,
    onOpenSportProfile: () -> Unit,
    onOpenCredits: () -> Unit
) {
    val vm = remember { container.goalsViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()

    LaunchedEffect(ui.savedMessage) {
        if (ui.savedMessage != null) {
            delay(2_000)
            vm.clearSavedMessage()
        }
    }

    ScreenContainer {
        Text("Tus metas", style = AppType.display, color = Theme.colors.onSurface)
        Text(
            text = "Ajustá el objetivo y nosotros hacemos las cuentas.",
            style = AppType.bodyMedium,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(22.dp))

        GoalSlider(
            label = "PESO OBJETIVO",
            valueLabel = ui.weightLabel,
            value = ui.targetWeightKg,
            range = NutritionGoals.WEIGHT_RANGE,
            step = 0.5,
            minLabel = "${NutritionGoals.WEIGHT_RANGE.start.toInt()} kg",
            maxLabel = "actual: ${ui.currentWeightKg.toInt()} kg     ${NutritionGoals.WEIGHT_RANGE.endInclusive.toInt()} kg",
            onValueChange = vm::setTargetWeight
        )

        Spacer(Modifier.height(22.dp))

        GoalSlider(
            label = "META CALÓRICA DIARIA",
            valueLabel = ui.kcalLabel,
            value = ui.kcalPerDay,
            range = NutritionGoals.KCAL_RANGE.first.toDouble()..NutritionGoals.KCAL_RANGE.last.toDouble(),
            step = 50.0,
            minLabel = NutritionGoals.KCAL_RANGE.first.toString(),
            maxLabel = NutritionGoals.KCAL_RANGE.last.toString(),
            onValueChange = vm::setKcalPerDay
        )

        Spacer(Modifier.height(24.dp))

        SectionLabel("DÍAS ACTIVOS POR SEMANA")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Weekday.entries.forEach { day ->
                val selected = day in ui.activeDays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            if (selected) Theme.colors.secondary
                            else Theme.colors.surfaceContainerHighest
                        )
                        .clickable { vm.toggleDay(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.short,
                        style = AppType.titleMedium,
                        color = if (selected) Theme.colors.onSecondary
                        else Theme.colors.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionLabel("DISTRIBUCIÓN DE MACROS")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PercentCard(
                value = "${ui.macroSplit.carbsPct}%",
                label = "Carbos",
                background = Theme.colors.primaryContainer,
                foreground = Theme.colors.primary,
                modifier = Modifier.weight(1f)
            )
            PercentCard(
                value = "${ui.macroSplit.proteinPct}%",
                label = "Proteína",
                background = Theme.colors.secondaryContainer,
                foreground = Theme.colors.secondary,
                modifier = Modifier.weight(1f)
            )
            PercentCard(
                value = "${ui.macroSplit.fatPct}%",
                label = "Grasas",
                background = Theme.colors.tertiaryContainer,
                foreground = Theme.colors.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Los macros se recalculan al elegir un perfil deportivo.",
            style = AppType.bodySmall,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Recordatorios", style = AppType.titleMedium)
                Text(
                    text = "Un aviso diario a las 20:00 para completar el diario",
                    style = AppType.bodySmall,
                    color = Theme.colors.onSurfaceVariant
                )
            }
            Switch(
                checked = ui.remindersEnabled,
                onCheckedChange = vm::setReminders,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Theme.colors.onPrimary,
                    checkedTrackColor = Theme.colors.primary
                )
            )
        }

        Spacer(Modifier.height(22.dp))

        PrimaryButton(title = "Guardar metas", onClick = vm::save)

        if (ui.savedMessage != null) {
            InfoBanner(
                message = ui.savedMessage.orEmpty(),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        SectionLabel("TU PERFIL")

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingsRow(
                emoji = "🏃",
                title = "Perfil deportivo",
                subtitle = ui.sportFocusTitle,
                onClick = onOpenSportProfile
            )
            SettingsRow(
                emoji = "ℹ️",
                title = "Descripción y créditos",
                subtitle = "Equipo, fuentes de datos y modo oscuro",
                onClick = onOpenCredits
            )
        }

        Spacer(Modifier.height(24.dp))

        val appState by container.store.state.collectAsState()
        val session = appState.session
        SectionLabel("CUENTA")

        SurfaceCard(modifier = Modifier.padding(top = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("👤", style = AppType.headlineMedium)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session?.label ?: "Sin sesión",
                        style = AppType.titleMedium
                    )
                    Text(
                        text = when (session?.provider) {
                            AuthProvider.GOOGLE -> "Cuenta de Google"
                            AuthProvider.EMAIL -> "Cuenta con correo"
                            else -> "Tus datos viven solo en este dispositivo"
                        },
                        style = AppType.bodySmall,
                        color = Theme.colors.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        SettingsRow(
            emoji = "🚪",
            title = "Cerrar sesión",
            subtitle = "Volver a la pantalla de inicio de sesión",
            onClick = container.store::signOut
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun GoalSlider(
    label: String,
    valueLabel: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    step: Double,
    minLabel: String,
    maxLabel: String,
    onValueChange: (Double) -> Unit
) {
    val steps = (((range.endInclusive - range.start) / step).roundToInt() - 1).coerceAtLeast(0)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(label, modifier = Modifier.weight(1f))
            Text(valueLabel, style = AppType.titleLarge, color = Theme.colors.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Theme.colors.primary,
                activeTrackColor = Theme.colors.primary,
                inactiveTrackColor = Theme.colors.surfaceContainerHighest,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = minLabel,
                style = AppType.labelSmall,
                color = Theme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = maxLabel,
                style = AppType.labelSmall,
                color = Theme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsRow(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Theme.cardShape)
            .background(Theme.colors.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Theme.colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = AppType.bodyMedium)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppType.titleMedium, color = Theme.colors.onSurface)
            Text(
                text = subtitle,
                style = AppType.bodySmall,
                color = Theme.colors.onSurfaceVariant
            )
        }
        Text("›", style = AppType.headlineMedium, color = Theme.colors.onSurfaceVariant)
    }
}
