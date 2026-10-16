package com.example.videojuego.data.rules

import com.example.videojuego.data.content.AffinityStage
import com.example.videojuego.data.content.AnswerData
import com.example.videojuego.data.content.EndingData
import com.example.videojuego.data.content.GameConfig
import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.content.MinigameData
import com.example.videojuego.data.content.NightData
import com.example.videojuego.data.content.QuestionData
import com.example.videojuego.data.save.AnswerRecord
import com.example.videojuego.data.save.GamePhase
import com.example.videojuego.data.save.MinigameRecord
import com.example.videojuego.data.save.SaveGame
import kotlin.math.roundToInt

/** Reglas puras que conectan el contenido (JSON) con el progreso (guardado). Cubiertas por pruebas unitarias. */
object ProgressRules {

    fun clamp(value: Int, config: GameConfig): Int = value.coerceIn(config.minAffinity, config.maxAffinity)

    fun newGame(content: GameContent): SaveGame = SaveGame(
        affinity = content.characters.associate { it.id to content.config.startAffinity },
    )

    fun withPhase(save: SaveGame, phase: GamePhase): SaveGame = save.copy(phase = phase)

    /**
     * Aplica una respuesta. Si [answer] es null se agotó el tiempo y se usa el efecto por defecto.
     * El efecto se multiplica por el peso de la noche y se acota al rango de afinidad.
     */
    fun recordAnswer(
        content: GameContent,
        save: SaveGame,
        night: NightData,
        question: QuestionData,
        answer: AnswerData?,
        timeSpentMs: Long,
    ): SaveGame {
        val raw = if (answer == null) {
            content.config.timeoutDelta
        } else {
            (answer.affinityDelta * night.answerWeight).roundToInt()
        }
        val (updated, applied) = changeAffinity(content, save, night.characterId, raw)
        val record = AnswerRecord(
            questionId = question.id,
            answerId = answer?.id,
            timeSpentMs = timeSpentMs,
            timedOut = answer == null,
            affinityDelta = applied,
        )
        return updated.copy(answers = updated.answers + record)
    }

    /** [score] de 0 a 100. Otorga afinidad proporcional hasta el máximo del minijuego. */
    fun recordMinigame(content: GameContent, save: SaveGame, minigame: MinigameData, score: Int): SaveGame {
        val safeScore = score.coerceIn(0, 100)
        val raw = minigame.maxAffinity * safeScore / 100
        val (updated, applied) = changeAffinity(content, save, minigame.characterId, raw)
        return updated.copy(
            minigameResults = updated.minigameResults + MinigameRecord(minigame.id, safeScore, applied),
        )
    }

    fun stageFor(affinity: Int, config: GameConfig): AffinityStage {
        val sorted = config.stages.sortedBy { it.minValue }
        return sorted.lastOrNull { affinity >= it.minValue } ?: sorted.first()
    }

    /** Devuelve el final de mayor prioridad cuya regla se cumple. */
    fun resolveEnding(content: GameContent, save: SaveGame): EndingData {
        val values = content.characters.map { save.affinity[it.id] ?: content.config.startAffinity }
        val ordered = content.endings.sortedByDescending { it.priority }
        return ordered.firstOrNull { ending ->
            val rule = ending.rule
            values.count { it >= rule.threshold } >= rule.minCharactersAbove &&
                values.all { it >= rule.noOneBelow }
        } ?: ordered.last()
    }

    fun finish(content: GameContent, save: SaveGame): SaveGame {
        val ending = resolveEnding(content, save)
        return save.copy(phase = GamePhase.ENDING, endingId = ending.id)
    }

    private fun changeAffinity(
        content: GameContent,
        save: SaveGame,
        characterId: String,
        delta: Int,
    ): Pair<SaveGame, Int> {
        val current = save.affinity[characterId] ?: content.config.startAffinity
        val updated = clamp(current + delta, content.config)
        return save.copy(affinity = save.affinity + (characterId to updated)) to (updated - current)
    }
}
