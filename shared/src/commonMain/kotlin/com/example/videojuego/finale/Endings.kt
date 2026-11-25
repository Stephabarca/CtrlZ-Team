package com.example.videojuego.finale

import com.example.videojuego.data.save.SaveStorage

/** Qué finales ha visto el jugador. Es lógica pura para poder probarla. */
object Endings {
    private const val SEPARATOR = ","

    /** "lucas,lila" -> {lucas, lila}. Ignora espacios y nombres que no son candidatos. */
    fun parse(raw: String?): Set<String> =
        raw.orEmpty().split(SEPARATOR).map { it.trim() }.filter { it in FinaleRules.ALL_ENDINGS }.toSet()

    fun encode(ids: Set<String>): String =
        FinaleRules.ALL_ENDINGS.filter { it in ids }.joinToString(SEPARATOR)

    /** Agrega [id] a lo guardado y devuelve el texto nuevo. */
    fun withEnding(raw: String?, id: String): String = encode(parse(raw) + id)
}

/**
 * Guarda los finales vistos. Va aparte de la partida, así que "Nueva partida" no los borra: sirven para la galería
 * de finales del menú.
 */
class EndingsRepository(private val storage: SaveStorage) {
    fun seen(): Set<String> = Endings.parse(storage.read())

    fun record(id: String) {
        storage.write(Endings.withEnding(storage.read(), id))
    }
}
