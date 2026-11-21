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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.FoodProduct
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.BackRow
import com.racion.app.ui.theme.EmptyState
import com.racion.app.ui.theme.ErrorBanner
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.LoadingRow
import com.racion.app.ui.theme.PillTab
import com.racion.app.ui.theme.RacionDivider
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.RacionTextField
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.Theme

/**
 * Pantalla 03 · Agregar alimento.
 *
 * La búsqueda tiene debounce en `AddFoodViewModel` y cae al catálogo local cuando
 * Open Food Facts falla. Cada resultado apila la pantalla de confirmación.
 */
class AddFoodScreen(
    private val container: AppContainer
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AddFoodContent(
            container = container,
            onBack = { navigator.pop() },
            onProductSelected = { product -> navigator.push(ConfirmScreen(container, product)) }
        )
    }
}

@Composable
fun AddFoodContent(
    container: AppContainer,
    onBack: () -> Unit,
    onProductSelected: (FoodProduct) -> Unit
) {
    val vm = remember { container.addFoodViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()
    val focusManager = LocalFocusManager.current

    ScreenContainer {
        BackRow(title = "¿Qué comiste?", onBack = onBack)

        Spacer(Modifier.height(14.dp))

        RacionTextField(
            value = ui.query,
            onValueChange = vm::onQueryChange,
            placeholder = "Busca arepa, ajiaco, buñuelo…",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    vm.submit()
                    focusManager.clearFocus()
                }
            ),
            trailingIcon = {
                if (ui.query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                vm.onQueryChange("")
                                focusManager.clearFocus()
                            }
                    ) {
                        Text(
                            text = "✕",
                            style = AppType.titleMedium,
                            color = Theme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        )

        if (ui.errorMessage != null) {
            ErrorBanner(
                message = ui.errorMessage.orEmpty(),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        if (ui.notice != null) {
            InfoBanner(
                message = ui.notice.orEmpty(),
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ui.categories.forEach { category ->
                PillTab(
                    text = category,
                    isSelected = ui.selectedCategory == category,
                    onClick = { vm.selectCategory(category) }
                )
            }
        }

        when {
            ui.isLoading -> LoadingRow(text = "Buscando en Open Food Facts…")

            ui.isEmpty -> EmptyState(
                emoji = "🔎",
                title = "Sin resultados",
                message = "Probá con otro término o quitá el filtro de categoría."
            )

            else -> {
                val row: @Composable (FoodProduct) -> Unit = { food ->
                    FoodResultRow(
                        food = food,
                        isFavorite = ui.isFavorite(food),
                        onToggleFavorite = { vm.toggleFavorite(food) },
                        modifier = Modifier.clickable { onProductSelected(food) }
                    )
                }
                if (ui.showShortcuts) {
                    FoodSection("Favoritos", ui.favorites, row)
                    FoodSection("Recientes", ui.recents, row)
                    if (ui.favorites.isNotEmpty() || ui.recents.isNotEmpty()) SectionLabel("Catálogo", Modifier.padding(bottom = 4.dp))
                }
                ui.results.forEachIndexed { index, food ->
                    row(food)
                    if (index != ui.results.lastIndex) RacionDivider()
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FoodSection(title: String, foods: List<FoodProduct>, row: @Composable (FoodProduct) -> Unit) {
    if (foods.isEmpty()) return
    SectionLabel(title, Modifier.padding(bottom = 4.dp))
    foods.forEachIndexed { index, food ->
        row(food)
        if (index != foods.lastIndex) RacionDivider()
    }
    Spacer(Modifier.height(16.dp))
}


@Composable
private fun FoodResultRow(
    food: FoodProduct,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Si el fabricante declara porción, se muestra esa base; si no, la única
    // honesta es la de 100 g (que es como se guarda `nutritionPer100g`).
    val serving = food.servingGrams
    val shownKcal = if (serving != null) {
        food.nutritionForGrams(serving).kcal
    } else {
        food.nutritionPer100g.kcal
    }
    val unitLabel = if (serving != null) "kcal / porción" else "kcal / 100 g"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Theme.colors.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(food.emoji, style = AppType.headlineMedium)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                style = AppType.titleLarge,
                color = Theme.colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = food.originLabel.uppercase(),
                style = AppType.labelSmall,
                color = Theme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = shownKcal.toString(),
                style = AppType.titleLarge,
                color = Theme.colors.onSurface
            )
            Text(
                text = unitLabel,
                style = AppType.labelSmall,
                color = Theme.colors.onSurfaceVariant
            )
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                tint = if (isFavorite) Theme.colors.primary else Theme.colors.onSurfaceVariant
            )
        }
    }
}
