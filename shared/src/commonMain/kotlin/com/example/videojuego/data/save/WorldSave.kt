package com.example.videojuego.data.save

import kotlinx.serialization.Serializable

@Serializable
data class WorldSave(
    val day: Int = 1,
    val bag: Map<String, Int> = emptyMap(),
    val chest: Map<String, Int> = emptyMap(),
    val giftedNpcIds: List<String> = emptyList(),
)
