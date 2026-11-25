package com.example.videojuego.ui.world

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.videojuego.world.ItemType
import kotlin.math.abs
import kotlin.math.sin

/** Qué señal se muestra sobre un personaje del mapa. */
enum class BadgeKind { GOAL_PENDING, GOAL_READY, GIFT }

class Badge(val kind: BadgeKind, val item: ItemType?)

/** Lo que hace cada personaje por su cuenta mientras no se le habla. Solo de día y en el exterior. */
fun idleBob(name: String?, timeMs: Long, tile: Float): Float {
    if (name == null) return 0f
    val phase = name.length * 0.9f
    return abs(sin(timeMs / 520f + phase)) * tile * 0.05f
}

/** Dibuja el objeto de la actividad junto al personaje (escoba, leña, pensamiento). */
fun DrawScope.drawNpcActivity(name: String?, centerX: Float, feetY: Float, tile: Float, timeMs: Long) {
    val u = tile / 16f
    val tf = timeMs.toFloat()
    when (name) {
        "Valentina" -> {
            // Barre: la escoba va de un lado al otro y salen motitas de polvo.
            val sw = sin(tf / 320f)
            val bx = centerX + 8f * u + sw * 3f * u
            rotate(degrees = 18f * sw - 12f, pivot = Offset(bx, feetY - 8f * u)) {
                drawRect(Color(0xFF8A5A2B), Offset(bx, feetY - 14f * u), Size(1.5f * u, 13f * u))
                drawRect(Color(0xFFD9B35A), Offset(bx - 2f * u, feetY - 2f * u), Size(5.5f * u, 3f * u))
            }
            for (i in 0 until 3) {
                val life = ((tf / 900f) + i / 3f) % 1f
                drawRect(Color(0xFFE8DCC0).copy(alpha = 0.7f * (1f - life)), Offset(centerX + (4f + i * 3f + life * 6f) * u, feetY - (1f + life * 5f) * u), Size(1.5f * u, 1.5f * u))
            }
        }
        "Lucas" -> {
            // Junta leña: un pequeño haz de troncos a su lado y astillas que saltan al golpear.
            drawRect(Color(0xFFB5793F), Offset(centerX + 7f * u, feetY - 3f * u), Size(8f * u, 2.5f * u))
            drawRect(Color(0xFF8A5A2B), Offset(centerX + 8f * u, feetY - 5.5f * u), Size(7f * u, 2.5f * u))
            val hit = ((tf / 700f) % 1f)
            if (hit < 0.25f) {
                val k = hit / 0.25f
                drawRect(Color(0xFFE0B27A), Offset(centerX + (10f + k * 5f) * u, feetY - (6f + k * 6f) * u), Size(1.5f * u, 1.5f * u))
                drawRect(Color(0xFFE0B27A), Offset(centerX + (9f + k * 2f) * u, feetY - (6f + k * 8f) * u), Size(1.5f * u, 1.5f * u))
            }
        }
        "Lila" -> {
            // Mira el mar: burbuja de pensamiento con una ola que se mueve.
            val bobY = sin(tf / 700f) * u
            val bx = centerX + 9f * u
            val by = feetY - 22f * u + bobY
            drawCircle(Color.White.copy(alpha = 0.9f), 1.2f * u, Offset(centerX + 5f * u, feetY - 16f * u))
            drawCircle(Color.White.copy(alpha = 0.9f), 1.8f * u, Offset(centerX + 7f * u, feetY - 19f * u))
            drawCircle(Color.White.copy(alpha = 0.92f), 6f * u, Offset(bx + 4f * u, by))
            val wave = sin(tf / 300f) * 1.5f * u
            drawRect(Color(0xFF5BA4E6), Offset(bx + 0.5f * u, by + wave), Size(7f * u, 1.5f * u))
            drawRect(Color(0xFF8CC4F2), Offset(bx + 1.5f * u, by + 2.5f * u - wave), Size(5f * u, 1.5f * u))
        }
    }
}

/** Burbuja con el objeto que pide un objetivo, o una caja de regalo. Dorada si ya se puede entregar. */
fun DrawScope.drawBadge(badge: Badge, centerX: Float, top: Float, tile: Float, timeMs: Long) {
    val u = tile / 16f
    val side = tile * 0.9f
    val bob = if ((timeMs / 400L) % 2L == 0L) 0f else u
    val x = centerX - side / 2f
    val y = top + bob
    val border = when (badge.kind) {
        BadgeKind.GOAL_READY -> Color(0xFFFFC62E)
        BadgeKind.GOAL_PENDING -> Color(0xFF1F2A2C)
        BadgeKind.GIFT -> Color(0xFFE0566B)
    }
    drawRect(border, Offset(x - 2f * u, y - 2f * u), Size(side + 4f * u, side + 4f * u))
    drawRect(Color(0xFFFFF6E0), Offset(x, y), Size(side, side))
    when {
        badge.item != null && badge.kind != BadgeKind.GIFT -> drawItemIcon(badge.item, Offset(x + side * 0.1f, y + side * 0.1f), side * 0.8f)
        else -> {
            // Caja de regalo.
            val bu = side / 16f
            drawRect(Color(0xFFE0566B), Offset(x + 3f * bu, y + 6f * bu), Size(10f * bu, 8f * bu))
            drawRect(Color(0xFFFFE08A), Offset(x + 7f * bu, y + 6f * bu), Size(2f * bu, 8f * bu))
            drawRect(Color(0xFFFFE08A), Offset(x + 2f * bu, y + 4f * bu), Size(12f * bu, 3f * bu))
        }
    }
}
