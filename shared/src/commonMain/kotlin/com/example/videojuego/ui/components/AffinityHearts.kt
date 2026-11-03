package com.example.videojuego.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.videojuego.ui.theme.IslaColors

/** Corazón de 7 x 6 "píxeles". */
private val HEART_PIXELS = listOf(
    "0110110",
    "1111111",
    "1111111",
    "0111110",
    "0011100",
    "0001000",
)
private const val HEART_COLUMNS = 7

/**
 * Cuánto se llena cada corazón (0 a 1) para una afinidad dada como fracción (0 a 1).
 * Con 5 corazones, cada uno vale 20 puntos: 30 de 100 llena 1 corazón y medio.
 */
fun heartFills(fraction: Float, hearts: Int = 5): List<Float> {
    require(hearts > 0) { "Debe haber al menos un corazón" }
    val value = fraction.coerceIn(0f, 1f) * hearts
    return List(hearts) { i -> (value - i).coerceIn(0f, 1f) }
}

/** Un corazón pixelado, relleno de izquierda a derecha según [fill] (0 a 1). */
@Composable
fun PixelHeart(
    fill: Float,
    modifier: Modifier = Modifier,
    width: Dp = 24.dp,
    filledColor: Color = IslaColors.Action,
    emptyColor: Color = Color(0xFF7C7396),
) {
    Canvas(modifier.size(width = width, height = width * HEART_PIXELS.size / HEART_COLUMNS)) {
        val cell = size.width / HEART_COLUMNS

        fun drawHeart(color: Color) {
            HEART_PIXELS.forEachIndexed { row, pixels ->
                pixels.forEachIndexed { col, ch ->
                    if (ch == '1') {
                        // +0.5f evita líneas finas entre píxeles al redondear.
                        drawRect(
                            color = color,
                            topLeft = Offset(col * cell, row * cell),
                            size = Size(cell + 0.5f, cell + 0.5f),
                        )
                    }
                }
            }
        }

        drawHeart(emptyColor)
        if (fill > 0f) {
            clipRect(left = 0f, top = 0f, right = size.width * fill.coerceAtMost(1f), bottom = size.height) {
                drawHeart(filledColor)
            }
        }
    }
}

/** Barra de corazones de afinidad. Se anima suavemente cuando el valor cambia. */
@Composable
fun AffinityHearts(
    fraction: Float,
    modifier: Modifier = Modifier,
    hearts: Int = 5,
    heartWidth: Dp = 24.dp,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "affinityHearts",
    )
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        heartFills(animated, hearts).forEach { fill ->
            PixelHeart(fill = fill, width = heartWidth)
        }
    }
}
