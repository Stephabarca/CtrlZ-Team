package com.example.videojuego.diary

import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.data.save.SaveGame

/** Una página del diario de la isla: un personaje y lo que el jugador sabe de él/ella. */
data class DiaryEntry(
    val characterId: String,
    val name: String,
    val occupation: String,
    val stageName: String,
    val affinityFraction: Float,
    /** Verdadero cuando ya se jugó la noche del personaje: solo entonces se muestran aprendizaje y límites. */
    val met: Boolean,
    val learning: String?,
    val boundaries: List<String>,
    val minigameScore: Int?,
)

object DiaryBuilder {
    fun build(content: GameContent, save: SaveGame?): List<DiaryEntry> {
        val answered = save?.answers.orEmpty().map { it.questionId }.toSet()
        return content.nights.sortedBy { it.order }.mapNotNull { night ->
            val c = content.characters.firstOrNull { it.id == night.characterId } ?: return@mapNotNull null
            val met = night.questions.any { it.id in answered }
            val affinity = save?.affinity?.get(c.id) ?: content.config.startAffinity
            val span = (content.config.maxAffinity - content.config.minAffinity).coerceAtLeast(1)
            DiaryEntry(
                characterId = c.id,
                name = c.name,
                occupation = c.occupation,
                stageName = ProgressRules.stageFor(affinity, content.config).name,
                affinityFraction = ((affinity - content.config.minAffinity).toFloat() / span).coerceIn(0f, 1f),
                met = met,
                learning = if (met) c.learning else null,
                boundaries = if (met) c.boundaries else emptyList(),
                minigameScore = save?.minigameResults?.lastOrNull { it.minigameId == night.minigameId }?.score,
            )
        }
    }
}
