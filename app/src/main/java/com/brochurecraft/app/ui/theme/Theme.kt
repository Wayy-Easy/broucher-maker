package com.brochurecraft.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VividCanvasColorScheme = lightColorScheme(
    primary = VCPrimary,
    onPrimary = VCOnPrimary,
    primaryContainer = VCPrimaryContainer,
    onPrimaryContainer = VCOnPrimaryContainer,
    inversePrimary = VCInversePrimary,
    secondary = VCSecondary,
    onSecondary = VCOnSecondary,
    secondaryContainer = VCSecondaryContainer,
    onSecondaryContainer = VCOnSecondaryContainer,
    tertiary = VCTertiary,
    onTertiary = VCOnTertiary,
    tertiaryContainer = VCTertiaryContainer,
    onTertiaryContainer = VCOnTertiaryContainer,
    error = VCError,
    onError = VCOnError,
    errorContainer = VCErrorContainer,
    onErrorContainer = VCOnErrorContainer,
    background = VCBackground,
    onBackground = VCOnBackground,
    surface = VCSurface,
    onSurface = VCOnSurface,
    surfaceVariant = VCSurfaceVariant,
    onSurfaceVariant = VCOnSurfaceVariant,
    outline = VCOutline,
    outlineVariant = VCOutlineVariant,
    inverseSurface = VCInverseSurface,
    inverseOnSurface = VCInverseOnSurface,
    surfaceTint = VCSurfaceTint,
)

// rounded token scale: sm 4, DEFAULT 8, md 12, lg 16, xl 24, full pill
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun BrochureCraftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VividCanvasColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
