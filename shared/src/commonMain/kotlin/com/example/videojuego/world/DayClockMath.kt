package com.example.videojuego.world

/** Momento del día para el reloj de la pantalla. */
enum class DayZone { MORNING, AFTERNOON, NIGHT }

/**
 * Cuentas del reloj del día: de cuánto del día ya pasó (0 a 1) y en qué parte estamos. Es lógica pura para poder
 * probarla; el dibujo está en ui/world/DayClock.kt.
 *
 * Las partes siguen lo que pasa en el juego: la noche del reloj empieza más o menos cuando el cielo se empieza a oscurecer
 * (los últimos [DUSK_MS] del día) y sigue durante toda la noche.
 */
object DayClockMath {
    /** Hasta aquí es mañana. */
    const val MORNING_END = 0.40f

    /** Hasta aquí es tarde; luego es noche. */
    const val AFTERNOON_END = 0.83f

    /** Cuánto del día ya pasó, de 0 (amanece) a 1 (cae la noche). De noche siempre es 1. */
    fun progress(night: Boolean, remainingMs: Long, dayLengthMs: Long = DAY_LENGTH_MS): Float {
        if (night || dayLengthMs <= 0L) return 1f
        return (1f - remainingMs.toFloat() / dayLengthMs).coerceIn(0f, 1f)
    }

    fun zone(progress: Float): DayZone = when {
        progress < MORNING_END -> DayZone.MORNING
        progress < AFTERNOON_END -> DayZone.AFTERNOON
        else -> DayZone.NIGHT
    }
}
