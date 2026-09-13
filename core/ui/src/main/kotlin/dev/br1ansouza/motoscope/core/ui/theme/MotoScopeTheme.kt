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
    primary = Color(0xFFD4B574),
    onPrimary = Color(0xFF211B0C),
    primaryContainer = Color(0xFF722B35),
    onPrimaryContainer = Color(0xFFFFDADD),
    secondary = Color(0xFFE5A4AC),
    onSecondary = Color(0xFF421920),
    secondaryContainer = Color(0xFF5C252E),
    onSecondaryContainer = Color(0xFFFFDADD),
    tertiary = Color(0xFFB8CBA6),
    onTertiary = Color(0xFF24341B),
    background = Color(0xFF101113),
    onBackground = Color(0xFFF3F0EB),
    surface = Color(0xFF101113),
    onSurface = Color(0xFFF3F0EB),
    surfaceVariant = Color(0xFF25262B),
    onSurfaceVariant = Color(0xFFC7C5C0),
    outline = Color(0xFF96948F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val MotoScopeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,
        lineHeight = 100.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
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
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
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
