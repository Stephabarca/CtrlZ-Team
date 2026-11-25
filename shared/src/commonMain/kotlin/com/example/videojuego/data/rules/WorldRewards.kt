package com.example.videojuego.data.rules

import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.save.SaveGame

/** Afinidad que se gana en el mapa (regalos y objetivos del día), con los mismos límites que en la ronda. */
object WorldRewards {
    fun addAffinity(content: GameContent, save: SaveGame, characterId: String, delta: Int): SaveGame {
        val current = save.affinity[characterId] ?: content.config.startAffinity
        val updated = ProgressRules.clamp(current + delta, content.config)
        return save.copy(affinity = save.affinity + (characterId to updated))
    }
}
