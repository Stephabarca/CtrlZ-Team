package com.example.videojuego.credits

/** Contenido de la pantalla de créditos. Edita los textos aquí. */
object CreditsData {
    const val GAME = "ISLA PERDIDA"

    /** TODO: reemplazar por los nombres reales del equipo. */
    val team: List<String> = listOf(
        "Equipo CtrlZ",
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
            "TECNOLOGÍA",
            listOf("Kotlin Multiplatform", "Compose Multiplatform"),
        ),
        Section(
            "AGRADECIMIENTOS",
            listOf("A quienes prueban el juego y nos ayudan a mejorarlo."),
        ),
    )
}
