package com.racion.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.DiaryEntry
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.EmptyState
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.MealRow
import com.racion.app.ui.theme.PrimaryButton
import com.racion.app.ui.theme.RacionDivider
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.SurfaceCard
import com.racion.app.ui.theme.Theme
import kotlinx.coroutines.delay

/**
 * Pantalla 02 · Diario. Historial completo, filtro "solo hoy", edición de porciones
 * y borrado. Persistencia delegada a `AppStore`.
 */
@Composable
fun DiaryContent(
    container: AppContainer,
    onAddFood: () -> Unit
) {
    val vm = remember { container.diaryViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ui.message) {
        if (ui.message != null) {
            delay(2_000)
            vm.clearMessage()
        }
    }

    ScreenContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tu diario",
                    style = AppType.display,
                    color = Theme.colors.onSurface
                )
                Text(
                    text = "Todo lo que registraste, con la porción exacta.",
                    style = AppType.bodyMedium,
                    color = Theme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = "Vaciar",
                style = AppType.titleSmall,
                color = Theme.colors.error,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showClearDialog = true }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        SurfaceCard {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ui.totalKcal.toString(),
                        style = AppType.headlineLarge,
                        color = Theme.colors.onSurface
                    )
                    Text(
                        text = "KCAL EN ESTA VISTA",
                        style = AppType.labelSmall,
                        color = Theme.colors.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ui.totalServings.toString(),
                        style = AppType.headlineLarge,
                        color = Theme.colors.onSurface
                    )
                    Text(
                        text = "PORCIONES",
                        style = AppType.labelSmall,
                        color = Theme.colors.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Solo hoy",
                    style = AppType.titleMedium,
                    color = Theme.colors.onSurface
                )
                Text(
                    text = if (ui.onlyToday) "Mostrando el día en curso"
                    else "Mostrando el historial completo",
                    style = AppType.bodySmall,
                    color = Theme.colors.onSurfaceVariant
                )
            }
            Switch(
                checked = ui.onlyToday,
                onCheckedChange = vm::setOnlyToday,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Theme.colors.onPrimary,
                    checkedTrackColor = Theme.colors.primary
                )
            )
        }

        SectionLabel("REGISTROS")
        Spacer(Modifier.height(8.dp))

        if (ui.entries.isEmpty()) {
            EmptyState(
                emoji = "📖",
                title = "No hay registros",
                message = if (ui.onlyToday) {
                    "Hoy todavía no agregaste comidas. Podés desactivar \"Solo hoy\" para ver el historial."
                } else {
                    "Agregá tu primera comida desde el botón de abajo."
                }
            )
        } else {
            ui.entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MealRow(
                        emoji = entry.product.emoji,
                        name = entry.product.name,
                        meta = AppFormat.mealRowMeta(entry),
                        kcal = entry.totalNutrition.kcal,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { vm.beginEdit(entry) }
                            .padding(vertical = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { vm.delete(entry) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑", style = AppType.bodySmall)
                    }
                }
                if (index != ui.entries.lastIndex) RacionDivider()
            }

            Text(
                text = "Tocá una fila para ajustar sus porciones.",
                style = AppType.bodySmall,
                color = Theme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (ui.message != null) {
            InfoBanner(message = ui.message.orEmpty(), modifier = Modifier.padding(top = 12.dp))
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(title = "Agregar comida", onClick = onAddFood)
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("¿Vaciar todo el diario?", style = AppType.headlineMedium) },
            text = {
                Text(
                    "Se eliminarán todas las entradas, incluidos los días anteriores. No se puede deshacer.",
                    style = AppType.bodyMedium
                )
            },
            confirmButton = {
                Text(
                    text = "Vaciar diario",
                    style = AppType.titleMedium,
                    color = Theme.colors.error,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            showClearDialog = false
                            vm.deleteAll()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancelar",
                    style = AppType.titleMedium,
                    color = Theme.colors.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showClearDialog = false }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            },
            containerColor = Theme.colors.surfaceContainerLowest
        )
    }

    val editing = ui.editingEntry
    if (editing != null) {
        DiaryEditDialog(
            entry = editing,
            servings = ui.editingServings,
            onServingsChange = vm::setEditingServings,
            onCancel = vm::cancelEdit,
            onSave = vm::commitEdit
        )
    }
}

@Composable
private fun DiaryEditDialog(
    entry: DiaryEntry,
    servings: Int,
    onServingsChange: (Int) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val preview = entry.product.nutritionForUnits(servings)

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Editar entrada", style = AppType.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Theme.colors.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(entry.product.emoji, style = AppType.headlineMedium)
                    }
                    Column {
                        Text(entry.product.name, style = AppType.titleLarge)
                        Text(
                            text = entry.mealSlot.label.uppercase(),
                            style = AppType.labelSmall,
                            color = Theme.colors.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Porciones",
                        style = AppType.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    StepButton(text = "−") { onServingsChange(servings - 1) }
                    Text(
                        text = AppFormat.unitsLabel(servings),
                        style = AppType.titleLarge,
                        color = Theme.colors.primary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    StepButton(text = "+") { onServingsChange(servings + 1) }
                }

                SurfaceCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniValue(preview.kcal.toString(), "KCAL")
                        MiniValue("${preview.carbsG.toInt()} g", "CARBOS")
                        MiniValue("${preview.proteinG.toInt()} g", "PROTEÍNA")
                        MiniValue("${preview.fatG.toInt()} g", "GRASAS")
                    }
                }
            }
        },
        confirmButton = {
            Text(
                text = "Guardar cambios",
                style = AppType.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Theme.colors.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onSave)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        },
        dismissButton = {
            Text(
                text = "Cancelar",
                style = AppType.titleMedium,
                color = Theme.colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        },
        containerColor = Theme.colors.surfaceContainerLowest
    )
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Theme.colors.surfaceContainerHighest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = AppType.titleLarge, color = Theme.colors.onSurface)
    }
}

@Composable
private fun MiniValue(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AppType.titleLarge, color = Theme.colors.onSurface)
        Text(label, style = AppType.labelSmall, color = Theme.colors.onSurfaceVariant)
    }
}
