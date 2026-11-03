package com.example.videojuego.ui.world

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.videojuego.ui.theme.IslaColors
import kotlin.math.hypot

/**
 * Joystick táctil de estilo cuadrado (pixel art).
 * [onChange] recibe la dirección de -1 a 1 en cada eje, y (0, 0) al soltar.
 */
@Composable
fun VirtualJoystick(
    onChange: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
) {
    val latest by rememberUpdatedState(onChange)
    var knob by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                val radius = this.size.width / 2f
                fun report(position: Offset) {
                    var v = (position - Offset(radius, radius)) / radius
                    val length = hypot(v.x, v.y)
                    if (length > 1f) v /= length
                    knob = v
                    latest(v)
                }
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        report(down.position)
                        var pressed = true
                        while (pressed) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) {
                                pressed = false
                            } else {
                                report(change.position)
                                change.consume()
                            }
                        }
                        knob = Offset.Zero
                        latest(Offset.Zero)
                    }
                }
            },
    ) {
        val side = this.size.width
        drawRect(Color(0x55FFF6E3), Offset.Zero, Size(side, side))
        drawRect(IslaColors.Ink, Offset.Zero, Size(side, side), style = Stroke(width = 6f))
        val knobSide = side * 0.38f
        val center = Offset(side / 2f + knob.x * side * 0.31f, side / 2f + knob.y * side * 0.31f)
        drawRect(IslaColors.Primary, Offset(center.x - knobSide / 2f, center.y - knobSide / 2f), Size(knobSide, knobSide))
        drawRect(
            IslaColors.Ink,
            Offset(center.x - knobSide / 2f, center.y - knobSide / 2f),
            Size(knobSide, knobSide),
            style = Stroke(width = 6f),
        )
    }
}
