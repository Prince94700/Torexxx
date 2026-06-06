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
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF141218),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color(0xFF381E72),
    onSecondary = Color(0xFF332D41),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFF2B8B5),
    outline = Color(0xFF938F99)
)

private val LightColorScheme = lightColorScheme(
    primary = TorexSaffronPrimary,
    secondary = TorexAshokaBlue,
    tertiary = TorexDharmaGreen,
    background = TorexCanvasBg,
    surface = TorexSurfaceGlass,
    onPrimary = Color.White,
    onSecondary = TorexTextPrimary,
    onBackground = TorexTextPrimary,
    onSurface = TorexTextPrimary,
    error = TorexAlertRed,
    outline = TorexBorderColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Set false by default to align with Geometric Balance Light theme
    dynamicColor: Boolean = false, // Use our customized TOREX core branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
