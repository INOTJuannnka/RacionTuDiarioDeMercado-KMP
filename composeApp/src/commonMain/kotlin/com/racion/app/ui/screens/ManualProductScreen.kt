package com.racion.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.FoodProduct
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.BackRow
import com.racion.app.ui.theme.ErrorBanner
import com.racion.app.ui.theme.PrimaryButton
import com.racion.app.ui.theme.RacionTextField
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.Theme

/**
 * Carga manual de un producto que Open Food Facts no conoce.
 *
 * Al guardar reemplaza esta pantalla por Confirmar, igual que un producto encontrado.
 * Una vez registrado, volver a escanear el mismo código lo encuentra sin red.
 */
class ManualProductScreen(
    private val container: AppContainer,
    private val barcode: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var name by remember { mutableStateOf("") }
        var kcal by remember { mutableStateOf("") }
        var carbs by remember { mutableStateOf("") }
        var protein by remember { mutableStateOf("") }
        var fat by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        ScreenContainer {
            BackRow(title = "Cargar producto", onBack = { navigator.pop() })
            Text(
                text = "El código $barcode no está en Open Food Facts. Copiá los datos de la tabla nutricional del empaque.",
                style = AppType.bodyMedium,
                color = Theme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            RacionTextField(value = name, onValueChange = { name = it }, placeholder = "Nombre del producto")

            Spacer(Modifier.height(18.dp))
            SectionLabel("Por porción", Modifier.padding(bottom = 8.dp))

            NumberField(kcal, { kcal = it }, "Calorías (kcal)")
            NumberField(carbs, { carbs = it }, "Carbohidratos (g)")
            NumberField(protein, { protein = it }, "Proteína (g)")
            NumberField(fat, { fat = it }, "Grasas (g)")

            error?.let { ErrorBanner(message = it, modifier = Modifier.padding(top = 12.dp)) }

            PrimaryButton(
                title = "Continuar",
                modifier = Modifier.padding(top = 18.dp),
                onClick = {
                    val product = FoodProduct.manual(barcode, name, kcal, carbs, protein, fat)
                    if (product == null) {
                        error = "Completá el nombre y usá solo números en los valores."
                    } else {
                        navigator.replace(ConfirmScreen(container, product))
                    }
                }
            )
        }
    }
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    RacionTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
