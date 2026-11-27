package com.example.videojuego.credits

/** Contenido de la pantalla de créditos. Edita los textos aquí. */
object CreditsData {
    const val GAME = "ISLA PERDIDA"

    /** Línea bajo el título, como en una película. */
    const val PRESENTED_BY = "Un juego de Equipo CtrlZ"

    /** Última línea: se queda en el centro antes de volver al menú. */
    const val THE_END = "GRACIAS POR JUGAR"

    /** Un cargo y quién lo hizo (una o varias personas). */
    data class Role(val title: String, val names: List<String>) {
        constructor(title: String, vararg names: String) : this(title, names.toList())
    }

    /** Un área del equipo (DESARROLLO, ARTE...) con sus cargos. */
    data class Department(val title: String, val roles: List<Role>)

    /** Sección del equipo: sale justo antes de la lista de cargos. */
    val teamSection: Section = Section("EQUIPO", listOf("Equipo CtrlZ"))

    /** Lista de cargos de Control Z, en el orden en que suben en los créditos. */
    val departments: List<Department> = listOf(
        Department(
            "DESARROLLO",
            listOf(
                Role("Directora de Desarrollo", "Steph"),
                Role("Programadora de Mecánicas", "Steph"),
                Role("Desarrolladores", "Luis", "Steph", "Cris"),
                Role("Especialista en Lógica de Juego", "Cris"),
            ),
        ),
        Department(
            "ARTE",
            listOf(
                Role("Director de Arte", "Cris"),
                Role("Artista de Pixel Art", "Cris"),
                Role("Artista de Escenarios", "Steph"),
                Role("Diseñadora de Elementos Gráficos", "Steph"),
            ),
        ),
        Department(
            "MÚSICA",
            listOf(
                Role("Director Musical", "Luis"),
                Role("Compositor de Banda Sonora", "Luis"),
                Role("Editor de Audio", "Luis Vega Araya"),
            ),
        ),
        Department(
            "MENCIONES ESPECIALES",
            listOf(
                Role("Supervisor de Ideas de Último Minuto", "Luis"),
                Role("Responsable de la Magia Técnica", "Steph"),
                Role("Supervisor de Caos Controlado", "Cris"),
            ),
        ),
        Department(
            "MENTORES",
            listOf(Role("Mentores", "Kevin", "Beatriz")),
        ),
    )

    /** Todas las personas del equipo (sin repetir), sacadas de [departments]. */
    val team: List<String> get() = departments.flatMap { d -> d.roles.flatMap { it.names } }.distinct()

    /** Personajes (id del sprite y nombre). Salen con su retrato en el reparto. */
    data class CastMember(val id: String, val name: String)

    val cast: List<CastMember> = listOf(
        CastMember("valentina", "Valentina Oliva"),
        CastMember("lucas", "Lucas Ámbar"),
        CastMember("lila", "Lila Ariza"),
    )

    data class Section(val title: String, val lines: List<String>)

    /** Lo que sube después de los cargos. */
    val sections: List<Section> = listOf(
        Section(
            "INSPIRACIÓN",
            listOf(
                "Basado en las ideas de \"Sobre el mito del amor romántico. Amores cinematográficos y educación\", de Alicia Pascual Fernández.",
            ),
        ),
        Section(
            "CANCIÓN DE LOS CRÉDITOS",
            listOf("«Donde termina el mar»"),
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
