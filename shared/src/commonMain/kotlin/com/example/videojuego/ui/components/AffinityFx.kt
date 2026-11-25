package com.example.videojuego.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import kotlinx.coroutines.launch

/**
 * Corazones de afinidad con efecto al responder: si la respuesta suma, laten y sube un "+N"; si resta, tiemblan,
 * se enrojecen y se rompe un corazón que cae.
 *
 * @param eventKey cambia cada vez que el jugador responde (por ejemplo, la cantidad de respuestas guardadas).
 * @param delta cuánta afinidad dio la última respuesta, o null si todavía no hay una.
 */
@Composable
fun AffinityHeartsFx(
    fraction: Float,
    eventKey: Int,
    delta: Int?,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }
    val shake = remember { Animatable(0f) }
    val flash = remember { Animatable(0f) }
    val float = remember { Animatable(1f) } // 0 = recién salió, 1 = terminó
    val fall = remember { Animatable(1f) }

    LaunchedEffect(eventKey) {
        val d = delta ?: return@LaunchedEffect
        float.snapTo(0f)
        launch { float.animateTo(1f, tween(1100)) }
        if (d > 0) {
            scale.animateTo(1.28f, tween(110))
            scale.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
        } else if (d < 0) {
            flash.snapTo(1f)
            fall.snapTo(0f)
            launch { flash.animateTo(0f, tween(700)) }
            launch { fall.animateTo(1f, tween(900)) }
            repeat(5) { shake.animateTo(if (it % 2 == 0) 9f else -9f, tween(45)) }
            shake.animateTo(0f, tween(45))
        }
    }

    Box(modifier) {
        Box(
            Modifier
                .background(IslaColors.Action.copy(alpha = 0.35f * flash.value), RectangleShape)
                .padding(2.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = shake.value
                },
        ) {
            AffinityHearts(fraction = fraction)
        }

        // "+N" o "-N" que sube y se desvanece.
        if (delta != null && delta != 0 && float.value < 1f) {
            Text(
                text = if (delta > 0) "+$delta" else "$delta",
                color = if (delta > 0) Color(0xFFFF9EC0) else Color(0xFFFF8A80),
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        translationY = -26f * float.value * density
                        alpha = (1f - float.value).coerceIn(0f, 1f)
                    },
            )
        }

        // Corazón roto que cae cuando la respuesta resta.
        if (delta != null && delta < 0 && fall.value < 1f) {
            Canvas(
                Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationY = 48f * fall.value * density
                        translationX = -6f * fall.value * density
                        rotationZ = -25f * fall.value
                        alpha = 1f - fall.value
                    },
            ) {
                val s = size.width
                val cell = s / 7f
                val rows = listOf("0110110", "1111111", "1111111", "0111110", "0011100", "0001000")
                rows.forEachIndexed { r, line ->
                    line.forEachIndexed { c, ch ->
                        if (ch == '1') drawRect(Color(0xFFC9423F), Offset(c * cell, r * cell), Size(cell + 0.5f, cell + 0.5f))
                    }
                }
                // Grieta en zigzag.
                val crack = listOf(3f to 0f, 4f to 1.5f, 3f to 2.5f, 4f to 3.5f, 3.5f to 5f)
                crack.zipWithNext().forEach { (a, b) ->
                    drawLine(Color(0xFFF5F0E6), Offset(a.first * cell, a.second * cell), Offset(b.first * cell, b.second * cell), strokeWidth = cell * 0.5f)
                }
            }
        }
    }
}
