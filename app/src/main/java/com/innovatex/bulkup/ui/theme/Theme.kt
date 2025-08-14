package com.innovatex.bulkup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BulkPrimary,
    onPrimary = BulkOnPrimary,
    background = BulkBackground,
    surface = BulkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = BulkPrimary,
    onPrimary = BulkOnPrimary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun BulkupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}