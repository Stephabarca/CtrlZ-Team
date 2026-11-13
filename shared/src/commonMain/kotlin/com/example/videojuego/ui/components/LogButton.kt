package com.example.videojuego.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

private val BARK = Color(0xFF8A5A33)
private val BARK_LIGHT = Color(0xFFA06C3F)
private val BARK_DARK = Color(0xFF65401F)
private val OUTLINE = Color(0xFF2B1A0E)
private val CUT_FACE = Color(0xFFD9A56B)
private val RING = Color(0xFFB07A44)
// Texto crema con sombra oscura: se lee bien sobre la madera.
private val TEXT_SHADOW = Color(0xFF2B1A0E)
private val TEXT_FACE = Color(0xFFFFF1D6)
private val ROPE_A = Color(0xFFD8B97A)
private val ROPE_B = Color(0xFFB8955A)
private val ROPE_KNOT = Color(0xFF8E6B3A)

/** Tamaño del texto de los botones. */
private val BUTTON_FONT_SIZE = 18.sp

/**
 * Botón de acción en forma de tronco con el texto en relieve. Se usa en todo el juego.
 *
 * @param container color base: el verde azulado por defecto deja la madera normal; otro color (por ejemplo
 * el rojo de acción) tiñe un poco la madera; si es translúcido el botón se ve apagado (opción no disponible).
 * @param content se conserva por compatibilidad; el texto usa crema con sombra oscura para leerse sobre la madera.
 * @param minWidth ancho mínimo, para que varios botones apilados queden del mismo tamaño.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = IslaColors.Primary,
    content: Color = IslaColors.OnDark,
    minWidth: Dp = 0.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val disabled = container.alpha < 1f
    val bark = if (container == IslaColors.Primary || disabled) BARK else lerp(BARK, container, 0.3f)

    Box(
        modifier = modifier
            .widthIn(min = minWidth)
            .offset(y = if (pressed) 2.dp else 0.dp)
            .alpha(if (disabled) 0.5f else 1f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .drawBehind { drawLog(bark, pressed) }
            .padding(horizontal = 40.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Sombra oscura detrás y texto crema encima.
        Text(
            text = text,
            color = TEXT_SHADOW,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = BUTTON_FONT_SIZE,
            maxLines = 1,
            modifier = Modifier.offset(2.dp, 2.dp),
        )
        Text(
            text = text,
            color = TEXT_FACE,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = BUTTON_FONT_SIZE,
            maxLines = 1,
        )
    }
}

/**
 * Dibuja el tronco visto en perspectiva: el extremo izquierdo es la corteza redondeada (la cara cortada
 * queda oculta detrás) y solo el extremo derecho muestra la cara cortada con los anillos.
 */
private fun DrawScope.drawLog(bark: Color, pressed: Boolean) {
    val w = size.width
    val h = size.height
    val px = 2.dp.toPx()
    val cap = h * 0.55f
    val bodyL = cap / 2f
    val bodyW = w - cap

    // Extremo izquierdo: media elipse de corteza; el cuerpo tapa su mitad derecha.
    drawOval(OUTLINE, Offset(0f, 0f), Size(cap, h))
    drawOval(bark, Offset(px * 1.5f, px * 1.5f), Size(cap - px * 3f, h - px * 3f))
    if (pressed) drawOval(Color(0x33000000), Offset(px * 1.5f, px * 1.5f), Size(cap - px * 3f, h - px * 3f))

    drawRect(bark, Offset(bodyL, 0f), Size(bodyW, h))
    // Vetas: tramos cortados a distintas alturas.
    val bands = listOf(0.2f to BARK_LIGHT, 0.45f to BARK_DARK, 0.72f to BARK_LIGHT, 0.88f to BARK_DARK)
    bands.forEachIndexed { row, (f, color) ->
        var x = bodyL + (row * 3 % 5) * px
        while (x < bodyL + bodyW - px * 3) {
            drawRect(color, Offset(x, h * f), Size(px * 4f, px))
            x += px * 9f
        }
    }
    drawRect(OUTLINE, Offset(bodyL, 0f), Size(bodyW, px * 1.5f))
    drawRect(OUTLINE, Offset(bodyL, h - px * 1.5f), Size(bodyW, px * 1.5f))
    if (pressed) drawRect(Color(0x33000000), Offset(bodyL, 0f), Size(bodyW, h))

    // Extremo derecho: la única cara cortada visible, con sus anillos.
    val x = w - cap
    drawOval(OUTLINE, Offset(x, 0f), Size(cap, h))
    drawOval(CUT_FACE, Offset(x + px * 1.5f, px * 1.5f), Size(cap - px * 3f, h - px * 3f))
    drawOval(RING, Offset(x + cap * 0.22f, h * 0.2f), Size(cap * 0.56f, h * 0.6f), style = Stroke(px * 0.8f))
    drawOval(RING, Offset(x + cap * 0.38f, h * 0.38f), Size(cap * 0.24f, h * 0.24f), style = Stroke(px * 0.8f))
}

/**
 * Apila botones de tronco y los ata unos a otros con dos cuerdas entre cada par.
 * Las cuerdas pasan por detrás del tronco de abajo, así que se ven atadas al de arriba.
 */
@Composable
fun RopedLogs(
    items: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    ropeHeight: Dp = 16.dp,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        items.forEachIndexed { index, item ->
            if (index > 0) Rope(ropeHeight)
            item()
        }
    }
}

@Composable
private fun Rope(height: Dp) {
    Canvas(Modifier.width(150.dp).height(height)) {
        val strand = 4.dp.toPx()
        val seg = 3.dp.toPx()
        for (fx in listOf(0.25f, 0.75f)) {
            val cx = size.width * fx
            var y = -3.dp.toPx()
            var i = 0
            while (y < size.height + 3.dp.toPx()) {
                // Trenzado: el tramo se corre un píxel a cada lado y cambia de tono.
                val shift = if (i % 2 == 0) -1.dp.toPx() else 1.dp.toPx()
                drawRect(if (i % 2 == 0) ROPE_A else ROPE_B, Offset(cx - strand / 2 + shift, y), Size(strand, seg))
                y += seg
                i++
            }
            // Nudo sobre el tronco de arriba.
            drawRect(ROPE_KNOT, Offset(cx - strand, -4.dp.toPx()), Size(strand * 2, 5.dp.toPx()))
        }
    }
}
