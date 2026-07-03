package com.scantickets.app.ui.theme

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

private val ClairScheme = lightColorScheme(
    primary = Color(0xFF1B5E20),
    secondary = Color(0xFF4CAF50)
)

private val SombreScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFA5D6A7)
)

@Composable
fun ScanTicketsTheme(content: @Composable () -> Unit) {
    val sombre = isSystemInDarkTheme()
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (sombre) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (sombre) SombreScheme else ClairScheme
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
