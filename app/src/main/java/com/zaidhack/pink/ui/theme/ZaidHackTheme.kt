package com.zaidhack.pink.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Monet-inspired fixed rose palette: visually consistent even when the wallpaper changes.
private val PinkMonet = darkColorScheme(
    primary = Color(0xFFF4ACD4),
    onPrimary = Color(0xFF4D1C3A),
    primaryContainer = Color(0xFF60324E),
    onPrimaryContainer = Color(0xFFFFD9EC),
    secondary = Color(0xFFE1BDCF),
    onSecondary = Color(0xFF422B38),
    secondaryContainer = Color(0xFF59414F),
    onSecondaryContainer = Color(0xFFFFD9EA),
    tertiary = Color(0xFFFFB0C4),
    onTertiary = Color(0xFF581D31),
    tertiaryContainer = Color(0xFF74394E),
    onTertiaryContainer = Color(0xFFFFD9E1),
    background = Color(0xFF180F1B),
    onBackground = Color(0xFFFFE5F0),
    surface = Color(0xFF211522),
    onSurface = Color(0xFFFFE5F0),
    surfaceVariant = Color(0xFF4B3546),
    onSurfaceVariant = Color(0xFFD8C0D1),
    outline = Color(0xFFA88A9D),
    outlineVariant = Color(0xFF634B5D),
    surfaceContainer = Color(0xFF291A2B)
)
private val PinkShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
@Composable
fun ZaidHackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PinkMonet, shapes = PinkShapes, content = content)
}
