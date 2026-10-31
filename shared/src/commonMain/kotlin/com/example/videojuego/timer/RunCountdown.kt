package com.example.videojuego.timer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay

private const val IDLE_POLL_MS = 50L

/**
 * Hace correr [timer] mientras la pantalla se dibuja.
 *
 * Cuando la app pasa a segundo plano, Android deja de dibujar cuadros y esta corrutina se detiene.
 * Al volver, el primer intervalo es muy largo y [frameDelta] lo recorta, así que el tiempo que la app
 * estuvo en pausa no se descuenta.
 */
@Composable
fun RunCountdown(timer: CountdownTimer, maxFrameGapMs: Long = DEFAULT_MAX_FRAME_GAP_MS) {
    LaunchedEffect(timer) {
        var last: Long? = null
        while (true) {
            if (!timer.state.value.isRunning) {
                last = null // al reanudar no se cuenta el tiempo que estuvo detenido
                delay(IDLE_POLL_MS)
                continue
            }
            val now = withFrameMillis { it }
            val previous = last
            if (previous != null) timer.advance(frameDelta(previous, now, maxFrameGapMs))
            last = now
        }
    }
}
