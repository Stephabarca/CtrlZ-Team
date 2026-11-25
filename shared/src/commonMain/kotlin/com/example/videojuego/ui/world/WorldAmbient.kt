package com.example.videojuego.ui.world

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.videojuego.world.TileType
import com.example.videojuego.world.WorldMap
import com.example.videojuego.world.Weather
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Detalles que le dan vida a la isla: olas en la orilla y destellos en el agua (siempre), hojas que vuelan de día,
 * luciérnagas de noche y humo en la fogata. Todo se calcula con [timeMs], sin guardar nada.
 *
 * @param darkness 0 (pleno día) a ~0.55 (noche cerrada), la misma que usa el mapa para oscurecerse.
 */
fun DrawScope.drawAmbient(
    map: WorldMap,
    camX: Float,
    camY: Float,
    tile: Float,
    timeMs: Long,
    darkness: Float,
    /** Solo llueve en el exterior. */
    outdoors: Boolean = true,
) {
    val rain = if (outdoors) Weather.rain(timeMs) else 0f
    val wind = if (outdoors) Weather.wind(timeMs) else 0f
    val u = tile / 16f
    val tf = timeMs.toFloat()
    val firstX = floor(camX).toInt().coerceAtLeast(0)
    val lastX = ceil(camX + size.width / tile).toInt().coerceAtMost(map.width - 1)
    val firstY = floor(camY).toInt().coerceAtLeast(0)
    val lastY = ceil(camY + size.height / tile).toInt().coerceAtMost(map.height - 1)
    fun sx(x: Float) = ((x - camX) * tile).roundToInt().toFloat()
    fun sy(y: Float) = ((y - camY) * tile).roundToInt().toFloat()

    val fires = ArrayList<Offset>()
    for (ty in firstY..lastY) {
        for (tx in firstX..lastX) {
            when (map.tileAt(tx, ty)) {
                TileType.WATER -> {
                    val x = sx(tx.toFloat())
                    val y = sy(ty.toFloat())
                    // Espuma que va y viene en la orilla.
                    val foam = 0.5f + 0.5f * sin(tf / 650f + tx * 0.9f + ty * 0.6f)
                    val foamColor = Color.White.copy(alpha = 0.35f + 0.3f * foam)
                    if (map.tileAt(tx, ty + 1) == TileType.SAND) {
                        drawRect(foamColor, Offset(x + 1f * u, y + tile - (2f + 2f * foam) * u), Size(14f * u, (1.5f + foam) * u))
                    }
                    if (map.tileAt(tx, ty - 1) == TileType.SAND) {
                        drawRect(foamColor, Offset(x + 1f * u, y + (0.5f + 2f * foam) * u), Size(14f * u, (1.5f + foam) * u))
                    }
                    if (map.tileAt(tx + 1, ty) == TileType.SAND) {
                        drawRect(foamColor, Offset(x + tile - (2f + 2f * foam) * u, y + 1f * u), Size((1.5f + foam) * u, 14f * u))
                    }
                    if (map.tileAt(tx - 1, ty) == TileType.SAND) {
                        drawRect(foamColor, Offset(x + (0.5f + 2f * foam) * u, y + 1f * u), Size((1.5f + foam) * u, 14f * u))
                    }
                    // Destellos del agua.
                    val h = (tx * 37 + ty * 91) % 11
                    if (h < 3 && ((timeMs / 450L) + h + tx).mod(5L) == 0L) {
                        val glint = if (darkness > 0.3f) Color(0xFFBFD8FF) else Color.White
                        drawRect(glint.copy(alpha = 0.8f), Offset(x + (3 + h * 3) * u, y + (4 + h * 2) * u), Size(2f * u, 1f * u))
                    }
                }
                TileType.FIRE -> fires.add(Offset(sx(tx + 0.5f), sy(ty + 0.5f)))
                else -> Unit
            }
        }
    }

    // Humo de la fogata.
    for (fire in fires) {
        for (i in 0 until 6) {
            val life = ((tf / 2600f) + i / 6f) % 1f
            val px = fire.x + sin(life * 6f + i * 1.7f) * tile * 0.25f + life * tile * 0.2f
            val py = fire.y - tile * 0.35f - life * tile * 2.2f
            val r = tile * (0.12f + 0.2f * life)
            drawCircle(Color(0xFFCFCFD8).copy(alpha = 0.32f * (1f - life)), r, Offset(px, py))
        }
        // Chispas.
        if (darkness > 0.1f) {
            for (i in 0 until 4) {
                val life = ((tf / 1500f) + i * 0.27f) % 1f
                val px = fire.x + sin(i * 2.3f + life * 5f) * tile * 0.4f
                val py = fire.y - tile * 0.3f - life * tile * 1.5f
                drawRect(Color(0xFFFFC857).copy(alpha = 1f - life), Offset(px, py), Size(2f * u, 2f * u))
            }
        }
    }

    // Hojas que vuelan de día.
    val dayAmount = (1f - darkness / 0.3f).coerceIn(0f, 1f)
    if (dayAmount > 0f) {
        for (i in 0 until 9) {
            val speed = 0.018f + (i % 3) * 0.006f
            val travel = tf * speed + i * 211f
            val x = size.width - (travel % (size.width + 80f)) + 40f
            val y = ((i * 97) % 100) / 100f * size.height * 0.8f + (travel * 0.35f) % (size.height * 0.2f) +
                sin(tf / 600f + i) * tile * 0.35f
            val leaf = if (i % 2 == 0) Color(0xFF6FA94B) else Color(0xFFB5C95A)
            rotate(degrees = (tf / 8f + i * 50f) % 360f, pivot = Offset(x, y)) {
                drawRect(leaf.copy(alpha = 0.75f * dayAmount), Offset(x, y), Size(3.5f * u, 2f * u))
            }
        }
    }

    // Luciérnagas de noche.
    val nightAmount = ((darkness - 0.2f) / 0.25f).coerceIn(0f, 1f)
    if (nightAmount > 0f) {
        for (i in 0 until 16) {
            val bx = ((i * 53) % 100) / 100f * size.width
            val by = (0.25f + ((i * 31) % 70) / 100f) * size.height
            val x = bx + sin(tf / 1700f + i * 1.3f) * tile * 1.4f
            val y = by + sin(tf / 1300f + i * 2.1f) * tile * 0.9f
            val blink = (0.5f + 0.5f * sin(tf / 450f + i * 1.9f)).let { it * it }
            val c = Color(0xFFE8FF7A)
            drawCircle(c.copy(alpha = 0.18f * blink * nightAmount), tile * 0.32f, Offset(x, y))
            drawCircle(c.copy(alpha = 0.9f * blink * nightAmount), 1.8f * u, Offset(x, y))
        }
    }
}

/** Gotas de lluvia en diagonal y un tinte gris. [rain] y [wind] vienen de [Weather]. */
fun DrawScope.drawRain(rain: Float, wind: Float, timeMs: Long, tile: Float) {
    if (rain <= 0.01f) return
    drawRect(Color(0xFF3A4660).copy(alpha = 0.22f * rain), Offset.Zero, size)
    val drops = (70 + 90 * rain).toInt()
    val slant = tile * (0.25f + 0.6f * wind)
    val len = tile * 0.7f
    val tf = timeMs.toFloat()
    for (i in 0 until drops) {
        val speed = 0.0011f + (i % 4) * 0.00025f
        val fy = (tf * speed + i * 0.173f) % 1f
        val fx = (i * 0.6180339f) % 1f
        val x = fx * (size.width + slant * 4f) - fy * slant * 2f
        val y = fy * (size.height + len) - len
        drawLine(Color(0xFFCFE0FA).copy(alpha = 0.5f * rain), Offset(x, y), Offset(x - slant * 0.35f, y + len), strokeWidth = (tile / 14f).coerceAtLeast(1.5f))
    }
}
