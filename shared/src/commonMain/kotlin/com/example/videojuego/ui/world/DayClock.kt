package com.example.videojuego.ui.world

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.world.DayClockMath
import com.example.videojuego.world.DayZone
import com.example.videojuego.world.formatClock
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Poner en true para mostrar también los minutos que quedan (útil para probar). */
private const val SHOW_NUMBERS = false

private val WOOD_DARK = Color(0xFF5A3418)
private val WOOD_LIGHT = Color(0xFF8B5A2B)
private val FACE = Color(0xFFF3E3B3)
private val MORNING_COLOR = Color(0xFFFFD84D)
private val AFTERNOON_COLOR = Color(0xFFF28C28)
private val NIGHT_COLOR = Color(0xFF2E3B63)
private val GOLD = Color(0xFFFFC93C)

private const val MORNING_LABEL = "MAÑANA"
private const val AFTERNOON_LABEL = "TARDE"
private const val NIGHT_LABEL = "NOCHE"

/** El dial empieza abajo a la izquierda y da 270° hasta abajo a la derecha (ángulos de Compose: 0° = derecha). */
private const val DIAL_START = 135f
private const val DIAL_SWEEP = 270f

/**
 * Reloj del día al estilo Stardew Valley: un dial redondo con marco de madera, anillo de mañana/tarde/noche,
 * sol y luna, y una manecilla que gira durante el día. Abajo, una placa con el momento actual.
 */
@Composable
fun DayClock(night: Boolean, remainingMs: Long, modifier: Modifier = Modifier) {
    val progress = DayClockMath.progress(night, remainingMs)
    val zone = DayClockMath.zone(progress)
    val label = when (zone) {
        DayZone.MORNING -> MORNING_LABEL
        DayZone.AFTERNOON -> AFTERNOON_LABEL
        DayZone.NIGHT -> NIGHT_LABEL
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(96.dp)) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val outer = size.minDimension / 2f
            val frame = outer * 0.14f
            val ringW = outer * 0.20f
            val faceR = outer - frame

            // Marco de madera (dos tonos) y fondo del dial.
            drawCircle(WOOD_DARK, outer, c)
            drawCircle(WOOD_LIGHT, outer - frame * 0.35f, c)
            drawCircle(WOOD_DARK, faceR + 2f, c)
            drawCircle(FACE, faceR, c)

            // Anillo de colores: mañana, tarde y noche.
            val ringR = faceR - ringW / 2f - 3f
            val arcTopLeft = Offset(c.x - ringR, c.y - ringR)
            val arcSize = Size(ringR * 2f, ringR * 2f)
            var start = DIAL_START
            listOf(
                DayClockMath.MORNING_END to MORNING_COLOR,
                (DayClockMath.AFTERNOON_END - DayClockMath.MORNING_END) to AFTERNOON_COLOR,
                (1f - DayClockMath.AFTERNOON_END) to NIGHT_COLOR,
            ).forEach { (share, color) ->
                val sweep = DIAL_SWEEP * share
                drawArc(color, start, sweep, false, arcTopLeft, arcSize, style = Stroke(ringW))
                start += sweep
            }
            // Rayitas que separan las partes.
            listOf(0f, DayClockMath.MORNING_END, DayClockMath.AFTERNOON_END, 1f).forEach { p ->
                val a = ((DIAL_START + DIAL_SWEEP * p) * PI / 180.0).toFloat()
                drawLine(
                    WOOD_DARK,
                    Offset(c.x + cos(a) * (ringR - ringW / 2f), c.y + sin(a) * (ringR - ringW / 2f)),
                    Offset(c.x + cos(a) * (ringR + ringW / 2f), c.y + sin(a) * (ringR + ringW / 2f)),
                    strokeWidth = 2f,
                )
            }

            // Sol de mañana, sol bajo de tarde y luna de noche, dentro del dial.
            fun at(p: Float, r: Float): Offset {
                val a = ((DIAL_START + DIAL_SWEEP * p) * PI / 180.0).toFloat()
                return Offset(c.x + cos(a) * r, c.y + sin(a) * r)
            }
            val iconR = ringR - ringW * 0.95f
            drawSun(at(DayClockMath.MORNING_END / 2f, iconR), outer * 0.13f, MORNING_COLOR)
            drawSun(
                at((DayClockMath.MORNING_END + DayClockMath.AFTERNOON_END) / 2f, iconR),
                outer * 0.11f,
                AFTERNOON_COLOR,
            )
            drawMoon(at((DayClockMath.AFTERNOON_END + 1f) / 2f, iconR), outer * 0.12f)

            // Manecilla.
            val angle = ((DIAL_START + DIAL_SWEEP * progress) * PI / 180.0).toFloat()
            val dx = cos(angle)
            val dy = sin(angle)
            val len = ringR + ringW * 0.35f
            val tip = Offset(c.x + dx * len, c.y + dy * len)
            drawLine(WOOD_DARK, c, tip, strokeWidth = outer * 0.085f)
            // Punta en rombo dorado.
            val d = outer * 0.13f
            val back = Offset(tip.x - dx * d * 1.6f, tip.y - dy * d * 1.6f)
            val diamond = Path().apply {
                moveTo(tip.x + dx * d * 0.4f, tip.y + dy * d * 0.4f)
                lineTo(back.x - dy * d, back.y + dx * d)
                lineTo(tip.x - dx * d * 2.2f, tip.y - dy * d * 2.2f)
                lineTo(back.x + dy * d, back.y - dx * d)
                close()
            }
            drawPath(diamond, GOLD)
            drawPath(diamond, WOOD_DARK, style = Stroke(2f))
            // Centro.
            drawCircle(WOOD_DARK, outer * 0.12f, c)
            drawCircle(GOLD, outer * 0.06f, c)
        }

        Text(
            label,
            modifier = Modifier
                .padding(top = 2.dp)
                .background(WOOD_LIGHT)
                .border(2.dp, WOOD_DARK)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            color = Color(0xFFFFF4D6),
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
        )
        if (SHOW_NUMBERS) {
            Text(formatClock(remainingMs), color = WOOD_DARK, fontFamily = PixelFontFamily, fontSize = 9.sp)
        }
    }
}

private fun DrawScope.drawSun(center: Offset, r: Float, color: Color) {
    for (i in 0 until 8) {
        val a = (i * PI / 4.0).toFloat()
        drawLine(
            color,
            Offset(center.x + cos(a) * r * 1.25f, center.y + sin(a) * r * 1.25f),
            Offset(center.x + cos(a) * r * 1.8f, center.y + sin(a) * r * 1.8f),
            strokeWidth = r * 0.35f,
        )
    }
    drawCircle(color, r, center)
    drawCircle(WOOD_DARK, r, center, style = Stroke(1.5f))
}

private fun DrawScope.drawMoon(center: Offset, r: Float) {
    drawCircle(NIGHT_COLOR, r, center)
    // Media luna: se "muerde" con un círculo del color del fondo del dial.
    drawCircle(FACE, r * 0.85f, Offset(center.x + r * 0.5f, center.y - r * 0.2f))
}
