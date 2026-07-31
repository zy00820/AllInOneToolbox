package com.allinone.toolbox.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF6750A4),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFEADDFF),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF21005D),
    secondary = androidx.compose.ui.graphics.Color(0xFF625B71),
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE8DEF8),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF1D192B),
    tertiary = androidx.compose.ui.graphics.Color(0xFF7D5260),
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFD8E4),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF31111D),
    error = androidx.compose.ui.graphics.Color(0xFFB3261E),
    errorContainer = androidx.compose.ui.graphics.Color(0xFFF9DEDC),
    onError = androidx.compose.ui.graphics.Color.White,
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFF410E0B),
    background = androidx.compose.ui.graphics.Color(0xFFFFFBFE),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFBFE),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7E0EC),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF49454F),
    outline = androidx.compose.ui.graphics.Color(0xFF79747E),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFCAC4D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF381E72),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF4F378B),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFEADDFF),
    secondary = androidx.compose.ui.graphics.Color(0xFFCCC2DC),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF332D41),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF4A4458),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFE8DEF8),
    tertiary = androidx.compose.ui.graphics.Color(0xFFEFB8C8),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF492532),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF633B48),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFD8E4),
    error = androidx.compose.ui.graphics.Color(0xFFF2B8B5),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF8C1D18),
    onError = androidx.compose.ui.graphics.Color(0xFF601410),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFF9DEDC),
    background = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    surface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF49454F),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFCAC4D0),
    outline = androidx.compose.ui.graphics.Color(0xFF938F99),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF49454F)
)

@Composable
fun AllInOneToolboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        content = content
    )
}
