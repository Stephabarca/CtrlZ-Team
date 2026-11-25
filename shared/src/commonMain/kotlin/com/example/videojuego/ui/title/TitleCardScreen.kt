package com.example.videojuego.ui.title

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.theme.PixelFontFamily
import kotlinx.coroutines.delay

/** Datos de una tarjeta de título: "DÍA 2" + "Con Lucas", o "NOCHE 2" + "El sendero de Lucas". */
data class TitleCard(val kicker: String, val title: String, val subtitle: String, val night: Boolean)

object TitleCards {
    fun day(day: Int, withName: String?) =
        TitleCard("ISLA PERDIDA", "DÍA $day", withName?.let { "Con $it" } ?: "Un nuevo amanecer", night = false)

    /** Convierte "Noche 2: El sendero de Lucas" en "NOCHE 2" + "El sendero de Lucas". */
    fun night(order: Int, rawTitle: String): TitleCard {
        val subtitle = rawTitle.substringAfter(":", rawTitle).trim()
        return TitleCard("ISLA PERDIDA", "NOCHE $order", subtitle, night = true)
    }
}

/** Pantalla corta (~2,8 s) con el título de la jornada. Se salta tocando. */
@Composable
fun TitleCardScreen(card: TitleCard, onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val done = rememberUpdatedState(onFinished)
    val a = remember { Animatable(0f) }
    LaunchedEffect(card) {
        a.animateTo(1f, tween(600))
        delay(1600)
        a.animateTo(0f, tween(450))
        done.value()
    }
    val top = if (card.night) Color(0xFF0B1230) else Color(0xFF3F7FB5)
    val bottom = if (card.night) Color(0xFF1C2A55) else Color(0xFFF2C56E)
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { done.value() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val alpha = a.value
            Text(card.kicker, color = Color.White.copy(alpha = alpha * 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = PixelFontFamily, letterSpacing = 4.sp)
            Text(card.title, color = Color.White.copy(alpha = alpha), fontSize = 42.sp, fontWeight = FontWeight.Black, fontFamily = PixelFontFamily, textAlign = TextAlign.Center)
            Text(card.subtitle, color = Color(0xFFFFE9B8).copy(alpha = alpha), fontSize = 18.sp, fontFamily = PixelFontFamily, textAlign = TextAlign.Center)
        }
    }
}
