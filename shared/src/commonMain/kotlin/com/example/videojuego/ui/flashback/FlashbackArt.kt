package com.example.videojuego.ui.flashback

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.abs
import kotlin.math.sin

/**
 * Dibuja una escena de recuerdo: el lugar con formas simples, el personaje encima y, al final, un tono sepia con
 * bordes oscuros y grano de película para que se note que es un recuerdo.
 *
 * @param nowMs reloj general (para lo que se mueve siempre, como luces o llamas).
 * @param sceneMs tiempo desde que empezó esta escena (para la cámara lenta y los movimientos de la escena).
 * @param character sprite del personaje (el mismo del mapa).
 */
fun DrawScope.drawFlashbackScene(art: SceneArt, nowMs: Long, sceneMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val zoom = 1f + 0.06f * (sceneMs / 9000f).coerceIn(0f, 1f)
    scale(scale = zoom, pivot = Offset(w / 2f, h * 0.6f)) {
        when (art) {
            SceneArt.CINEMA -> cinema(nowMs, character)
            SceneArt.LIBRARY -> library(nowMs, character)
            SceneArt.DINNER -> dinner(nowMs, character)
            SceneArt.MOUNTAIN -> mountain(sceneMs, character)
            SceneArt.OFFICE -> office(nowMs, character)
            SceneArt.ARGUMENT -> argument(nowMs, sceneMs, character)
            SceneArt.STAGE -> stage(nowMs, character)
            SceneArt.HALLWAY -> hallway(sceneMs, character)
            SceneArt.ART_CLASS -> artClass(character)
            SceneArt.DOCK_NOTEBOOK, SceneArt.DOCK_MESSAGE, SceneArt.DOCK_LETTER -> dock(art, nowMs, character)
        }
    }

    // Tono sepia.
    drawRect(Color(0xFFB08A55), Offset.Zero, size, blendMode = BlendMode.Color)
    drawRect(Color(0xFF4A3520).copy(alpha = 0.18f), Offset.Zero, size)
    // Bordes oscuros.
    drawRect(
        Brush.radialGradient(
            listOf(Color.Transparent, Color(0xCC1A1008)),
            center = Offset(w / 2f, h / 2f),
            radius = maxOf(w, h) * 0.75f,
        ),
        Offset.Zero, size,
    )
    // Grano de película: puntitos que cambian unas veces por segundo.
    val frame = (nowMs / 90L).toInt()
    for (i in 0 until 38) {
        val gx = ((i * 7919 + frame * 104729).mod(1000)) / 1000f * w
        val gy = ((i * 6007 + frame * 15485863).mod(1000)) / 1000f * h
        drawRect(Color.White.copy(alpha = 0.10f), Offset(gx, gy), Size(2f, 2f))
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// Ayudas
// ---------------------------------------------------------------------------------------------------------------------

private fun DrawScope.person(p: Painter, cx: Float, feetY: Float, height: Float) {
    val width = height * 0.75f
    drawOval(Color(0x55000000), Offset(cx - width * 0.4f, feetY - 4f), Size(width * 0.8f, 9f))
    translate(left = cx - width / 2f, top = feetY - height) {
        with(p) { draw(size = Size(width, height)) }
    }
}

/** Una persona de fondo: solo una silueta, para no necesitar más sprites. */
private fun DrawScope.silhouette(cx: Float, feetY: Float, height: Float, color: Color = Color(0xFF2B2B3A)) {
    val bodyW = height * 0.38f
    drawRoundRect(color, Offset(cx - bodyW / 2f, feetY - height * 0.62f), Size(bodyW, height * 0.62f), CornerRadius(bodyW * 0.3f))
    drawCircle(color, height * 0.15f, Offset(cx, feetY - height * 0.78f))
}

// ---------------------------------------------------------------------------------------------------------------------
// Valentina
// ---------------------------------------------------------------------------------------------------------------------

private fun DrawScope.cinema(nowMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    drawRect(Color(0xFF1B1B2F), Offset.Zero, size)
    // Pantalla apagándose y luces de la marquesina que se alternan.
    val glow = 0.55f + 0.1f * sin(tf / 500f)
    drawRect(Color(0xFFCFE8FF).copy(alpha = glow), Offset(w * 0.18f, h * 0.10f), Size(w * 0.64f, h * 0.30f))
    for (i in 0 until 16) {
        val on = ((nowMs / 350L).toInt() + i) % 2 == 0
        drawCircle(
            if (on) Color(0xFFFFD84D) else Color(0xFF6B5B22),
            h * 0.012f, Offset(w * (0.06f + 0.88f * i / 15f), h * 0.05f),
        )
    }
    // Filas de asientos.
    for (row in 0 until 3) {
        for (i in 0 until 9) {
            drawRoundRect(
                Color(0xFF3A2438).copy(alpha = 0.9f - row * 0.2f),
                Offset(w * (0.04f + i * 0.11f), h * (0.50f + row * 0.07f)),
                Size(w * 0.09f, h * 0.06f), CornerRadius(8f),
            )
        }
    }
    val floor = h * 0.90f
    drawRect(Color(0xFF2A1F2E), Offset(0f, floor), Size(w, h - floor))
    // Palomitas en el piso.
    for (i in 0 until 14) {
        drawCircle(Color(0xFFFFF1C2), 3f, Offset(w * ((i * 53 % 100) / 100f), floor + h * 0.015f * (i % 4)))
    }
    // Compañera que llora y Valentina con la escoba.
    silhouette(w * 0.72f, floor, h * 0.34f, Color(0xFF55506B))
    val tear = ((tf / 900f) % 1f)
    drawCircle(Color(0xFF8EC5FF).copy(alpha = 1f - tear), 3f, Offset(w * 0.72f - 8f, floor - h * 0.26f + tear * h * 0.10f))
    drawCircle(Color(0xFF8EC5FF).copy(alpha = 1f - tear), 3f, Offset(w * 0.72f + 8f, floor - h * 0.26f + tear * h * 0.10f))
    person(character, w * 0.38f, floor, h * 0.46f)
    drawLine(Color(0xFF7A5230), Offset(w * 0.38f + h * 0.10f, floor - h * 0.32f), Offset(w * 0.38f + h * 0.22f, floor), strokeWidth = 5f)
}

private fun DrawScope.library(nowMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    drawRect(Color(0xFF3B2A1C), Offset.Zero, size)
    val palette = listOf(Color(0xFF8B3A3A), Color(0xFF3A5A8B), Color(0xFF3A8B5A), Color(0xFFB8893A), Color(0xFF6B3A8B))
    for (shelf in 0 until 3) {
        val y = h * (0.10f + shelf * 0.17f)
        var x = w * 0.03f
        var i = 0
        while (x < w * 0.97f) {
            val bw = w * (0.018f + (i * 7 % 3) * 0.006f)
            val bh = h * (0.09f + (i * 5 % 4) * 0.012f)
            drawRect(palette[(i + shelf) % palette.size], Offset(x, y + h * 0.13f - bh), Size(bw, bh))
            x += bw + 3f
            i++
        }
        drawRect(Color(0xFF241608), Offset(0f, y + h * 0.13f), Size(w, h * 0.012f))
    }
    val desk = h * 0.72f
    drawRect(Color(0xFF5A3A22), Offset(0f, desk), Size(w, h - desk))
    drawRect(Color(0xFFE8DDB5), Offset(w * 0.30f, desk - h * 0.01f), Size(w * 0.14f, h * 0.02f)) // libro abierto
    // Teléfono que vibra con avisos que suben.
    val shake = sin(tf / 40f) * 1.5f
    drawRoundRect(Color(0xFF1A1A22), Offset(w * 0.66f + shake, desk - h * 0.05f), Size(w * 0.05f, h * 0.045f), CornerRadius(6f))
    drawRect(Color(0xFF8EC5FF).copy(alpha = 0.7f), Offset(w * 0.665f + shake, desk - h * 0.045f), Size(w * 0.04f, h * 0.034f))
    for (i in 0 until 3) {
        val life = ((tf / 1800f) + i / 3f) % 1f
        val bx = w * (0.64f + i * 0.04f)
        val by = desk - h * 0.08f - life * h * 0.22f
        drawCircle(Color(0xFFFF5C5C).copy(alpha = 1f - life), h * 0.016f, Offset(bx, by))
        drawCircle(Color.White.copy(alpha = 1f - life), h * 0.004f, Offset(bx, by))
    }
    person(character, w * 0.50f, desk + h * 0.10f, h * 0.50f)
}

private fun DrawScope.dinner(nowMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    drawRect(Color(0xFF2A1A18), Offset.Zero, size)
    // Ventana con luna, a la izquierda.
    drawRect(Color(0xFF14204A), Offset(w * 0.04f, h * 0.12f), Size(w * 0.18f, h * 0.40f))
    drawCircle(Color(0xFFF5F0D0), h * 0.04f, Offset(w * 0.13f, h * 0.24f))
    drawRect(Color(0xFF3B2418), Offset(w * 0.04f, h * 0.31f), Size(w * 0.18f, h * 0.012f))
    drawRect(Color(0xFF3B2418), Offset(w * 0.125f, h * 0.12f), Size(w * 0.012f, h * 0.40f))
    // Mesa, velas y gente riendo.
    val table = h * 0.70f
    drawRect(Color(0xFF6B4226), Offset(w * 0.25f, table), Size(w * 0.72f, h * 0.05f))
    for (i in 0 until 3) {
        val cx = w * (0.45f + i * 0.14f)
        drawRect(Color(0xFFF5F0E6), Offset(cx - 4f, table - h * 0.05f), Size(8f, h * 0.05f))
        val flick = 1f + 0.25f * sin(tf / 90f + i * 2f)
        drawOval(Color(0xFFFFB347), Offset(cx - 6f, table - h * 0.09f), Size(12f, 20f * flick))
        drawCircle(Color(0xFFFFB347).copy(alpha = 0.12f), h * 0.10f, Offset(cx, table - h * 0.06f))
    }
    for (i in 0 until 4) {
        val bob = abs(sin(tf / 260f + i * 1.3f)) * h * 0.012f
        silhouette(w * (0.40f + i * 0.17f), table + h * 0.02f - bob, h * 0.36f, Color(0xFF4A3A38))
    }
    // Valentina, junto a la ventana, sonríe pero mira hacia afuera.
    person(character, w * 0.32f, h * 0.92f, h * 0.50f)
}

// ---------------------------------------------------------------------------------------------------------------------
// Lucas
// ---------------------------------------------------------------------------------------------------------------------

private fun DrawScope.mountain(sceneMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val rise = (sceneMs / 7000f).coerceIn(0f, 1f)
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFF6A7FB5), Color(0xFFF3C58B)), startY = 0f, endY = h * 0.6f),
        Offset.Zero, Size(w, h * 0.6f),
    )
    // Sol que sale despacio detrás de las montañas.
    val sunY = h * (0.52f - 0.20f * rise)
    drawCircle(Color(0x66FFE9A8), h * 0.22f, Offset(w * 0.68f, sunY))
    drawCircle(Color(0xFFFFE08A), h * 0.07f, Offset(w * 0.68f, sunY))
    fun ridge(color: Color, base: Float, peaks: List<Pair<Float, Float>>) {
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, base)
            peaks.forEach { (x, y) -> lineTo(w * x, h * y) }
            lineTo(w, base)
            lineTo(w, h)
            close()
        }
        drawPath(path, color)
    }
    ridge(Color(0xFF6B7BA8), h * 0.55f, listOf(0.15f to 0.38f, 0.35f to 0.50f, 0.55f to 0.34f, 0.80f to 0.48f, 1f to 0.42f))
    ridge(Color(0xFF4A5F6B), h * 0.65f, listOf(0.10f to 0.55f, 0.30f to 0.62f, 0.60f to 0.50f, 0.85f to 0.60f))
    ridge(Color(0xFF2E4A3A), h * 0.80f, listOf(0.20f to 0.72f, 0.50f to 0.78f, 0.75f to 0.68f, 1f to 0.76f))
    // Sendero y Lucas caminando.
    val trail = Path().apply {
        moveTo(w * 0.05f, h * 0.97f)
        cubicTo(w * 0.30f, h * 0.90f, w * 0.20f, h * 0.84f, w * 0.45f, h * 0.80f)
        cubicTo(w * 0.60f, h * 0.77f, w * 0.55f, h * 0.73f, w * 0.78f, h * 0.70f)
    }
    drawPath(trail, Color(0xFFD9C08A), style = Stroke(h * 0.012f))
    val walk = (sceneMs / 9000f).coerceIn(0f, 1f)
    person(character, w * (0.22f + 0.30f * walk), h * (0.92f - 0.08f * walk), h * (0.34f - 0.06f * walk))
}

private fun DrawScope.office(nowMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    drawRect(Color(0xFF2C3440), Offset.Zero, size)
    drawRect(Color(0xFF1B2230), Offset(w * 0.55f, h * 0.10f), Size(w * 0.40f, h * 0.30f)) // ventana
    for (i in 0 until 5) drawRect(Color(0xFF3A4660), Offset(w * (0.58f + i * 0.07f), h * (0.28f - (i % 3) * 0.04f)), Size(w * 0.04f, h * (0.12f + (i % 3) * 0.04f)))
    val desk = h * 0.70f
    drawRect(Color(0xFF4A3F36), Offset(0f, desk), Size(w, h - desk))
    // Monitor con gráficos que suben y bajan.
    drawRect(Color(0xFF0E1420), Offset(w * 0.42f, desk - h * 0.26f), Size(w * 0.30f, h * 0.22f))
    for (i in 0 until 7) {
        val bar = h * (0.04f + 0.10f * (0.5f + 0.5f * sin(tf / 700f + i * 0.9f)))
        drawRect(Color(0xFF6BD0A0), Offset(w * (0.44f + i * 0.04f), desk - h * 0.06f - bar), Size(w * 0.025f, bar))
    }
    // Compañero que habla sin que lo oigan: "..." en un globo.
    silhouette(w * 0.12f, h * 0.92f, h * 0.46f, Color(0xFF55607A))
    drawRoundRect(Color(0xFFF5F0E6), Offset(w * 0.14f, h * 0.34f), Size(w * 0.14f, h * 0.09f), CornerRadius(14f))
    for (i in 0 until 3) {
        val up = if (((nowMs / 350L).toInt() % 3) == i) -4f else 0f
        drawCircle(Color(0xFF2B2B3A), 4f, Offset(w * (0.17f + i * 0.04f), h * 0.385f + up))
    }
    person(character, w * 0.50f, h * 0.93f, h * 0.46f)
    // Auriculares sobre la cabeza.
    drawArc(Color(0xFF111111), 180f, 180f, false, Offset(w * 0.50f - h * 0.07f, h * 0.93f - h * 0.455f), Size(h * 0.14f, h * 0.12f), style = Stroke(5f))
}

private fun DrawScope.argument(nowMs: Long, sceneMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    drawRect(Color(0xFF1E1A22), Offset.Zero, size)
    drawRect(Color(0xFF2B2530), Offset(0f, h * 0.78f), Size(w, h * 0.22f))
    // Mesa con el teléfono que brilla.
    val table = h * 0.74f
    drawRect(Color(0xFF4A3A30), Offset(w * 0.30f, table), Size(w * 0.40f, h * 0.04f))
    val pulse = 0.6f + 0.2f * sin(tf / 400f)
    drawCircle(Color(0xFF8EC5FF).copy(alpha = 0.18f * pulse), h * 0.12f, Offset(w * 0.50f, table - h * 0.02f))
    drawRoundRect(Color(0xFF111118), Offset(w * 0.48f, table - h * 0.045f), Size(w * 0.04f, h * 0.045f), CornerRadius(5f))
    drawRect(Color(0xFF8EC5FF).copy(alpha = pulse), Offset(w * 0.485f, table - h * 0.04f), Size(w * 0.03f, h * 0.034f))
    // La otra persona, con el brazo extendido hacia el teléfono.
    silhouette(w * 0.20f, h * 0.92f, h * 0.50f, Color(0xFF55506B))
    drawLine(Color(0xFF55506B), Offset(w * 0.20f + h * 0.04f, h * 0.92f - h * 0.30f), Offset(w * 0.46f, table - h * 0.03f), strokeWidth = h * 0.018f)
    // Lucas se va: camina hacia la derecha.
    val leave = (sceneMs / 8000f).coerceIn(0f, 1f)
    person(character, w * (0.78f + 0.12f * leave), h * 0.93f, h * 0.48f)
}

// ---------------------------------------------------------------------------------------------------------------------
// Lila
// ---------------------------------------------------------------------------------------------------------------------

private fun DrawScope.stage(nowMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    drawRect(Color(0xFF5A1A22), Offset.Zero, size)
    for (i in 0 until 12) {
        drawRect(Color(0xFF3D0F16).copy(alpha = 0.5f), Offset(w * i / 12f, 0f), Size(w / 24f, h * 0.7f))
    }
    // Haz de luz sobre el atril.
    val cone = Path().apply {
        moveTo(w * 0.50f - 6f, 0f); lineTo(w * 0.50f + 6f, 0f)
        lineTo(w * 0.72f, h * 0.80f); lineTo(w * 0.28f, h * 0.80f); close()
    }
    drawPath(cone, Color(0xFFFFF4D6).copy(alpha = 0.16f))
    val floor = h * 0.80f
    drawRect(Color(0xFF3A2418), Offset(0f, floor), Size(w, h - floor))
    drawRect(Color(0xFF6B4226), Offset(w * 0.56f, floor - h * 0.20f), Size(w * 0.10f, h * 0.20f)) // atril
    person(character, w * 0.44f, floor + h * 0.06f, h * 0.50f)
    // Público: cabezas oscuras que aplauden con destellos.
    for (i in 0 until 14) {
        val x = w * (0.04f + i * 0.07f)
        val bob = abs(sin(tf / 220f + i)) * h * 0.012f
        drawCircle(Color(0xFF14080C), h * 0.045f, Offset(x, h * 0.95f - bob))
    }
    for (i in 0 until 9) {
        val life = ((tf / 700f) + i * 0.31f) % 1f
        drawRect(
            Color(0xFFFFE08A).copy(alpha = 1f - life),
            Offset(w * ((i * 37 % 100) / 100f), h * 0.88f - life * h * 0.10f), Size(4f, 4f),
        )
    }
}

private fun DrawScope.hallway(sceneMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val vx = w * 0.62f
    val vy = h * 0.42f
    drawRect(Color(0xFF39404E), Offset.Zero, size)
    // Piso y techo en perspectiva.
    val floor = Path().apply { moveTo(0f, h); lineTo(w, h); lineTo(vx + w * 0.08f, vy + h * 0.08f); lineTo(vx - w * 0.08f, vy + h * 0.08f); close() }
    drawPath(floor, Color(0xFF6B6558))
    val ceiling = Path().apply { moveTo(0f, 0f); lineTo(w, 0f); lineTo(vx + w * 0.08f, vy - h * 0.08f); lineTo(vx - w * 0.08f, vy - h * 0.08f); close() }
    drawPath(ceiling, Color(0xFF2A303C))
    // Casilleros a ambos lados, cada vez más chicos.
    for (i in 0 until 7) {
        val t = i / 7f
        val k = 1f - t * 0.85f
        val lockerW = w * 0.09f * k
        val topY = vy - h * 0.08f - (1f - t) * h * 0.30f
        val botY = vy + h * 0.08f + (1f - t) * h * 0.34f
        val leftX = vx - w * 0.08f - (1f - t) * w * 0.50f
        val rightX = vx + w * 0.08f + (1f - t) * w * 0.50f - lockerW
        val shade = Color(0xFF4A6A8B).copy(alpha = 0.55f + 0.4f * t)
        drawRect(shade, Offset(leftX, topY), Size(lockerW, botY - topY))
        drawRect(shade, Offset(rightX, topY), Size(lockerW, botY - topY))
    }
    // La compañera se aleja por el fondo, cada vez más pequeña.
    val away = (sceneMs / 8000f).coerceIn(0f, 1f)
    silhouette(vx, vy + h * 0.10f + h * 0.01f, h * (0.22f - 0.12f * away), Color(0xFF1E1E2A))
    person(character, w * 0.24f, h * 0.95f, h * 0.52f)
}

private fun DrawScope.artClass(character: Painter) {
    val w = size.width
    val h = size.height
    drawRect(Color(0xFFD8C7A5), Offset.Zero, size)
    drawRect(Color(0xFF8B7A5A), Offset(0f, h * 0.80f), Size(w, h * 0.20f))
    // Ventanas altas.
    for (i in 0 until 3) drawRect(Color(0xFFBFD8E8), Offset(w * (0.08f + i * 0.30f), h * 0.08f), Size(w * 0.18f, h * 0.26f))
    // Caballetes con lienzos de colores.
    val colors = listOf(Color(0xFFD9546B), Color(0xFF4A8BD0), Color(0xFFF2C14E), Color(0xFF6BBF7A))
    for (i in 0 until 4) {
        val x = w * (0.45f + i * 0.13f)
        drawRect(Color(0xFFF5F0E6), Offset(x, h * 0.40f), Size(w * 0.10f, h * 0.20f))
        drawCircle(colors[i], h * 0.035f, Offset(x + w * 0.05f, h * 0.50f))
        drawLine(Color(0xFF5A3A22), Offset(x + w * 0.015f, h * 0.60f), Offset(x - w * 0.005f, h * 0.82f), strokeWidth = 4f)
        drawLine(Color(0xFF5A3A22), Offset(x + w * 0.085f, h * 0.60f), Offset(x + w * 0.105f, h * 0.82f), strokeWidth = 4f)
    }
    // Alumno con la cabeza baja.
    silhouette(w * 0.82f, h * 0.93f, h * 0.38f, Color(0xFF6B6B8B))
    drawCircle(Color(0xFF6B6B8B), h * 0.02f, Offset(w * 0.82f + 4f, h * 0.93f - h * 0.28f))
    // Lila señala el dibujo con el brazo extendido.
    person(character, w * 0.30f, h * 0.95f, h * 0.54f)
    drawLine(Color(0xFF3A2A1C), Offset(w * 0.30f + h * 0.10f, h * 0.95f - h * 0.30f), Offset(w * 0.52f, h * 0.45f), strokeWidth = 4f)
}

// ---------------------------------------------------------------------------------------------------------------------
// El muelle (la última escena de cada historia)
// ---------------------------------------------------------------------------------------------------------------------

private fun DrawScope.dock(art: SceneArt, nowMs: Long, character: Painter) {
    val w = size.width
    val h = size.height
    val tf = nowMs.toFloat()
    val horizon = h * 0.50f
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFF7A8FC0), Color(0xFFF5C28A)), startY = 0f, endY = horizon),
        Offset.Zero, Size(w, horizon),
    )
    drawCircle(Color(0x66FFE9A8), h * 0.20f, Offset(w * 0.5f, horizon))
    drawCircle(Color(0xFFFFE08A), h * 0.06f, Offset(w * 0.5f, horizon))
    // Mar grande, con olitas.
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFF3F6FA0), Color(0xFF1C3A5F)), startY = horizon, endY = h),
        Offset(0f, horizon), Size(w, h - horizon),
    )
    for (i in 0 until 8) {
        val y = horizon + (h - horizon) * (i + 1) / 9f
        drawLine(Color.White.copy(alpha = 0.18f), Offset(w * 0.1f + sin(tf / 800f + i) * 14f, y), Offset(w * 0.9f + sin(tf / 800f + i) * 14f, y), strokeWidth = 2f)
    }
    // Muelle de madera en perspectiva.
    val pier = Path().apply { moveTo(w * 0.30f, h); lineTo(w * 0.70f, h); lineTo(w * 0.54f, horizon + h * 0.04f); lineTo(w * 0.46f, horizon + h * 0.04f); close() }
    drawPath(pier, Color(0xFF8B6A45))
    for (i in 0 until 8) {
        val t = i / 8f
        val y = h - (h - horizon - h * 0.04f) * t * t
        val half = w * (0.20f - 0.12f * t * t)
        drawLine(Color(0xFF5A4028), Offset(w * 0.5f - half, y), Offset(w * 0.5f + half, y), strokeWidth = 2f + (1f - t) * 3f)
    }
    drawRect(Color(0xFF3A2A1C), Offset(w * 0.30f - 6f, h * 0.78f), Size(12f, h * 0.22f)) // poste
    drawRect(Color(0xFF3A2A1C), Offset(w * 0.70f - 6f, h * 0.78f), Size(12f, h * 0.22f))

    val feet = h * 0.90f
    val ph = h * 0.46f
    person(character, w * 0.5f, feet, ph)
    when (art) {
        SceneArt.DOCK_NOTEBOOK -> { // cuaderno nuevo en la mano
            drawRect(Color(0xFFD9546B), Offset(w * 0.5f + ph * 0.18f, feet - ph * 0.42f), Size(ph * 0.12f, ph * 0.16f))
            drawRect(Color(0xFFF5F0E6), Offset(w * 0.5f + ph * 0.20f, feet - ph * 0.41f), Size(ph * 0.09f, ph * 0.14f))
        }
        SceneArt.DOCK_MESSAGE -> { // mensaje que sale hacia su amiga
            val up = (tf / 1500f) % 1f
            val ex = w * 0.5f + ph * 0.2f + up * w * 0.20f
            val ey = feet - ph * 0.9f - up * h * 0.12f
            drawRect(Color(0xFFF5F0E6).copy(alpha = 1f - up), Offset(ex, ey), Size(ph * 0.14f, ph * 0.09f))
            drawLine(Color(0xFF8B6A45).copy(alpha = 1f - up), Offset(ex, ey), Offset(ex + ph * 0.07f, ey + ph * 0.05f), strokeWidth = 2f)
            drawLine(Color(0xFF8B6A45).copy(alpha = 1f - up), Offset(ex + ph * 0.14f, ey), Offset(ex + ph * 0.07f, ey + ph * 0.05f), strokeWidth = 2f)
        }
        else -> { // carta sin enviar, que asoma del bolso
            val bob = sin(tf / 600f) * 2f
            drawRect(Color(0xFFF5F0E6), Offset(w * 0.5f - ph * 0.22f, feet - ph * 0.40f + bob), Size(ph * 0.14f, ph * 0.09f))
            drawLine(Color(0xFF8B6A45), Offset(w * 0.5f - ph * 0.22f, feet - ph * 0.40f + bob), Offset(w * 0.5f - ph * 0.15f, feet - ph * 0.355f + bob), strokeWidth = 2f)
            drawLine(Color(0xFF8B6A45), Offset(w * 0.5f - ph * 0.08f, feet - ph * 0.40f + bob), Offset(w * 0.5f - ph * 0.15f, feet - ph * 0.355f + bob), strokeWidth = 2f)
        }
    }
}
