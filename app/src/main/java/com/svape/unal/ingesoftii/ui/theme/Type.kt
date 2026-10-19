package com.svape.unal.ingesoftii.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.svape.unal.ingesoftii.R

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun display(
    size: Int,
    line: Int,
    spacing: Float = -0.25f,
    weight: FontWeight = FontWeight.Bold
) =
    TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = spacing.sp
    )

private fun body(
    size: Int,
    line: Int,
    weight: FontWeight = FontWeight.Normal,
    spacing: Float = 0f
) =
    TextStyle(
        fontFamily = Inter,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = spacing.sp
    )

val AppTypography = Typography(
    displayLarge = display(57, 64, -0.5f),
    displayMedium = display(45, 52, -0.5f),
    displaySmall = display(36, 44, -0.25f),
    headlineLarge = display(32, 40, -0.25f),
    headlineMedium = display(28, 36, -0.25f),
    headlineSmall = display(24, 32, 0f, FontWeight.SemiBold),
    titleLarge = display(22, 28, 0f, FontWeight.SemiBold),
    titleMedium = body(16, 24, FontWeight.SemiBold, 0.1f),
    titleSmall = body(14, 20, FontWeight.SemiBold, 0.1f),
    bodyLarge = body(16, 24, spacing = 0.15f),
    bodyMedium = body(14, 20, spacing = 0.2f),
    bodySmall = body(12, 16, spacing = 0.3f),
    labelLarge = body(14, 20, FontWeight.SemiBold, 0.1f),
    labelMedium = body(12, 16, FontWeight.Medium, 0.4f),
    labelSmall = body(11, 16, FontWeight.Medium, 0.5f),
)