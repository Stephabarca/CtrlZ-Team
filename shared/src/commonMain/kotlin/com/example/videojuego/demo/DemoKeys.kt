package com.example.videojuego.demo

/**
 * Atajos ocultos para la presentación. Ponlo en false para desactivarlos por completo.
 *
 * Ctrl+Shift+2 / 3 = saltar a la noche 2 / 3 · S = tormenta · V / U / L = final de Valentina / Lucas / Lila
 * · A = final de amistad · F = final según la afinidad real · R = informe · T = tarjeta de título.
 */
const val DEMO_KEYS = true

sealed interface DemoCommand {
    data class Night(val order: Int) : DemoCommand
    data object Storm : DemoCommand
    data class Finale(val endingId: String?) : DemoCommand
    data object Report : DemoCommand
}

/** Letras de los atajos (en mayúscula). Separado de Compose para poder probarlo. */
object DemoKeyMap {
    fun commandFor(char: Char): DemoCommand? = when (char.uppercaseChar()) {
        '2' -> DemoCommand.Night(2)
        '3' -> DemoCommand.Night(3)
        'S' -> DemoCommand.Storm
        'V' -> DemoCommand.Finale("valentina")
        'U' -> DemoCommand.Finale("lucas")
        'L' -> DemoCommand.Finale("lila")
        'A' -> DemoCommand.Finale("amistad")
        'F' -> DemoCommand.Finale(null)
        'R' -> DemoCommand.Report
        else -> null
    }
}
