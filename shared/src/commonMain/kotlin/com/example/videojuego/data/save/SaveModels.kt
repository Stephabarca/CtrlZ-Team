package com.example.videojuego.data.save

import kotlinx.serialization.Serializable

enum class GamePhase { INTRO, NIGHT_1, NIGHT_2, NIGHT_3, ENDING }

/** Una respuesta del jugador (IP-13). [answerId] es null si se agotó el tiempo. */
@Serializable
data class AnswerRecord(
    val questionId: String,
    val answerId: String?,
    val timeSpentMs: Long,
    val timedOut: Boolean,
    val affinityDelta: Int,
)

@Serializable
data class MinigameRecord(val minigameId: String, val score: Int, val affinityGranted: Int)

/** Progreso de una partida. Se guarda completo como un solo documento. */
@Serializable
data class SaveGame(
    val version: Int = CURRENT_VERSION,
    val phase: GamePhase = GamePhase.INTRO,
    val skippedTutorial: Boolean = false,
    val affinity: Map<String, Int> = emptyMap(),
    val answers: List<AnswerRecord> = emptyList(),
    val minigameResults: List<MinigameRecord> = emptyList(),
    val flags: Map<String, Boolean> = emptyMap(),
    val endingId: String? = null,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/** Preferencias del jugador (IP-37). */
@Serializable
data class GameSettings(
    val textDelayMs: Long = 30L,
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 0.8f,
) {
    /** Si hay valores fuera de rango se usan los límites permitidos. */
    fun sanitized() = copy(
        textDelayMs = textDelayMs.coerceIn(10L, 100L),
        musicVolume = musicVolume.coerceIn(0f, 1f),
        sfxVolume = sfxVolume.coerceIn(0f, 1f),
    )
}

sealed interface LoadResult {
    data class Loaded(val save: SaveGame) : LoadResult
    data object NoSave : LoadResult
    data object Corrupt : LoadResult
}
