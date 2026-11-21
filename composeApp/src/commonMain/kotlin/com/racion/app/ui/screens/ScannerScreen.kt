package com.racion.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.racion.app.shared.data.AppFormat
import com.racion.app.shared.di.AppContainer
import com.racion.app.shared.models.FoodProduct
import com.racion.app.ui.theme.AppType
import com.racion.app.ui.theme.ErrorBanner
import com.racion.app.ui.theme.InfoBanner
import com.racion.app.ui.theme.LoadingRow
import com.racion.app.ui.theme.RacionDivider
import com.racion.app.ui.theme.RacionTextField
import com.racion.app.ui.theme.ScreenContainer
import com.racion.app.ui.theme.SectionLabel
import com.racion.app.ui.theme.SurfaceCard
import com.racion.app.ui.theme.Theme

/**
 * Pantalla 05 · Escanear.
 *
 * La cámara física vive en `BarcodeCamera` (expect/actual, solo Android por ahora).
 * La lectura simulada usa el mismo camino de resolución que un código detectado
 * (`ScannerViewModel.resolve`), incluido el respaldo al catálogo local.
 */
@Composable
fun ScannerContent(
    container: AppContainer,
    onProductFound: (FoodProduct) -> Unit
) {
    val vm = remember { container.scannerViewModel() }
    DisposableEffect(vm) { onDispose { vm.onCleared() } }
    val ui by vm.state.collectAsState()
    val focusManager = LocalFocusManager.current

    // El ViewModel entrega el producto una sola vez para que volver atrás no
    // dispare una segunda navegación.
    LaunchedEffect(ui.foundProduct) {
        ui.foundProduct?.let { product ->
            vm.consumeFoundProduct()
            onProductFound(product)
        }
    }

    ScreenContainer(bottomPadding = 96.dp) {
        Text(
            text = "Escanea el producto",
            style = AppType.headlineLarge,
            color = Theme.colors.onSurface
        )
        Text(
            text = "Apunta la cámara al código de barras del empaque o escribilo abajo.",
            style = AppType.bodyMedium,
            color = Theme.colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(18.dp))

        Viewfinder(
            onClick = { vm.simulateScan() },
            onCode = { if (!ui.isLoading) vm.handleDetectedCode(it) }
        )

        if (ui.isLoading) {
            LoadingRow(text = "Consultando Open Food Facts…")
        } else {
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(14.dp))

        SectionLabel("INGRESAR CÓDIGO MANUALMENTE")

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RacionTextField(
                value = ui.barcode,
                onValueChange = vm::setBarcode,
                placeholder = "7801234567890",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        vm.lookup()
                        focusManager.clearFocus()
                    }
                )
            )

            Box(
                modifier = Modifier
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Theme.colors.primary)
                    .clickable {
                        focusManager.clearFocus()
                        vm.lookup()
                    }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Buscar", style = AppType.titleMedium, color = Theme.colors.onPrimary)
            }
        }

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

        Spacer(Modifier.height(22.dp))

        SurfaceCard {
            Text(
                text = "ESCANEADO RECIENTEMENTE",
                style = AppType.label,
                color = Theme.colors.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            if (ui.recentScans.isEmpty()) {
                Text(
                    text = "Todavía no registraste productos.",
                    style = AppType.bodyMedium,
                    color = Theme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                ui.recentScans.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Theme.colors.surfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(entry.product.emoji, style = AppType.bodyMedium)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.product.name,
                                style = AppType.titleMedium,
                                color = Theme.colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = AppFormat.timeAgo(entry.loggedAtEpochMillis).uppercase(),
                                style = AppType.labelSmall,
                                color = Theme.colors.onSurfaceVariant
                            )
                        }
                        Text(
                            text = entry.totalNutrition.kcal.toString(),
                            style = AppType.titleLarge,
                            color = Theme.colors.onSurface
                        )
                    }
                    if (index != ui.recentScans.lastIndex) RacionDivider()
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Visor negro con marco de esquinas amarillas y láser animado.
 *
 * Con cámara (Android) muestra la vista previa debajo del marco y lee códigos reales.
 * Sin cámara (iOS/Desktop) tocar el visor simula una lectura con el producto destacado.
 */
@Composable
private fun Viewfinder(onClick: () -> Unit, onCode: (String) -> Unit) {
    val transition = rememberInfiniteTransition(label = "laser")
    val laserProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser-progress"
    )

    val cornerColor = Theme.colors.tertiary
    val laserColor = Theme.colors.primary
    val chrome = Theme.colors.cameraChrome
    val mutedText = Theme.colors.cameraTextMuted

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(Theme.largeShape)
            .background(chrome)
            .then(if (hasBarcodeCamera) Modifier else Modifier.clickable(onClick = onClick))
    ) {
        BarcodeCamera(modifier = Modifier.fillMaxSize(), onCode = onCode)

        val framePadding = 22.dp
        val travel = (maxHeight - 52.dp).coerceAtLeast(1.dp)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val pad = framePadding.toPx()
            val length = minOf(size.width, size.height) * 0.18f
            val left = pad
            val right = size.width - pad
            val top = pad
            val bottom = size.height - pad
            val strokeWidth = 5.dp.toPx()

            drawCorner(listOf(Offset(left, top + length), Offset(left, top), Offset(left + length, top)), cornerColor, strokeWidth)
            drawCorner(listOf(Offset(right - length, top), Offset(right, top), Offset(right, top + length)), cornerColor, strokeWidth)
            drawCorner(listOf(Offset(left, bottom - length), Offset(left, bottom), Offset(left + length, bottom)), cornerColor, strokeWidth)
            drawCorner(listOf(Offset(right - length, bottom), Offset(right, bottom), Offset(right, bottom - length)), cornerColor, strokeWidth)

            val laserY = 26.dp.toPx() + travel.toPx() * laserProgress
            drawLine(
                color = laserColor.copy(alpha = 0.85f),
                start = Offset(left, laserY),
                end = Offset(right, laserY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasBarcodeCamera) "Centrá el código de barras en el marco"
                else "Simulador: tocá el visor para simular una lectura",
                style = AppType.labelSmall,
                color = mutedText
            )
        }
    }
}

/** Dibuja una esquina como una polilínea de tres puntos. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCorner(
    points: List<Offset>,
    color: androidx.compose.ui.graphics.Color,
    strokeWidth: Float
) {
    points.zipWithNext().forEach { (start, end) ->
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
