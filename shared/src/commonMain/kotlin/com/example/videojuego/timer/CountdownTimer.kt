package com.example.videojuego.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerState(
    val totalMs: Long,
    val remainingMs: Long,
    val isRunning: Boolean,
    /** Pasa a true una sola vez, cuando la cuenta llega a 0 (evento "tiempo agotado"). */
    val expired: Boolean,
) {
    val elapsedMs: Long get() = totalMs - remainingMs
    val fractionLeft: Float get() = remainingMs.toFloat() / totalMs
    /** Segundos que se muestran: se redondea hacia arriba, así "0s" solo aparece cuando ya terminó. */
    val secondsLeft: Long get() = (remainingMs + 999) / 1000
}

/**
 * Cuenta regresiva sin reloj propio (IP-10): quien la usa le dice cuánto tiempo pasó con [advance].
 * Así se prueba con un reloj virtual, y en la app la maneja [RunCountdown].
 *
 * @param initialRemainingMs sirve para restaurar la cuenta después de rotar la pantalla.
 */
class CountdownTimer(
    private val totalMs: Long,
    initialRemainingMs: Long = totalMs,
    startRunning: Boolean = true,
) {
    init {
        require(totalMs > 0) { "La duración debe ser mayor que 0" }
    }

    private val _state = MutableStateFlow(stateWith(initialRemainingMs.coerceIn(0L, totalMs), startRunning))
    val state: StateFlow<TimerState> = _state.asStateFlow()

    /** Descuenta [deltaMs]. No hace nada si está en pausa, ya terminó o el valor no es positivo. */
    fun advance(deltaMs: Long) {
        val current = _state.value
        if (!current.isRunning || current.expired || deltaMs <= 0L) return
        val remaining = (current.remainingMs - deltaMs).coerceAtLeast(0L)
        _state.value = current.copy(
            remainingMs = remaining,
            isRunning = remaining > 0L,
            expired = remaining == 0L,
        )
    }

    fun pause() {
        val current = _state.value
        if (current.isRunning) _state.value = current.copy(isRunning = false)
    }

    fun resume() {
        val current = _state.value
        if (!current.expired && !current.isRunning) _state.value = current.copy(isRunning = true)
    }

    /** Vuelve al tiempo completo y reanuda. */
    fun reset() {
        _state.value = stateWith(totalMs, running = true)
    }

    private fun stateWith(remaining: Long, running: Boolean) = TimerState(
        totalMs = totalMs,
        remainingMs = remaining,
        isRunning = running && remaining > 0L,
        expired = remaining <= 0L,
    )
}

/** Máximo que cuenta un solo intervalo entre cuadros. Un salto mayor se toma como "la app estaba en pausa". */
const val DEFAULT_MAX_FRAME_GAP_MS = 250L

/**
 * Tiempo a descontar entre dos cuadros. Al volver de segundo plano el primer intervalo es enorme
 * (todo el tiempo que estuvo en pausa); se recorta para que ese tiempo no cuente.
 */
fun frameDelta(previousMs: Long, currentMs: Long, maxGapMs: Long = DEFAULT_MAX_FRAME_GAP_MS): Long =
    (currentMs - previousMs).coerceIn(0L, maxGapMs)
