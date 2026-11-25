package com.example.videojuego.audio

import com.example.videojuego.world.DayPhase

/** Música y sonidos de fondo (en bucle) del juego y el archivo de cada uno (dentro de composeResources). */
enum class MusicTrack(val path: String) {
    MENU("files/music/island_sway.mp3"),

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
}

/** Qué suena de fondo en cada pantalla de [com.example.videojuego.App]. null = silencio. */
object MusicRules {
    /**
     * Menú y créditos usan la música del menú; el mapa, el ambiente de playa de día o de noche según [phase];
     * la ronda de preguntas, el reloj; el final en el barco, las olas del mar. Ajustes sigue con lo que suene en la pantalla desde la que se abrió
     * ([settingsReturn]), para no empezar la música del menú en pleno juego.
     */
    fun trackFor(screen: String, settingsReturn: String = "menu", phase: DayPhase = DayPhase.DAY): MusicTrack? =
        when (screen) {
            "menu", "credits", "endings" -> MusicTrack.MENU
            "world" -> if (phase == DayPhase.NIGHT) MusicTrack.BEACH_NIGHT else MusicTrack.BEACH_DAY
            "round" -> MusicTrack.ROUND_CLOCK
            "finale" -> MusicTrack.SEA
            "settings" -> if (settingsReturn == "settings") null else trackFor(settingsReturn, phase = phase)
            else -> null
        }
}
