package com.example.fitgymkt.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ColoresFit.Blanco,
    secondary = ColoresFit.TextoOscuroSecundario,
    tertiary = ColoresFit.Naranja,
    background = ColoresFit.FondoOscuro,
    surface = ColoresFit.FondoOscuroSecundario,
    surfaceVariant = Color(0xFF2A3140),
    onPrimary = ColoresFit.Negro,
    onSecondary = ColoresFit.Negro,
    onTertiary = ColoresFit.Negro,
    onBackground = ColoresFit.Blanco,
    onSurface = ColoresFit.Blanco,
    onSurfaceVariant = ColoresFit.Blanco,
    outline = Color(0xFF3A4355),
    outlineVariant = Color(0xFF2D3644),
    error = ColoresFit.Rojo
)

private val LightColorScheme = lightColorScheme(
    primary = ColoresFit.Negro,
    secondary = ColoresFit.AzulFit,
    tertiary = ColoresFit.Naranja,
    background = ColoresFit.FondoApp,
    surface = ColoresFit.FondoTarjeta,
    surfaceVariant = ColoresFit.FondoSuave,
    onPrimary = ColoresFit.Blanco,
    onSecondary = ColoresFit.Blanco,
    onTertiary = ColoresFit.Blanco,
    onBackground = ColoresFit.Negro,
    onSurface = ColoresFit.Negro,
    onSurfaceVariant = ColoresFit.NegroSuave,
    outline = ColoresFit.Borde,
    outlineVariant = Color(0xFFF0F2F5),
    error = ColoresFit.Rojo
)

@Composable
fun FitgymktTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = true,
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
