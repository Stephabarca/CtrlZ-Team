package com.example.videojuego.world

/**
 * Clima de la isla: función pura del tiempo (sin azar ni estado guardado), así que se ve igual en cada
 * ejecución y no hay nada que guardar. Cada ciclo hay una llovizna corta con entrada y salida suaves.
 */
object Weather {
    const val CYCLE_MS = 150_000L
    /** Cuándo empieza la lluvia dentro del ciclo, cuánto dura el máximo y cuánto tarda en aparecer/irse. */
    const val RAIN_START_MS = 95_000L
    const val RAIN_FULL_MS = 14_000L
    const val RAIN_FADE_MS = 4_000L

    /** Intensidad de la lluvia entre 0 y 1. */
    fun rain(timeMs: Long): Float {
        val t = ((timeMs % CYCLE_MS) + CYCLE_MS) % CYCLE_MS - RAIN_START_MS
        val total = RAIN_FADE_MS * 2 + RAIN_FULL_MS
        if (t < 0 || t > total) return 0f
        return when {
            t < RAIN_FADE_MS -> t.toFloat() / RAIN_FADE_MS
            t < RAIN_FADE_MS + RAIN_FULL_MS -> 1f
            else -> 1f - (t - RAIN_FADE_MS - RAIN_FULL_MS).toFloat() / RAIN_FADE_MS
        }
    }

    /** Viento entre 0 y 1: sopla más con la lluvia y tiene una brisa suave el resto del ciclo. */
    fun wind(timeMs: Long): Float {
        val gust = ((timeMs % 20_000L) / 20_000f)
        val breeze = if (gust < 0.25f) gust / 0.25f else if (gust < 0.5f) 1f - (gust - 0.25f) / 0.25f else 0f
        return (0.15f + 0.35f * breeze + 0.5f * rain(timeMs)).coerceIn(0f, 1f)
    }
}
