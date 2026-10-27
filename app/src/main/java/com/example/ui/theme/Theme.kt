package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MathBlueLight,
    onPrimary = Color(0xFF002266),
    primaryContainer = MathBlueDark,
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = MathAmberLight,
    onSecondary = Color(0xFF452200),
    secondaryContainer = MathAmberSecondary,
    onSecondaryContainer = Color(0xFFFFDDB8),
    tertiary = MathEmeraldLight,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = MathEmeraldTertiary,
    onTertiaryContainer = Color(0xFFD1FADF),
    background = MathDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = MathDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MathDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = MathDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = MathBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = MathAmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = MathEmeraldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = MathLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = MathLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = MathLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = MathLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our branded mathematical palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
