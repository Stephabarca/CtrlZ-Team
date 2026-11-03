package com.example.videojuego.world

enum class DayPhase { DAY, NIGHT }

/** Duración de un día en milisegundos: 5 minutos. */
const val DAY_LENGTH_MS = 5L * 60L * 1000L

/** Número de días (y de noches) del juego. */
const val TOTAL_DAYS = 3

/** En los últimos segundos del día el cielo se va oscureciendo poco a poco. */
const val DUSK_MS = 30_000L

private const val NIGHT_DARKNESS = 0.55f

/** Id de la noche que empieza al terminar el día [day]: día 1 → "night1". */
fun nightIdForDay(day: Int): String = "night$day"

/**
 * Qué tan oscuro está de 0 (pleno día) a [NIGHT_DARKNESS] (noche cerrada).
 * Se usa para dibujar la capa oscura sobre el mapa.
 */
fun darknessAt(phase: DayPhase, remainingMs: Long): Float = when {
    phase == DayPhase.NIGHT -> NIGHT_DARKNESS
    remainingMs >= DUSK_MS -> 0f
    else -> NIGHT_DARKNESS * (1f - remainingMs.coerceAtLeast(0L).toFloat() / DUSK_MS)
}

/** Formatea el tiempo restante como m:ss, redondeando hacia arriba (4:59 → 0:01 → 0:00). */
fun formatClock(remainingMs: Long): String {
    val totalSeconds = ((remainingMs.coerceAtLeast(0L) + 999L) / 1000L).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
