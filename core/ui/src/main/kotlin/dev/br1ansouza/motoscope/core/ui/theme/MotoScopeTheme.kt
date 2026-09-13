package dev.br1ansouza.motoscope.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MotoScopeColors = darkColorScheme(
    primary = MotoScopePalette.darkRed,
    onPrimary = MotoScopePalette.ink,
    primaryContainer = MotoScopePalette.darkRedRaised,
    onPrimaryContainer = MotoScopePalette.ink,
    secondary = MotoScopePalette.gold,
    onSecondary = MotoScopePalette.black,
    secondaryContainer = MotoScopePalette.graphiteRaised,
    onSecondaryContainer = MotoScopePalette.gold,
    tertiary = MotoScopePalette.gold,
    onTertiary = MotoScopePalette.black,
    background = MotoScopePalette.black,
    onBackground = MotoScopePalette.ink,
    surface = MotoScopePalette.graphite,
    onSurface = MotoScopePalette.ink,
    surfaceVariant = MotoScopePalette.graphiteRaised,
    onSurfaceVariant = Color(0xFFA9ACB2),
    outline = MotoScopePalette.edge,
    error = MotoScopeStatusColors.failure,
    onError = MotoScopePalette.ink
)

private val MotoScopeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,
        lineHeight = 100.sp,
        fontFeatureSettings = "tnum",
        letterSpacing = (-2).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontFeatureSettings = "tnum"
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

private val MotoScopeShapes = Shapes(
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp)
)

@Composable
fun MotoScopeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MotoScopeColors,
        typography = MotoScopeTypography,
        shapes = MotoScopeShapes,
        content = content
    )
}
