package com.pickett82.barcodewidget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF205D45),
    onPrimary = Color.White,
    secondary = Color(0xFF4C6357),
    background = Color(0xFFF7FAF7),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF87D9AF),
    onPrimary = Color(0xFF003822),
    secondary = Color(0xFFB5CCBE),
    background = Color(0xFF101512),
    surface = Color(0xFF171D19),
)

@Composable
fun BarcodeWidgetTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
