package com.example.videojuego.ui.credits

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.credits.CreditsData
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import kotlinx.coroutines.delay

/**
 * Créditos del juego. El contenido se edita en CreditsData.
 *
 * Se desplazan solos despacio; el jugador puede mover el texto con el dedo o la rueda cuando quiera.
 * @param endingTitle si viene de terminar el juego, el final obtenido (por ejemplo "FINAL DE LUCAS"): se muestra arriba.
 */
@Composable
fun CreditsScreen(onBack: () -> Unit, modifier: Modifier = Modifier, endingTitle: String? = null) {
    val scroll = rememberScrollState()
    // Arranca después de una pausa y recorre todo el texto. Si el jugador lo mueve, se queda donde lo dejó.
    LaunchedEffect(Unit) {
        delay(1800)
        if (scroll.maxValue > 0 && scroll.value == 0) {
            scroll.animateScrollTo(scroll.maxValue, tween(durationMillis = 26_000, easing = LinearEasing))
        }
    }

    Box(modifier.fillMaxSize().background(IslaColors.Primary).safeDrawingPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (endingTitle != null) {
                Text(
                    endingTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, IslaColors.Ink, RectangleShape)
                        .background(IslaColors.Paper, RectangleShape)
                        .padding(14.dp),
                    color = IslaColors.Action,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Text("CRÉDITOS", color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(CreditsData.GAME, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontSize = 14.sp)
            CreditsData.sections.forEach { section ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(3.dp, IslaColors.Ink, RectangleShape)
                        .background(IslaColors.Paper, RectangleShape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(section.title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    section.lines.forEach {
                        Text(it, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
            Text(
                "¡Gracias por jugar!",
                modifier = Modifier.fillMaxWidth(),
                color = IslaColors.OnDark,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            ActionButton(text = "VOLVER", onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
