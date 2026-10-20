package com.touille.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Terracotta = Color(0xFFC1663F)
val TerracottaDeep = Color(0xFF9E4E2C)
val TerracottaSoft = Color(0xFFF3DED2)
val Basil = Color(0xFF4A5D3A)
val BasilSoft = Color(0xFFDDE5D2)
val Cream = Color(0xFFFAF5EE)
val Parchment = Color(0xFFFFFCF7)
val Charcoal = Color(0xFF2E2A26)
val Stone = Color(0xFF7C736A)
val Amber = Color(0xFFD89B3C)
val AmberSoft = Color(0xFFF7E7C8)
val Ash = Color(0xFFB9B0A6)

private val LightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaSoft,
    onPrimaryContainer = TerracottaDeep,
    secondary = Basil,
    onSecondary = Color.White,
    secondaryContainer = BasilSoft,
    onSecondaryContainer = Color(0xFF2C3722),
    tertiary = Amber,
    onTertiary = Color(0xFF3A2A08),
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = Color(0xFF4A360C),
    background = Cream,
    onBackground = Charcoal,
    surface = Parchment,
    onSurface = Charcoal,
    surfaceVariant = Color(0xFFF0E8DD),
    onSurfaceVariant = Stone,
    surfaceContainerLowest = Color(0xFFFFFDFA),
    surfaceContainerLow = Color(0xFFF8F2E9),
    surfaceContainer = Color(0xFFF4EDE3),
    surfaceContainerHigh = Color(0xFFEFE6DA),
    surfaceContainerHighest = Color(0xFFE9DFD2),
    surfaceBright = Parchment,
    surfaceDim = Color(0xFFE3D9CC),
    inverseSurface = Color(0xFF3B3631),
    inverseOnSurface = Color(0xFFF6EFE6),
    inversePrimary = Color(0xFFE08D64),
    outline = Color(0xFFDCD2C5),
    outlineVariant = Color(0xFFEAE2D7),
    error = Color(0xFFB3392C),
    onError = Color.White,
    errorContainer = Color(0xFFF7DDD8),
    onErrorContainer = Color(0xFF5D160D),
    scrim = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE08D64),
    onPrimary = Color(0xFF3A1A0C),
    primaryContainer = Color(0xFF7A3B22),
    onPrimaryContainer = TerracottaSoft,
    secondary = Color(0xFFA8BE8C),
    onSecondary = Color(0xFF222B18),
    secondaryContainer = Color(0xFF3A4830),
    onSecondaryContainer = BasilSoft,
    tertiary = Color(0xFFE5BC72),
    onTertiary = Color(0xFF3A2A08),
    tertiaryContainer = Color(0xFF5A4418),
    onTertiaryContainer = Color(0xFFF8E3B4),
    background = Color(0xFF1A1714),
    onBackground = Color(0xFFEDE4D9),
    surface = Color(0xFF221E1A),
    onSurface = Color(0xFFEDE4D9),
    surfaceVariant = Color(0xFF332D27),
    onSurfaceVariant = Color(0xFFBBAFA2),
    surfaceContainerLowest = Color(0xFF141110),
    surfaceContainerLow = Color(0xFF1E1A16),
    surfaceContainer = Color(0xFF262120),
    surfaceContainerHigh = Color(0xFF302A25),
    surfaceContainerHighest = Color(0xFF3A332D),
    surfaceBright = Color(0xFF3E3630),
    surfaceDim = Color(0xFF1A1714),
    inverseSurface = Color(0xFFEDE4D9),
    inverseOnSurface = Color(0xFF332F2B),
    inversePrimary = Color(0xFF8C4A2A),
    outline = Color(0xFF4A4139),
    outlineVariant = Color(0xFF3A332C),
    error = Color(0xFFE58074),
    onError = Color(0xFF4A0F07),
    errorContainer = Color(0xFF7C3125),
    onErrorContainer = Color(0xFFFFDAD3),
    scrim = Color(0xFF000000)
)

private val TouilleShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private val TouilleTypography = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 23.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.6.sp)
)

@Composable
fun TouilleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = TouilleTypography,
        shapes = TouilleShapes,
        content = content
    )
}
