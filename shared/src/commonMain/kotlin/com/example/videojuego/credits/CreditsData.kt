package com.example.videojuego.credits

/** Contenido de la pantalla de créditos. Edita los textos aquí. */
object CreditsData {
    const val GAME = "ISLA PERDIDA"

    /** Línea bajo el título, como en una película. */
    const val PRESENTED_BY = "Un juego de Equipo CtrlZ"

    /** Última línea: se queda en el centro antes de volver al menú. */
    const val THE_END = "GRACIAS POR JUGAR"

    /** TODO: reemplazar por los nombres reales del equipo. */
    val team: List<String> = listOf(
        "Equipo CtrlZ",
    )

    /** Personajes (id del sprite y nombre). Salen con su retrato en el reparto. */
    data class CastMember(val id: String, val name: String)

    val cast: List<CastMember> = listOf(
        CastMember("valentina", "Valentina Oliva"),
        CastMember("lucas", "Lucas Ámbar"),
        CastMember("lila", "Lila Ariza"),
    )

    data class Section(val title: String, val lines: List<String>)

    val sections: List<Section> = listOf(
        Section("EQUIPO", team),
        Section(
            "INSPIRACIÓN",
            listOf(
                "Basado en las ideas de \"Sobre el mito del amor romántico. Amores cinematográficos y educación\", de Alicia Pascual Fernández.",
            ),
        ),
        Section(
            "MÚSICA",
            listOf("«Donde termina el mar»", "Canción de los créditos"),
        ),
        Section(
            "TECNOLOGÍA",
            listOf("Kotlin Multiplatform", "Compose Multiplatform"),
        ),
        Section(
            "AGRADECIMIENTOS",
            listOf("A quienes prueban el juego y nos ayudan a mejorarlo."),
        ),
    )
}

/**
 * Movimiento de los créditos tipo película: todo sube a velocidad constante desde abajo de la pantalla hasta que
 * la última línea ([CreditsData.THE_END]) queda en el centro; ahí se detiene [HOLD_MS] y vuelve al menú.
 * Lógica pura (en píxeles), para poder probarla.
 */
object CreditsRoll {
    /** Velocidad de subida, en dp por segundo. */
    const val SPEED_DP_PER_S = 42f

    /** Cuánto se queda quieto el final antes de volver al menú. */
    const val HOLD_MS = 4_000L

    /**
     * Posición (y) de la parte de arriba de los créditos a los [elapsedMs]. Empieza justo debajo de la pantalla
     * ([viewportPx]) y no sube más allá de donde el final de los créditos ([contentPx]) queda en el centro.
     */
    fun topAt(elapsedMs: Long, speedPxPerS: Float, viewportPx: Float, contentPx: Float): Float {
        val y = viewportPx - elapsedMs / 1000f * speedPxPerS
        return maxOf(y, stopTop(viewportPx, contentPx))
    }

    /** Donde se detiene: el final de los créditos en el centro de la pantalla. */
    fun stopTop(viewportPx: Float, contentPx: Float): Float = viewportPx / 2f - contentPx

    /** Cuánto tarda en llegar al final (sin contar la pausa). */
    fun scrollMs(speedPxPerS: Float, viewportPx: Float, contentPx: Float): Long =
        ((viewportPx - stopTop(viewportPx, contentPx)) / speedPxPerS * 1000f).toLong()

    /** Ya terminaron los créditos (llegó al final y pasó la pausa): toca volver al menú. */
    fun finished(elapsedMs: Long, speedPxPerS: Float, viewportPx: Float, contentPx: Float): Boolean =
        contentPx > 0f && elapsedMs >= scrollMs(speedPxPerS, viewportPx, contentPx) + HOLD_MS
}
