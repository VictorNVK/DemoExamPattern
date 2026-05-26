package ru.demoexam.template.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val lightScheme = lightColorScheme(
    primary = Color(0xFF234B4B),
    onPrimary = Color(0xFFF8F6F0),
    secondary = Color(0xFFB07D32),
    onSecondary = Color(0xFFF8F6F0),
    background = Color(0xFFF4EFE4),
    surface = Color(0xFFFFFBF4),
    surfaceVariant = Color(0xFFE9E1D4),
    primaryContainer = Color(0xFFDCE9E9),
    error = Color(0xFFB3261E),
)

private val darkScheme = darkColorScheme(
    primary = Color(0xFF8FC7C7),
    secondary = Color(0xFFE8BE74),
    background = Color(0xFF161A18),
    surface = Color(0xFF222826),
    surfaceVariant = Color(0xFF303734),
    onSurface = Color(0xFFF8F6F0),
    error = Color(0xFFFFB4AB),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) {
        darkScheme
    } else {
        lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography.copy(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 30.sp,
                lineHeight = 36.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                lineHeight = 30.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
        ),
        content = content,
    )
}

