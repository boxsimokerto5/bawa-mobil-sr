package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val VintageLightColorScheme = lightColorScheme(
    primary = EspressoBrown,
    onPrimary = VintageCreamBg,
    primaryContainer = SoftGoldHighlight,
    onPrimaryContainer = DeepInkBrown,
    secondary = AntiqueGold,
    onSecondary = DeepInkBrown,
    secondaryContainer = VintageParchmentSurface,
    onSecondaryContainer = EspressoBrown,
    tertiary = RichLeatherBrown,
    onTertiary = Color.White,
    tertiaryContainer = WarmSepiaBrown,
    onTertiaryContainer = VintageCreamBg,
    background = VintageCreamBg,
    onBackground = DeepInkBrown,
    surface = VintageCardCream,
    onSurface = DeepInkBrown,
    surfaceVariant = VintageParchmentSurface,
    onSurfaceVariant = SoftMochaText,
    outline = VintageWarmBorder,
    outlineVariant = MetallicGold,
    error = VintageCrimsonReject,
    onError = Color.White,
    errorContainer = VintageCrimsonBg,
    onErrorContainer = VintageCrimsonReject
)

val VintageShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Always enforce the Light Vintage Brown & Gold theme requested by user
    MaterialTheme(
        colorScheme = VintageLightColorScheme,
        typography = Typography,
        shapes = VintageShapes,
        content = content
    )
}
