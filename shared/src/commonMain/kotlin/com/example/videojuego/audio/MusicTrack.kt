package com.example.videojuego.audio

/** Canciones del juego y el archivo de cada una (dentro de composeResources). */
enum class MusicTrack(val path: String) {
    MENU("files/music/island_sway.mp3"),
}

/** Qué canción suena en cada pantalla de [com.example.videojuego.App]. null = silencio. */
object MusicRules {
    /**
     * Menú, ajustes y créditos usan la música del menú. Ajustes abierto desde la pausa del mapa ([settingsReturn] = "world")
     * sigue con lo que suene en esa pantalla, para no empezar la música del menú en pleno juego.
     */
    fun trackFor(screen: String, settingsReturn: String = "menu"): MusicTrack? = when (screen) {
        "menu", "credits" -> MusicTrack.MENU
        "settings" -> if (settingsReturn == "settings") null else trackFor(settingsReturn)
        else -> null
    }
}
