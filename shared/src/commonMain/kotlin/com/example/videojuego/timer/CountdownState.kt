package com.example.videojuego.timer

import kotlin.time.Duration

/** Estado del temporizador. */
enum class TimerStatus { Idle, Running, Paused, Finished }

/**
 * Estado inmutable de una cuenta regresiva. La lógica es pura (sin reloj ni
 * corrutinas) para poder probarla fácilmente en commonTest.
 */
data class CountdownState(
    val total: Duration,
    val remaining: Duration = total,
    val status: TimerStatus = TimerStatus.Idle,
) {
    /** Segundos para mostrar en pantalla, redondeados hacia arriba (0 solo al terminar). */
    val remainingSeconds: Int
        get() = ((remaining.inWholeMilliseconds + 999) / 1000).toInt()

    /** Progreso de 1.0 (tiempo completo) a 0.0 (agotado). */
    val progress: Float
        get() = if (total > Duration.ZERO) {
            (remaining / total).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    /**
     * Descuenta [elapsed] solo si el temporizador está corriendo. Nunca baja de cero
     * y pasa a [TimerStatus.Finished] al llegar a cero.
     */
    fun advance(elapsed: Duration): CountdownState {
        if (status != TimerStatus.Running) return this
        val left = (remaining - elapsed).coerceAtLeast(Duration.ZERO)
        return copy(
            remaining = left,
            status = if (left == Duration.ZERO) TimerStatus.Finished else TimerStatus.Running,
        )
    }
}
