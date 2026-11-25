package com.example.videojuego.finale

/**
 * La "ruleta" de corazones de la sorpresa: el corazón salta de personaje en personaje, cada vez más lento,
 * y se detiene justo en quien eligió [FinaleRules]. Todo se calcula con el tiempo, así que no hay nada al azar.
 */
object FinaleRoll {
    /** Cuántos saltos da el corazón antes de detenerse. */
    const val STEPS = 16

    /** Cuánto se queda quieto el corazón en el elegido antes de seguir. */
    const val HOLD_MS = 1400L

    fun stepDuration(k: Int): Long = 100L + 14L * k

    val spinMs: Long = (0 until STEPS).sumOf { stepDuration(it) }

    val totalMs: Long = spinMs + HOLD_MS

    /** Los espacios que visita el corazón, en orden. El último es siempre [winnerSlot]. */
    fun sequence(winnerSlot: Int, slots: Int): List<Int> =
        List(STEPS) { k -> (((winnerSlot - (STEPS - 1 - k)) % slots) + slots) % slots }

    /** En qué espacio está el corazón a los [elapsedMs] de empezar. */
    fun slotAt(elapsedMs: Long, winnerSlot: Int, slots: Int): Int {
        val seq = sequence(winnerSlot, slots)
        var acc = 0L
        for (k in 0 until STEPS) {
            acc += stepDuration(k)
            if (elapsedMs < acc) return seq[k]
        }
        return winnerSlot
    }

    fun landed(elapsedMs: Long): Boolean = elapsedMs >= spinMs
}
