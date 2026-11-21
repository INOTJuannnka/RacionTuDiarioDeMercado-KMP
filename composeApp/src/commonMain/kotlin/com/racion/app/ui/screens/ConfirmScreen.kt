package com.racion.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.FoodProduct
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.BackRow
import com.racion.app.ui.theme.DarkStatCard
import com.racion.app.ui.theme.OptionPill
import com.racion.app.ui.theme.PrimaryButton
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.StatusChip
import com.racion.app.ui.theme.Theme
import kotlin.math.roundToInt

/**
 * Pantalla 04 · Confirmar.
 *
 * El número que se muestra y el que se guarda salen del mismo método
 * (`FoodProduct.nutritionForUnits`), así que no puede repetirse el doble redondeo
 * del proyecto original.
 */
class ConfirmScreen(
    private val container: AppContainer,
    private val product: FoodProduct
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ConfirmContent(
            container = container,
            product = product,
            onBack = { navigator.pop() },
            onAdded = { navigator.popUntilRoot() }
        )
    }
}

@Composable
fun ConfirmContent(
    container: AppContainer,
    product: FoodProduct,
    onBack: () -> Unit,
    onAdded: () -> Unit
) {
    val vm = remember(container, product) { container.confirmViewModel(product) }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()

    ScreenContainer {
        BackRow(title = "Confirmar", onBack = onBack)

        Spacer(Modifier.height(14.dp))

        StatusChip(
            text = ui.originLabel.uppercase(),
            color = Theme.colors.primary,
            background = Theme.colors.primaryContainer
        )

        if (ui.nutriscoreLabel != null) {
            StatusChip(
                text = ui.nutriscoreLabel.orEmpty(),
                color = Theme.colors.secondary,
                background = Theme.colors.secondaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            text = product.name,
            style = AppType.headlineLarge,
            color = Theme.colors.onSurface,
            modifier = Modifier.padding(top = 14.dp)
        )

        Text(
            text = ui.ingredients,
            style = AppType.body,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Theme.cardShape)
                .background(Theme.colors.surfaceContainerHighest)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Porciones", style = AppType.titleMedium, color = Theme.colors.onSurface)
                Text(
                    text = ui.gramsLabel,
                    style = AppType.bodySmall,
                    color = Theme.colors.onSurfaceVariant
                )
            }
            StepButton(text = "−") { vm.setServings(ui.servings - 1) }
            Text(
                text = AppFormat.unitsLabel(ui.servings),
                style = AppType.titleLarge,
                color = Theme.colors.primary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            StepButton(text = "+") { vm.setServings(ui.servings + 1) }
        }

        Spacer(Modifier.height(20.dp))

        DarkStatCard(value = ui.total.kcal.toString(), label = "CALORÍAS TOTALES") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InverseStat("${ui.total.carbsG.roundToInt()}g", "Carbos")
                InverseStat("${ui.total.proteinG.roundToInt()}g", "Proteína")
                InverseStat("${ui.total.fatG.roundToInt()}g", "Grasas")
            }
        }

        Spacer(Modifier.height(22.dp))

        SectionLabel("¿A QUÉ COMIDA?")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            vm.mealSlots.forEach { slot ->
                OptionPill(
                    text = slot.label,
                    isSelected = ui.mealSlot == slot,
                    onClick = { vm.selectMealSlot(slot) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        PrimaryButton(
            title = "Agregar al diario",
            onClick = {
                vm.add()
                onAdded()
            }
        )

        Text(
            text = "El total se guarda calculado con las porciones elegidas; editar la entrada más tarde vuelve a calcularlo desde el producto.",
            style = AppType.bodySmall,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Theme.colors.surfaceContainerLowest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = AppType.titleLarge, color = Theme.colors.onSurface)
    }
}

@Composable
private fun InverseStat(value: String, label: String) {
    Column {
        Text(value, style = AppType.titleLarge, color = Theme.colors.inverseOnSurface)
        Text(
            text = label,
            style = AppType.labelSmall,
            color = Theme.colors.inverseOnSurface.copy(alpha = 0.7f)
        )
    }
}
