package com.example.videojuego.world

import com.example.videojuego.data.save.SaveGame

/**
 * Banderas de lo que pasó antes, para que los personajes lo recuerden al día siguiente.
 * Se calculan del guardado, así que no hay nada nuevo que guardar.
 */
object MemoryFlags {
    /** Mínimo de puntaje de la pesca para contarla como buena. */
    const val GOOD_FISHING_SCORE = 60

    fun from(save: SaveGame?): Set<String> {
        if (save == null) return emptySet()
        return buildSet {
            val night1 = save.answers.filter { it.questionId.startsWith("n1") }
            if (night1.isNotEmpty()) {
                if (night1.any { it.affinityDelta < 0 }) add("n1_riesgo")
                if (night1.all { !it.timedOut && it.affinityDelta > 0 }) add("n1_sana")
            }
            save.minigameResults.lastOrNull { it.minigameId == "pesca" }?.let {
                add(if (it.score >= GOOD_FISHING_SCORE) "pesca_buena" else "pesca_floja")
            }
        }
    }
}
