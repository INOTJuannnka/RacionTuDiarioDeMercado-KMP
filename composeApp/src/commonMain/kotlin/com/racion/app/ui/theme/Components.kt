package com.racion.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contenedor de pantalla: fondo crema, scroll vertical y el padding horizontal del
 * diseño (20 dp).
 */
@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 32.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Theme.colors.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = bottomPadding),
        content = content
    )
}

/** Cabecera de una pantalla apilada: botón Atrás + título. */
@Composable
fun BackRow(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Theme.colors.surfaceContainerHighest)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("←", style = AppType.titleLarge, color = Theme.colors.onSurface)
        }
        Text(
            text = title,
            style = AppType.titleLarge,
            color = Theme.colors.onSurface
        )
    }
}

/** Botón primario naranja de ancho completo (52 dp). */
@Composable
fun PrimaryButton(
    title: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled && !isLoading) Theme.colors.primary
                else Theme.colors.primary.copy(alpha = 0.4f)
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Theme.colors.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = title,
                style = AppType.titleLarge,
                color = Theme.colors.onPrimary
            )
        }
    }
}

/** Etiqueta de sección en mayúsculas pequeñas ("LO DE HOY"). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = AppType.label,
        color = Theme.colors.onSurfaceVariant
    )
}

/** Pastilla seleccionable tipo tab (Almuerzo / Frituras / Sopas...). */
@Composable
fun PillTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (isSelected) Theme.colors.inverseSurface
                else Theme.colors.surfaceContainerHighest
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = AppType.titleMedium,
            color = if (isSelected) Theme.colors.inverseOnSurface else Theme.colors.onSurfaceVariant
        )
    }
}

/** Pastilla rectangular para Desayuno / Almuerzo / Snack / Cena. */
@Composable
fun OptionPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) Theme.colors.inverseSurface else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) Theme.colors.inverseSurface else Theme.colors.outlineVariant,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = AppType.titleMedium,
            color = if (isSelected) Theme.colors.inverseOnSurface else Theme.colors.onSurface
        )
    }
}

/** Botón flotante circular naranja ("+ agregar comida"). */
@Composable
fun OrangeFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(12.dp, CircleShape, ambientColor = Theme.colors.primary.copy(alpha = 0.4f))
            .clip(CircleShape)
            .background(Theme.colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "＋",
            style = AppType.headlineLarge,
            color = Theme.colors.onPrimary
        )
    }
}

// MARK: - Datos

/** Chip de macro con barra de color (resumen del día). */
@Composable
fun MacroStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(value, style = AppType.titleLarge, color = Theme.colors.onSurface)
        Text(
            text = label.uppercase(),
            style = AppType.labelSmall,
            color = Theme.colors.onSurfaceVariant
        )
    }
}

/** Fila de comida (Inicio y Diario). */
@Composable
fun MealRow(
    emoji: String,
    name: String,
    meta: String,
    kcal: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Theme.colors.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = AppType.body)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = AppType.titleMedium,
                color = Theme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = meta.uppercase(),
                style = AppType.labelSmall,
                color = Theme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = kcal.toString(),
            style = AppType.titleLarge,
            color = Theme.colors.onSurface
        )
    }
}

/** Tarjeta oscura con métrica grande (cabecera de "Confirmar"). */
@Composable
fun DarkStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Theme.largeShape)
            .background(Theme.colors.inverseSurface)
            .padding(20.dp)
    ) {
        Text(value, style = AppType.display, color = Theme.colors.inverseOnSurface)
        Text(
            text = label.uppercase(),
            style = AppType.label,
            color = Theme.colors.inverseOnSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

/** Tarjeta blanca/crema con borde suave. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Theme.cardShape)
            .background(Theme.colors.surfaceContainerLowest)
            .border(1.dp, Theme.colors.outlineVariant, Theme.cardShape)
            .padding(16.dp),
        content = content
    )
}

/** Chip informativo con punto de estado. */
@Composable
fun StatusChip(
    text: String,
    color: Color = Theme.colors.secondary,
    background: Color = Theme.colors.secondaryContainer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text, style = AppType.titleSmall, color = color)
    }
}

/** Tarjeta de porcentaje de macros. */
@Composable
fun PercentCard(
    value: String,
    label: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(Theme.cardShape)
            .background(background)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = AppType.headlineMedium, color = foreground)
        Text(
            text = label.uppercase(),
            style = AppType.labelSmall,
            color = foreground
        )
    }
}

/** Píldora de resumen con valor grande y etiqueta (Informe). */
@Composable
fun SummaryPill(
    value: String,
    label: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(Theme.cardShape)
            .background(background)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = AppType.headlineMedium, color = foreground)
        Text(
            text = label,
            style = AppType.labelSmall,
            color = foreground
        )
    }
}

// MARK: - Estados

@Composable
fun LoadingRow(text: String = "Cargando…", modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = Theme.colors.primary,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = AppType.bodyMedium, color = Theme.colors.onSurfaceVariant)
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Theme.colors.errorContainer)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("⚠️", style = AppType.bodyMedium)
        Text(
            text = message,
            style = AppType.bodyMedium,
            color = Theme.colors.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun InfoBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Theme.colors.primaryContainer.copy(alpha = 0.6f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("ℹ️", style = AppType.bodySmall)
        Text(
            text = message,
            style = AppType.bodySmall,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(emoji, style = AppType.display)
        Text(title, style = AppType.titleLarge, color = Theme.colors.onSurface)
        Text(
            text = message,
            style = AppType.bodyMedium,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun RacionDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Theme.colors.outlineVariant)
    )
}

/** Campo de texto con el aspecto del diseño (fondo blanco, borde suave). */
@Composable
fun RacionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                style = AppType.body,
                color = Theme.colors.onSurfaceVariant
            )
        },
        textStyle = AppType.body,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation
    )
}
