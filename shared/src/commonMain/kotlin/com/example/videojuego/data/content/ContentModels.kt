package com.example.videojuego.data.content

import kotlinx.serialization.Serializable

/** Contenido del juego (solo lectura). Cada clase corresponde a una tabla del modelo de datos. */

@Serializable
data class AffinityStage(val id: String, val name: String, val minValue: Int)

@Serializable
data class GameConfig(
    val startAffinity: Int,
    val minAffinity: Int,
    val maxAffinity: Int,
    /** Efecto de afinidad cuando se agota el tiempo (IP-12). */
    val timeoutDelta: Int,
    val stages: List<AffinityStage>,
)

/** Qué demuestra una respuesta. [healthy] = false marca conductas de riesgo (celos, control, etc.). */
@Serializable
data class MotiveData(val id: String, val label: String, val healthy: Boolean)

@Serializable
data class CharacterData(
    val id: String,
    val name: String,
    val age: Int,
    val occupation: String,
    val accentColor: String,
    val difficulty: String,
    val learning: String,
    val summary: String,
    val boundaries: List<String>,
    val pendingLearning: String,
)

@Serializable
data class DialogueLineData(val speakerId: String, val text: String, val expression: String = "neutral")

@Serializable
data class AnswerData(
    val id: String,
    val text: String,
    val affinityDelta: Int,
    val motive: String,
    val feedback: String,
)

@Serializable
data class QuestionData(
    val id: String,
    val speakerId: String,
    val expression: String = "neutral",
    val prompt: String,
    val answers: List<AnswerData>,
)

@Serializable
data class NightData(
    val id: String,
    val order: Int,
    val characterId: String,
    val title: String,
    val backgroundId: String,
    val timeLimitSec: Int,
    /** Multiplicador del efecto de las respuestas: sube la dificultad (IP-14). */
    val answerWeight: Double,
    val minigameId: String,
    val intro: List<DialogueLineData>,
    val questions: List<QuestionData>,
    val outro: List<DialogueLineData>,
)

@Serializable
data class MinigameData(
    val id: String,
    val name: String,
    val characterId: String,
    val type: String,
    val maxAffinity: Int,
    val durationSec: Int,
    val difficulty: String,
)

@Serializable
data class EndingRule(
    val minCharactersAbove: Int = 0,
    val threshold: Int = 0,
    val noOneBelow: Int = 0,
)

@Serializable
data class EndingData(
    val id: String,
    val title: String,
    val text: String,
    val priority: Int,
    val rule: EndingRule,
)

data class GameContent(
    val config: GameConfig,
    val motives: List<MotiveData>,
    val characters: List<CharacterData>,
    val nights: List<NightData>,
    val minigames: List<MinigameData>,
    val endings: List<EndingData>,
) {
    fun character(id: String): CharacterData = characters.first { it.id == id }
    fun minigame(id: String): MinigameData = minigames.first { it.id == id }
    fun motive(id: String): MotiveData? = motives.firstOrNull { it.id == id }
}
