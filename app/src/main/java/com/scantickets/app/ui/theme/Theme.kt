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

/** Une couleur d'accent choisissable dans les réglages. */
data class AccentApp(
    val nom: String,
    val libelle: String,
    val clair: Color,
    val sombre: Color
)

val accentsDisponibles = listOf(
    AccentApp("vert", "Vert", Color(0xFF1B5E20), Color(0xFF81C784)),
    AccentApp("bleu", "Bleu", Color(0xFF0D47A1), Color(0xFF90CAF9)),
    AccentApp("violet", "Violet", Color(0xFF5E35B1), Color(0xFFB39DDB)),
    AccentApp("solaire", "Solaire", Color(0xFF9A5B00), Color(0xFFFFC97A))
)

@Composable
fun ScanTicketsTheme(
    themeMode: String = "systeme",
    accent: String = "vert",
    dynamique: Boolean = false,
    content: @Composable () -> Unit
) {
    val sombre = when (themeMode) {
        "clair" -> false
        "sombre" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (dynamique && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (sombre) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        val choix = accentsDisponibles.firstOrNull { it.nom == accent }
            ?: accentsDisponibles.first()
        if (sombre) {
            darkColorScheme(primary = choix.sombre, secondary = choix.sombre)
        } else {
            lightColorScheme(primary = choix.clair, secondary = choix.clair)
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
