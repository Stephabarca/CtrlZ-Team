package com.example.videojuego.round

import com.example.videojuego.data.GameJson
import com.example.videojuego.data.save.SaveGame
import kotlinx.serialization.Serializable

/**
 * Lo mínimo para retomar la ronda tras rotar la pantalla o recrearse la actividad:
 * el progreso (respuestas ya dadas) y el tiempo que le quedaba a la pregunta en curso.
 */
@Serializable
data class RoundSnapshot(
    val save: SaveGame,
    /** Null cuando la pregunta en curso ya se respondió (la siguiente empieza con el tiempo completo). */
    val remainingMs: Long? = null,
)

object RoundSnapshotCodec {
    fun encode(snapshot: RoundSnapshot): String =
        GameJson.encodeToString(RoundSnapshot.serializer(), snapshot)

    /** Devuelve null si el texto está dañado. */
    fun decode(text: String): RoundSnapshot? =
        try {
            GameJson.decodeFromString(RoundSnapshot.serializer(), text)
        } catch (e: IllegalArgumentException) {
            null
        }
}
