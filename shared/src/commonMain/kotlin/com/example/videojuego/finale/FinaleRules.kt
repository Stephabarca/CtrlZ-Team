package com.example.videojuego.finale

/** Con quién termina la historia, y cómo de reñido estuvo. */
data class FinaleResult(
    val partnerId: String,
    /** Puntaje de compatibilidad de cada candidato (lo usa la pantalla final para las barras). */
    val scores: Map<String, Int>,
    /** El segundo lugar quedó muy cerca del primero. */
    val closeCall: Boolean,
    val runnerUpId: String?,
)

/**
 * Elige con quién se queda el jugador según la compatibilidad de todo el juego. Es lógica pura (se puede probar).
 *
 * Compatibilidad = la afinidad guardada (respuestas, regalos y objetivos del día).
 * Si hay empate gana quien tenga mejor puntaje en su minijuego, y si sigue el empate, el orden de [ORDER].
 */
object FinaleRules {
    val ORDER: List<String> = listOf("valentina", "lucas", "lila")

    /** Si la diferencia con el segundo es de esto o menos, se cuenta como "muy parejo". */
    const val CLOSE_MARGIN = 3

    fun decide(
        affinity: Map<String, Int>,
        tieBreak: Map<String, Int> = emptyMap(),
        startAffinity: Int = 0,
        candidates: List<String> = ORDER,
    ): FinaleResult {
        require(candidates.isNotEmpty()) { "Hace falta al menos un candidato" }
        val scores = candidates.associateWith { affinity[it] ?: startAffinity }
        val ranked = candidates.sortedWith(
            compareByDescending<String> { scores.getValue(it) }
                .thenByDescending { tieBreak[it] ?: 0 }
                .thenBy { candidates.indexOf(it) },
        )
        val winner = ranked.first()
        val second = ranked.getOrNull(1)
        val close = second != null && scores.getValue(winner) - scores.getValue(second) <= CLOSE_MARGIN
        return FinaleResult(winner, scores, close, second)
    }
}
