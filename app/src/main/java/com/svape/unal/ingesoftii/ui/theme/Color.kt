package com.svape.unal.ingesoftii.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


private val DarkPrimary = Color(0xFF4CC9F0)
private val DarkOnPrimary = Color(0xFF06131B)
private val DarkPrimaryContainer = Color(0xFF182E3A)
private val DarkOnPrimaryContainer = Color(0xFF7DDCF6)

private val DarkSecondary = Color(0xFFB4C0DA)
private val DarkOnSecondary = Color(0xFF1E2638)
private val DarkSecondaryContainer = Color(0xFF2A3247)
private val DarkOnSecondaryContainer = Color(0xFFD6DEF2)

private val DarkTertiary = Color(0xFFF5B971)
private val DarkOnTertiary = Color(0xFF2E1A00)
private val DarkTertiaryContainer = Color(0xFF3F2D12)
private val DarkOnTertiaryContainer = Color(0xFFFFDDB3)

private val DarkError = Color(0xFFFF5C5C)
private val DarkOnError = Color(0xFF410002)
private val DarkErrorContainer = Color(0xFF4A1717)
private val DarkOnErrorContainer = Color(0xFFFFB4AB)

private val DarkSurface = Color(0xFF0E1117)
private val DarkOnSurface = Color(0xFFF1F2F6)
private val DarkOnSurfaceVariant = Color(0xFF9DA6BF)
private val DarkSurfaceContainerLowest = Color(0xFF0A0C11)
private val DarkSurfaceContainerLow = Color(0xFF14171F)
private val DarkSurfaceContainer = Color(0xFF191D27)
private val DarkSurfaceContainerHigh = Color(0xFF232838)
private val DarkSurfaceContainerHighest = Color(0xFF2A3042)
private val DarkOutline = Color(0xFF6B7390)
private val DarkOutlineVariant = Color(0xFF2B2F39)
private val DarkInverseSurface = Color(0xFF343C55)

private val LightPrimary = Color(0xFF0B84B8)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFD8F1FA)
private val LightOnPrimaryContainer = Color(0xFF045D82)

private val LightSecondary = Color(0xFF56617A)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFDEE3EE)
private val LightOnSecondaryContainer = Color(0xFF141C2C)

private val LightTertiary = Color(0xFFA15C00)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFFFE2C2)
private val LightOnTertiaryContainer = Color(0xFF3A2200)

private val LightError = Color(0xFFD93636)
private val LightOnError = Color(0xFFFFFFFF)
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF5C0A0A)

private val LightSurface = Color(0xFFFAF7F1)
private val LightInverseSurface = Color(0xFF1E2638)
private val LightOnSurface = Color(0xFF1C1A15)
private val LightOnSurfaceVariant = Color(0xFF6B6252)
private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
private val LightSurfaceContainerLow = Color(0xFFF5F1E7)
private val LightSurfaceContainer = Color(0xFFEFE9DD)
private val LightSurfaceContainerHigh = Color(0xFFEAE3D5)
private val LightSurfaceContainerHighest = Color(0xFFE4DCCC)
private val LightOutline = Color(0xFF857B6A)
private val LightOutlineVariant = Color(0xFFDDDAD3)

internal val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkPrimary,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkOnSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = Color.Black,
    surfaceBright = Color(0xFF2C3140),
    surfaceDim = DarkSurface,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

internal val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = DarkPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightSurface,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightPrimary,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = Color(0xFFF1F3F8),
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = Color.Black,
    surfaceBright = LightSurface,
    surfaceDim = Color(0xFFE0D9CB),
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val card: Color,
    val cardBorder: Color,
    val glow: Color,
)

internal val DarkExtended = ExtendedColors(
    success = Color(0xFF3DDC84),
    onSuccess = Color(0xFF00210F),
    successContainer = Color(0xFF15352A),
    onSuccessContainer = Color(0xFF8AF5B5),
    warning = Color(0xFFF5B971),
    warningContainer = Color(0xFF3F2D12),
    onWarningContainer = Color(0xFFFFDDB3),
    card = DarkSurfaceContainerHigh,
    cardBorder = DarkOutlineVariant,
    glow = DarkPrimary.copy(alpha = 0.22f),
)

internal val LightExtended = ExtendedColors(
    success = Color(0xFF1E9E5A),
    onSuccess = Color.White,
    successContainer = Color(0xFFD5F5E2),
    onSuccessContainer = Color(0xFF0B4A2A),
    warning = Color(0xFFA15C00),
    warningContainer = Color(0xFFFFE2C2),
    onWarningContainer = Color(0xFF3A2200),
    card = Color.White,
    cardBorder = LightOutlineVariant,
    glow = LightPrimary.copy(alpha = 0.14f),
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtended }