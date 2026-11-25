package com.example.videojuego.ui.storm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import videojuego.shared.generated.resources.Res
import videojuego.shared.generated.resources.avion_tormenta
import kotlin.math.abs
import kotlin.math.sin

/** Duración total de la escena y líneas de narración con el segundo en que aparece cada una. */
object StormTimeline {
    const val TOTAL_MS = 16_000L
    const val FALL_START_MS = 10_500L
    const val FLASH_WHITE_MS = 14_200L
    private const val CHARS_PER_SECOND = 34f

    val lines: List<Pair<Long, String>> = listOf(
        0L to "El vuelo iba tranquilo... hasta que el cielo se puso negro.",
        3_500L to "Las luces parpadearon. Nadie dijo nada, pero todos lo sentimos.",
        8_000L to "Un trueno sacudió el avión. Alguien gritó. Yo me agarré fuerte.",
        12_500L to "Y después, solo hubo agua... y silencio.",
    )

    /** Línea visible en un momento dado y cuántas letras ya se escribieron (nulo antes de la primera). */
    fun lineAt(ms: Long): Pair<String, Int>? {
        val i = lines.indexOfLast { ms >= it.first }
        if (i < 0) return null
        val (start, text) = lines[i]
        val chars = ((ms - start) / 1000f * CHARS_PER_SECOND).toInt().coerceIn(0, text.length)
        return text to chars
    }

    /** Intensidad del relámpago (0..1) en un momento: destellos fijos, sin azar. */
    fun lightning(ms: Long): Float {
        val flashes = longArrayOf(1_800L, 4_600L, 7_900L, 9_300L, 12_000L)
        var best = 0f
        for (f in flashes) {
            val d = ms - f
            if (d in 0..420) best = maxOf(best, 1f - d / 420f)
        }
        return best
    }
}

/**
 * Apertura del juego: la tormenta en el avión. Dura ~16 s, se puede saltar con SALTAR (o Esc / Enter).
 */
@Composable
fun StormScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val done by rememberUpdatedState(onFinished)
    var t by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        val t0 = withFrameMillis { it }
        while (true) {
            t = withFrameMillis { it } - t0
            if (t >= StormTimeline.TOTAL_MS) {
                done()
                break
            }
        }
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    val plane = painterResource(Res.drawable.avion_tormenta)

    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focus)
            .focusable()
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown && (it.key == Key.Escape || it.key == Key.Enter)) {
                    done(); true
                } else false
            },
    ) {
        Canvas(Modifier.fillMaxSize()) { drawStorm(t, plane) }

        Text(
            "SALTAR ▸",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(16.dp)
                .border(2.dp, Color(0xFFDDE6F5), RectangleShape)
                .background(Color(0x99000000), RectangleShape)
                .clickable { done() }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color(0xFFDDE6F5), fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp,
        )

        StormTimeline.lineAt(t)?.let { (text, chars) ->
            Text(
                text.take(chars),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .fillMaxWidth(0.94f)
                    .padding(bottom = 16.dp)
                    .border(3.dp, IslaColors.Ink, RectangleShape)
                    .background(IslaColors.Paper, RectangleShape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 13.sp, lineHeight = 19.sp,
            )
        }
    }
}

private fun DrawScope.drawStorm(t: Long, plane: Painter) {
    val w = size.width
    val h = size.height
    val tf = t.toFloat()
    val fall = ((tf - StormTimeline.FALL_START_MS) / 3_500f).coerceIn(0f, 1f)
    val dark = (0.25f + t / StormTimeline.TOTAL_MS.toFloat() * 0.35f).coerceAtMost(0.6f)

    // Cielo que se va oscureciendo.
    drawRect(
        Brush.verticalGradient(
            listOf(Color(0xFF1B2A44).copy(alpha = 1f - dark * 0.4f), Color(0xFF0B1220), Color(0xFF05080F)),
        ),
        size = size,
    )

    // Nubes que pasan rápido.
    for (i in 0 until 7) {
        val speed = 0.05f + i * 0.012f
        val x = ((i * 0.23f - tf / 1000f * speed) % 1.3f + 1.3f) % 1.3f * w - w * 0.15f
        val y = h * (0.08f + (i % 4) * 0.11f)
        drawOval(Color(0xFF2A3550).copy(alpha = 0.7f), Offset(x, y), Size(w * 0.38f, h * 0.10f))
    }

    // Avión: se sacude y, al final, cae inclinándose.
    val shake = if (fall > 0f) 14f else 5f
    val px = w * (0.5f + 0.02f * sin(tf / 90f)) + sin(tf / 37f) * shake
    val py = h * 0.38f + sin(tf / 61f) * shake + fall * fall * h * 0.75f
    val tilt = sin(tf / 700f) * 4f + fall * 38f
    // El sprite mira a la derecha; al caer baja la nariz (giro horario) y deja una estela de humo.
    val pw = w * 0.46f
    val ph = pw * 359f / 638f
    if (fall > 0f) {
        for (i in 0 until 8) {
            val k = i / 8f
            val sx = px - pw * 0.4f - k * pw * 0.6f
            val sy = py - ph * 0.1f - k * h * 0.12f * fall
            drawCircle(Color(0xFF2B2B33).copy(alpha = 0.55f * (1f - k)), ph * (0.10f + 0.12f * k), Offset(sx, sy))
        }
    }
    rotate(tilt, pivot = Offset(px, py)) {
        translate(px - pw / 2f, py - ph / 2f) {
            with(plane) { draw(size = Size(pw, ph)) }
        }
    }

    // Lluvia en diagonal.
    val streaks = 90
    for (i in 0 until streaks) {
        val sp = 0.9f + (i % 5) * 0.18f
        val fx = ((i * 0.6180339f) % 1f)
        val fy = (((tf / 1000f) * sp + i * 0.137f) % 1f)
        val x = fx * (w + 120f) - 60f - fy * 40f
        val y = fy * h
        drawLine(Color(0xFFBFD4F2).copy(alpha = 0.45f), Offset(x, y), Offset(x - 9f, y + 26f), strokeWidth = 2f)
    }

    // Mar abajo cuando el avión cae.
    if (fall > 0f) {
        val seaY = h * (1f - 0.22f * fall)
        drawRect(Color(0xFF0E2A4A).copy(alpha = fall), Offset(0f, seaY), Size(w, h - seaY))
    }

    // Relámpagos y destello blanco final.
    val l = StormTimeline.lightning(t)
    if (l > 0f) drawRect(Color.White.copy(alpha = l * 0.75f), size = size)
    if (t >= StormTimeline.FLASH_WHITE_MS) {
        val k = ((tf - StormTimeline.FLASH_WHITE_MS) / 700f).coerceIn(0f, 1f)
        val back = ((tf - StormTimeline.FLASH_WHITE_MS - 700f) / 1000f).coerceIn(0f, 1f)
        drawRect(Color.White.copy(alpha = k * (1f - back)), size = size)
        drawRect(Color.Black.copy(alpha = back), size = size)
    }
}
