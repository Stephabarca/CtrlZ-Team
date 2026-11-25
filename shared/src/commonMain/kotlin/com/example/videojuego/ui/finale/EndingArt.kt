package com.example.videojuego.ui.finale

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.sin

/** Colores del cielo de la "postal" de cada final: arriba, medio y horizonte. */
private fun skyColors(partnerId: String): List<Color> = when (partnerId) {
    "valentina" -> listOf(Color(0xFF5B3A7A), Color(0xFFFF7E8B), Color(0xFFFFC38B))   // rosa coral
    "lucas" -> listOf(Color(0xFF1F4A5C), Color(0xFFE8845A), Color(0xFFFFD27A))       // verde azulado y ámbar
    "amistad" -> listOf(Color(0xFF35508A), Color(0xFFF0A860), Color(0xFFFFD98A))     // azul y dorado
    else -> listOf(Color(0xFF2A1B5A), Color(0xFFB05FA0), Color(0xFFFF9E80))          // morado
}

private val HEART = Color(0xFFFF5C8A)

/**
 * Ilustración final ("postal") de cada pareja: un atardecer grande con los dos personajes juntos y un detalle propio
 * de cada final (pétalos, chispas de fogata o estrellas). Se acerca muy despacio, como una cámara lenta.
 *
 * @param artMs milisegundos desde que apareció la postal.
 */
fun DrawScope.drawEndingArt(
    partnerId: String,
    nowMs: Long,
    artMs: Long,
    protaPainter: Painter,
    partnerPainters: List<Painter>,
) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    val sky = skyColors(partnerId)
    val horizon = h * 0.58f
    val zoom = 1f + 0.06f * (artMs / 20_000f).coerceIn(0f, 1f)

    scale(scale = zoom, pivot = Offset(w / 2f, h * 0.7f)) {
        // Cielo.
        drawRect(
            Brush.verticalGradient(listOf(sky[0], sky[1], sky[2]), startY = 0f, endY = horizon),
            Offset.Zero, Size(w, horizon),
        )
        // Estrellas arriba.
        for (i in 0 until 22) {
            val sx = ((i * 41) % 100) / 100f * w
            val sy = ((i * 29) % 100) / 100f * horizon * 0.5f
            val tw = 0.4f + 0.6f * (0.5f + 0.5f * sin(tf / 500f + i))
            drawCircle(Color.White.copy(alpha = 0.55f * tw), 1.5f + (i % 2), Offset(sx, sy))
        }
        // Sol grande a medio hundir.
        val sunR = minOf(w, h) * 0.17f
        val sun = Offset(w * 0.5f, horizon - sunR * 0.25f)
        drawCircle(
            Brush.radialGradient(listOf(Color(0xCCFFE9A8), Color.Transparent), center = sun, radius = sunR * 3.4f),
            radius = sunR * 3.4f, center = sun,
        )
        drawCircle(lerp(Color(0xFFFFE08A), Color(0xFFFF8A3D), 0.55f), sunR, sun)

        // Isla lejana.
        val island = Path().apply {
            moveTo(w * 0.70f, horizon)
            lineTo(w * 0.76f, horizon - h * 0.045f)
            lineTo(w * 0.80f, horizon - h * 0.03f)
            lineTo(w * 0.86f, horizon - h * 0.06f)
            lineTo(w * 0.95f, horizon)
            close()
        }
        drawPath(island, Color(0xFF1B1A3A).copy(alpha = 0.75f))

        // Mar con reflejo.
        drawRect(
            Brush.verticalGradient(listOf(lerp(sky[1], Color(0xFF1A2250), 0.55f), Color(0xFF0B1033)), startY = horizon, endY = h),
            Offset(0f, horizon), Size(w, h - horizon),
        )
        for (i in 0 until 12) {
            val y = horizon + i * (h - horizon) * 0.065f
            val rw = sunR * (2.6f - i * 0.17f) * (0.8f + 0.2f * sin(tf / 320f + i))
            drawRect(Color(0xFFFFD27A).copy(alpha = (1f - i / 13f) * 0.7f), Offset(sun.x - rw / 2f, y), Size(rw, 3f + i * 0.5f))
        }

        // Pájaros.
        for (i in 0 until 3) {
            val bx = ((tf / 40f) + i * 170f) % (w + 120f) - 60f
            val by = h * (0.16f + 0.05f * i) + sin(tf / 500f + i) * 6f
            val flap = 4f + 3f * sin(tf / 140f + i * 2f)
            val bird = Path().apply {
                moveTo(bx - 9f, by - flap)
                lineTo(bx, by)
                lineTo(bx + 9f, by - flap)
            }
            drawPath(bird, Color(0xFF1B1A3A), style = Stroke(2.5f))
        }

        // Los dos personajes, juntos, sobre una roca oscura.
        // Con el final de amistad salen los cuatro, así que se achican un poco.
        val spriteH = if (partnerPainters.size > 1) h * 0.34f else h * 0.40f
        val spriteW = spriteH * 0.75f
        val feetY = h * 0.90f
        drawRect(Color(0xFF14122E), Offset(0f, feetY - spriteH * 0.02f), Size(w, h - feetY + spriteH * 0.02f))
        val sway = sin(tf / 1100f) * 3f
        fun sprite(p: Painter, cx: Float, extra: Float) {
            drawOval(Color(0x55000000), Offset(cx - spriteW * 0.4f, feetY - 4f), Size(spriteW * 0.8f, 9f))
            translate(left = cx - spriteW / 2f, top = feetY - spriteH + extra) {
                with(p) { draw(size = Size(spriteW, spriteH)) }
            }
        }
        if (partnerPainters.size > 1) {
            val all = partnerPainters + protaPainter
            all.forEachIndexed { i, p ->
                sprite(p, w * 0.5f + (i - (all.size - 1) / 2f) * spriteW * 0.86f, if (i % 2 == 0) sway else -sway)
            }
        } else {
            sprite(partnerPainters.first(), w * 0.5f - spriteW * 0.42f, sway)
            sprite(protaPainter, w * 0.5f + spriteW * 0.42f, -sway)
        }

        // Detalle propio de cada final.
        when (partnerId) {
            "valentina" -> for (i in 0 until 14) { // pétalos que caen
                val life = ((tf / 5200f) + i / 14f) % 1f
                val px = ((i * 67) % 100) / 100f * w + sin(life * 8f + i) * 18f
                val py = life * h * 0.9f
                drawOval(Color(0xFFFFB3C7).copy(alpha = 0.85f), Offset(px, py), Size(9f, 5f))
            }
            "lucas" -> for (i in 0 until 12) { // chispas de fogata que suben
                val life = ((tf / 3000f) + i / 12f) % 1f
                val px = w * 0.5f + sin(i * 2.4f + life * 5f) * w * 0.28f
                val py = feetY - life * h * 0.5f
                drawRect(Color(0xFFFFC857).copy(alpha = 1f - life), Offset(px, py), Size(3f, 3f))
            }
            else -> for (i in 0 until 10) { // luces que parpadean, como luciérnagas
                val px = ((i * 53) % 100) / 100f * w
                val py = h * (0.35f + ((i * 37) % 40) / 100f) + sin(tf / 900f + i) * 8f
                val blink = (0.5f + 0.5f * sin(tf / 420f + i * 1.7f))
                drawCircle(Color(0xFFE8D8FF).copy(alpha = 0.3f * blink), 12f, Offset(px, py))
                drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.9f * blink), 2.2f, Offset(px, py))
            }
        }

        // Corazones que suben entre los dos.
        for (i in 0 until 7) {
            val life = ((tf / 3600f) + i / 7f) % 1f
            val hx = w * 0.5f + sin(i * 2.1f + life * 5f) * spriteW * 0.35f
            val hy = feetY - spriteH * 0.85f - life * h * 0.28f
            val s = w * 0.014f * (0.8f + (i % 3) * 0.3f)
            drawPath(heart(hx, hy, s), HEART.copy(alpha = (1f - life).coerceIn(0f, 1f)))
        }
    }

    // Marco de postal y entrada desde negro.
    val frame = minOf(w, h) * 0.025f
    drawRect(Color(0xFFF5F0E6), Offset.Zero, Size(w, frame))
    drawRect(Color(0xFFF5F0E6), Offset(0f, h - frame), Size(w, frame))
    drawRect(Color(0xFFF5F0E6), Offset(0f, 0f), Size(frame, h))
    drawRect(Color(0xFFF5F0E6), Offset(w - frame, 0f), Size(frame, h))
    val fade = (1f - artMs / 1400f).coerceIn(0f, 1f)
    if (fade > 0f) drawRect(Color.Black.copy(alpha = fade), Offset.Zero, size)
}

private fun heart(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + s * 0.9f)
    cubicTo(cx - s * 1.6f, cy - s * 0.1f, cx - s * 0.9f, cy - s * 1.2f, cx, cy - s * 0.35f)
    cubicTo(cx + s * 0.9f, cy - s * 1.2f, cx + s * 1.6f, cy - s * 0.1f, cx, cy + s * 0.9f)
    close()
}
