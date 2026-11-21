package com.racion.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paleta de la marca. Son los mismos colores del proyecto iOS/SwiftUI, que a su vez
 * vienen del esquema Material 3 del proyecto Android original.
 */
@Immutable
data class RacionColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val surface: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val cameraChrome: Color,
    val cameraText: Color,
    val cameraTextMuted: Color
)

val LightRacionColors = RacionColors(
    primary = Color(0xFFE05A2B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF7D9C9),
    secondary = Color(0xFF2F6B4F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCEAE1),
    tertiary = Color(0xFFE3A93E),
    tertiaryContainer = Color(0xFFF6E7C8),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    surface = Color(0xFFF7F2E9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF7EF),
    surfaceContainerHighest = Color(0xFFEFE8D8),
    onSurface = Color(0xFF1C1A16),
    onSurfaceVariant = Color(0xFF8A8272),
    outline = Color(0xFF857A69),
    outlineVariant = Color(0xFFE7DFCF),
    inverseSurface = Color(0xFF1C1A16),
    inverseOnSurface = Color(0xFFF7F2E9),
    cameraChrome = Color(0xFF0D0D0C),
    cameraText = Color(0xFFF7F2E9),
    cameraTextMuted = Color(0xFFB8B2A2)
)

val DarkRacionColors = RacionColors(
    primary = Color(0xFFFFB59C),
    onPrimary = Color(0xFF5B1C06),
    primaryContainer = Color(0xFF7A2E12),
    secondary = Color(0xFFA6C9B5),
    onSecondary = Color(0xFF133526),
    secondaryContainer = Color(0xFF2C4A3A),
    tertiary = Color(0xFFFFD98A),
    tertiaryContainer = Color(0xFF554413),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    surface = Color(0xFF1C1A16),
    surfaceContainerLowest = Color(0xFF13110E),
    surfaceContainerLow = Color(0xFF211F1A),
    surfaceContainerHighest = Color(0xFF3A352B),
    onSurface = Color(0xFFF7F2E9),
    onSurfaceVariant = Color(0xFFCCC4B4),
    outline = Color(0xFF968F80),
    outlineVariant = Color(0xFF4B453B),
    inverseSurface = Color(0xFFF7F2E9),
    inverseOnSurface = Color(0xFF1C1A16),
    cameraChrome = Color(0xFF0D0D0C),
    cameraText = Color(0xFFF7F2E9),
    cameraTextMuted = Color(0xFFB8B2A2)
)

val LocalRacionColors = staticCompositionLocalOf { LightRacionColors }

/** Acceso corto a la paleta, igual que `Theme` en SwiftUI. */
object Theme {
    val colors: RacionColors
        @Composable
        @ReadOnlyComposable
        get() = LocalRacionColors.current

    val cardRadius: Dp = 18.dp
    val cornerRadius: Dp = 24.dp

    val cardShape: RoundedCornerShape get() = RoundedCornerShape(cardRadius)
    val largeShape: RoundedCornerShape get() = RoundedCornerShape(cornerRadius)
}

/** Escala tipográfica: serif para títulos editoriales, sans para el cuerpo. */
object AppType {
    val display = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 34.sp,
        fontWeight = FontWeight.SemiBold
    )
    val displayNumber = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 48.sp,
        fontWeight = FontWeight.SemiBold
    )
    val headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 26.sp,
        fontWeight = FontWeight.SemiBold
    )
    val headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    )

    val titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    val titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    val titleSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

    val body = TextStyle(fontSize = 15.sp)
    val bodyMedium = TextStyle(fontSize = 13.sp)
    val bodySmall = TextStyle(fontSize = 12.sp)

    val label = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    val labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium)
}

private val RacionTypography = Typography()

@Composable
fun RacionTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkRacionColors else LightRacionColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            tertiary = colors.tertiary,
            tertiaryContainer = colors.tertiaryContainer,
            error = colors.error,
            errorContainer = colors.errorContainer,
            background = colors.surface,
            onBackground = colors.onSurface,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceContainerHighest,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant,
            inverseSurface = colors.inverseSurface,
            inverseOnSurface = colors.inverseOnSurface
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            tertiary = colors.tertiary,
            tertiaryContainer = colors.tertiaryContainer,
            error = colors.error,
            errorContainer = colors.errorContainer,
            background = colors.surface,
            onBackground = colors.onSurface,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceContainerHighest,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant,
            inverseSurface = colors.inverseSurface,
            inverseOnSurface = colors.inverseOnSurface
        )
    }

    CompositionLocalProvider(LocalRacionColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = RacionTypography,
            content = content
        )
    }
}
