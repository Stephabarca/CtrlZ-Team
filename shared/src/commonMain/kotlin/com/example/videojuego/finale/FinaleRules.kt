package com.example.videojuego.finale

/** Con quién termina la historia, y cómo de reñido estuvo. */
data class FinaleResult(
    val partnerId: String,
    /** Puntaje de compatibilidad de cada candidato (lo usa la pantalla final para las barras). */
    val scores: Map<String, Int>,
    /** El segundo lugar quedó muy cerca del primero. */
    val closeCall: Boolean,
    val runnerUpId: String?,
    /** Nadie llegó a la compatibilidad necesaria para una pareja: el final es de amistad, con los tres. */
    val friendship: Boolean = false,
) {
    /** Identificador del final obtenido (para guardarlo y mostrarlo): el personaje, o "amistad". */
    val endingId: String get() = if (friendship) FinaleRules.FRIENDS_ID else partnerId
}

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

    /** Identificador del cuarto final: amistad con los tres. */
    const val FRIENDS_ID = "amistad"

    /**
     * Compatibilidad mínima para terminar en pareja. Está entre "Amistad" (50) y "Cercanía" (75) de la configuración:
     * una partida bien jugada llega a unos 80, y la de alguien que no se esforzó se queda cerca de 30.
     */
    const val ROMANCE_MIN = 65

    /** Todos los finales posibles, en el orden de la galería. */
    val ALL_ENDINGS: List<String> = ORDER + FRIENDS_ID

    /** Resultado forzado, para probar o enseñar un final concreto (modo demo). [endingId] puede ser "amistad". */
    fun forced(endingId: String): FinaleResult {
        val friends = endingId == FRIENDS_ID
        val partner = if (friends) ORDER.first() else endingId
        require(partner in ORDER) { "Final desconocido: $endingId" }
        val scores = ORDER.associateWith { if (friends) 45 else if (it == partner) 85 else 40 }
        return FinaleResult(partner, scores, closeCall = false, runnerUpId = null, friendship = friends)
    }

    fun decide(
        affinity: Map<String, Int>,
        tieBreak: Map<String, Int> = emptyMap(),
        startAffinity: Int = 0,
        candidates: List<String> = ORDER,
        romanceMin: Int = ROMANCE_MIN,
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
        return FinaleResult(winner, scores, close, second, friendship = scores.getValue(winner) < romanceMin)
    }
}
