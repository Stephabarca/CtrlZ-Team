package com.example.videojuego.audio

import com.example.videojuego.world.DayPhase

/**
 * Música y sonidos de fondo (en bucle) del juego y el archivo de cada uno (dentro de composeResources).
 * [isSong]: las canciones usan el volumen de Música de Ajustes; los ambientes (playa, reloj, mar), el de Efectos.
 */
enum class MusicTrack(val path: String, val isSong: Boolean = false) {
    /** Canción del menú, «Island Sway». */
    MENU("files/music/island_sway.mp3", isSong = true),

    /** Canción de los créditos finales, «Donde termina el mar». */
    CREDITS("files/music/donde_termina_el_mar.mp3", isSong = true),

    /** Olas y ambiente de la playa de día, en el mapa de la isla. */
    BEACH_DAY("files/sounds/beach_day_sound.wav"),

    /** Ambiente de la playa cuando cae la noche, en el mapa de la isla. */
    BEACH_NIGHT("files/sounds/beach_night_sound.wav"),

    /** Tic tac del reloj durante la ronda de preguntas. */
    ROUND_CLOCK("files/sounds/round_clock.wav"),

    /**
     * Olas del mar abierto: durante Hydro Hopper (Noche 3, lo controla la pantalla del minijuego) y en la escena
     * final en la cubierta del barco.
     */
    SEA("files/sounds/sea_sound.wav"),

    /** Tormenta (lluvia, viento y truenos) al empezar la historia, con el avión en medio del temporal. */
    STORM("files/sounds/storm_sound.wav"),
}

/** Qué suena de fondo en cada pantalla de [com.example.videojuego.App]. null = silencio. */
object MusicRules {
    /** Volumen con el que suena [track]: el de Música si es canción, el de Efectos si es un ambiente. */
    fun volumeFor(track: MusicTrack?, musicVolume: Float, sfxVolume: Float): Float =
        if (track?.isSong == true) musicVolume else sfxVolume

    /**
     * El menú usa su música y los créditos su canción; el mapa, el ambiente de playa de día o de noche según [phase];
     * la ronda de preguntas, el reloj; la fogata, la playa de noche; el final en el barco, las olas del mar. Ajustes sigue con lo que suene en la pantalla desde la que se abrió
     * ([settingsReturn]), para no empezar la música del menú en pleno juego.
     */
    fun trackFor(screen: String, settingsReturn: String = "menu", phase: DayPhase = DayPhase.DAY): MusicTrack? =
        when (screen) {
            "menu", "endings" -> MusicTrack.MENU
            "credits" -> MusicTrack.CREDITS
            "world" -> if (phase == DayPhase.NIGHT) MusicTrack.BEACH_NIGHT else MusicTrack.BEACH_DAY
            "storm" -> MusicTrack.STORM
            "round" -> MusicTrack.ROUND_CLOCK
            // Fogata (noche 2): el ambiente de la playa de noche mientras arman el fuego.
            "build" -> MusicTrack.BEACH_NIGHT
            "finale" -> MusicTrack.SEA
            "settings" -> if (settingsReturn == "settings") null else trackFor(settingsReturn, phase = phase)
            else -> null
        }
}
