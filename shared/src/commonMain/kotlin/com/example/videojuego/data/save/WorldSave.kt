package com.example.videojuego.data.save

import kotlinx.serialization.Serializable

/**
 * Lo del mapa que se guarda al dormir en la cama: el día, la mochila, el cofre y los regalos de hoy.
 * Las claves de los mapas son el nombre de ItemType ("COCONUT", "COAL", "WOOD", "FISH").
 */
@Serializable
data class WorldSave(
    val day: Int = 1,
    val bag: Map<String, Int> = emptyMap(),
    val chest: Map<String, Int> = emptyMap(),
    val giftedNpcIds: List<String> = emptyList(),
    /** Días cuyo objetivo ya se cumplió (ver DayGoals). Los guardados viejos no lo traen y empiezan vacío. */
    val goalsDone: List<Int> = emptyList(),
)
