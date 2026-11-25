package com.example.videojuego.ui.world

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import com.example.videojuego.world.ItemType

/** Cuánto dura un texto flotante. */
const val POP_LIFE_MS = 1300L

/** Cuánto ha avanzado un texto flotante (0 = recién salió, 1 = ya desapareció). */
fun popProgress(ageMs: Long): Float = (ageMs.toFloat() / POP_LIFE_MS).coerceIn(0f, 1f)

/** Opacidad de un texto flotante: se ve entero casi todo el tiempo y se desvanece al final. */
fun popAlpha(progress: Float): Float = (1f - ((progress - 0.6f) / 0.4f)).coerceIn(0f, 1f)

/** Un aviso que sube sobre el personaje: un objeto ("+1 madera") o un corazón ("+3"). */
class Pop(val item: ItemType?, val amount: Int) {
    /** Se pone en el primer cuadro de animación en que se muestra. */
    var startMs: Long = -1L
}

/** Los avisos que se están mostrando. Se anima solo mientras haya alguno. */
@Stable
class PopState {
    val pops = mutableStateListOf<Pop>()
    var nowMs by mutableStateOf(0L)

    fun item(item: ItemType, amount: Int) {
        if (amount > 0) pops.add(Pop(item, amount))
    }

    fun heart(amount: Int) {
        if (amount != 0) pops.add(Pop(null, amount))
    }
}

@Composable
fun rememberPopState(): PopState {
    val state = remember { PopState() }
    LaunchedEffect(state.pops.isNotEmpty()) {
        while (state.pops.isNotEmpty()) {
            withFrameMillis { t ->
                state.nowMs = t
                state.pops.forEach { if (it.startMs < 0L) it.startMs = t }
                state.pops.removeAll { it.startMs >= 0L && t - it.startMs > POP_LIFE_MS }
            }
        }
    }
    return state
}

/** Dibuja los avisos sobre el punto ([x], [y]) (la cabeza del personaje). */
fun DrawScope.drawPops(
    state: PopState,
    x: Float,
    y: Float,
    tile: Float,
    measurer: TextMeasurer,
    style: TextStyle,
) {
    val now = state.nowMs
    var slot = 0
    for (pop in state.pops) {
        if (pop.startMs < 0L) continue
        val p = popProgress(now - pop.startMs)
        val a = popAlpha(p)
        if (a <= 0f) continue
        val eased = 1f - (1f - p) * (1f - p)
        val top = y - tile * 0.9f - eased * tile * 1.5f - slot * tile * 0.62f
        slot++

        val sign = if (pop.amount > 0) "+" else ""
        val layout = measurer.measure("$sign${pop.amount}", style)
        val textW = layout.size.width.toFloat()
        val textH = layout.size.height.toFloat()
        val side = tile * 0.62f
        val gap = tile * 0.14f
        val totalW = side + gap + textW
        val left = x - totalW / 2f

        drawRoundRect(
            Color(0xFF1F2A2C).copy(alpha = 0.62f * a),
            Offset(left - tile * 0.18f, top - tile * 0.06f),
            Size(totalW + tile * 0.36f, maxOf(side, textH) + tile * 0.12f),
            CornerRadius(tile * 0.18f),
        )
        val iconTop = Offset(left, top + (maxOf(side, textH) - side) / 2f)
        if (pop.item != null) {
            drawItemIcon(pop.item, iconTop, side)
        } else {
            drawPopHeart(iconTop.x + side / 2f, iconTop.y + side / 2f, side * 0.5f, pop.amount > 0, a)
        }
        val color = if (pop.amount >= 0) Color(0xFFFFF4D6) else Color(0xFFFF8A80)
        drawText(layout, color = color.copy(alpha = a), topLeft = Offset(left + side + gap, top + (maxOf(side, textH) - textH) / 2f))
    }
}

private fun DrawScope.drawPopHeart(cx: Float, cy: Float, s: Float, good: Boolean, alpha: Float) {
    val path = Path().apply {
        moveTo(cx, cy + s * 0.9f)
        cubicTo(cx - s * 1.6f, cy - s * 0.1f, cx - s * 0.9f, cy - s * 1.2f, cx, cy - s * 0.35f)
        cubicTo(cx + s * 0.9f, cy - s * 1.2f, cx + s * 1.6f, cy - s * 0.1f, cx, cy + s * 0.9f)
        close()
    }
    drawPath(path, (if (good) Color(0xFFFF5C8A) else Color(0xFF8E8AA8)).copy(alpha = alpha))
}
