package com.example.videojuego.navigation

/** Define el orden de las 4 fases del juego y las reglas de navegación. */
object GameFlow {
    val sequence: List<Screen> =
        listOf(Screen.Intro, Screen.Night1, Screen.Night2, Screen.Night3)

    /** Primera pantalla de una partida nueva. */
    fun first(): Screen = sequence.first()

    /** Siguiente pantalla de la partida, o null si es la última o no pertenece al flujo. */
    fun next(current: Screen): Screen? {
        val i = sequence.indexOf(current)
        return if (i == -1) null else sequence.getOrNull(i + 1)
    }

    /** True si el botón atrás debe pedir confirmación en esa pantalla. */
    fun requiresBackConfirmation(current: Screen): Boolean = current in sequence
}
