package com.example.videojuego.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.videojuego.narrative.Speaker

/** Paleta definida en el diseño de Isla Perdida. */
object IslaColors {
    val Primary = Color(0xFF28545A)   // UI principal
    val Action = Color(0xFFF36B6B)    // acción y selección
    val Valentina = Color(0xFFCAD988)
    val Lucas = Color(0xFFFEB204)
    val Lila = Color(0xFFAF50E5)

    // Apoyo para legibilidad (no están en la paleta oficial)
    val Ink = Color(0xFF1F2A2C)
    val Paper = Color(0xFFFFF6E3)
    val OnDark = Color(0xFFFFFFFF)
}

/** Fuente pixelada pendiente de definir. Cambiarla solo aquí cuando se decida. */
val PixelFontFamily: FontFamily = FontFamily.Monospace

fun Speaker.accent(): Color = when (this) {
    Speaker.NARRATOR -> IslaColors.Primary
    Speaker.VALENTINA -> IslaColors.Valentina
    Speaker.LUCAS -> IslaColors.Lucas
    Speaker.LILA -> IslaColors.Lila
}

/** Texto oscuro sobre verde y naranja (contraste bajo con blanco); blanco sobre teal y morado. */
fun Speaker.onAccent(): Color = when (this) {
    Speaker.NARRATOR, Speaker.LILA -> IslaColors.OnDark
    Speaker.VALENTINA, Speaker.LUCAS -> IslaColors.Ink
}

@Composable
fun IslaPerdidaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = IslaColors.Primary,
            onPrimary = IslaColors.OnDark,
            secondary = IslaColors.Action,
            onSecondary = IslaColors.OnDark,
            background = IslaColors.Paper,
            onBackground = IslaColors.Ink,
            surface = IslaColors.Paper,
            onSurface = IslaColors.Ink,
        ),
        content = content,
    )
}
