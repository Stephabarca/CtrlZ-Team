package com.example.videojuego.round

import com.example.videojuego.data.content.AnswerData
import com.example.videojuego.data.content.CharacterData
import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.content.NightData
import com.example.videojuego.data.content.QuestionData
import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.data.save.SaveGame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Qué pasó con la última respuesta; se muestra antes de pasar a la siguiente pregunta. */
data class AnswerFeedback(
    val chosenAnswerId: String?,
    val timedOut: Boolean,
    val appliedDelta: Int,
    val message: String,
    val motiveLabel: String?,
    val healthy: Boolean?,
)

data class RoundState(
    val questionIndex: Int,
    val totalQuestions: Int,
    val question: QuestionData,
    val save: SaveGame,
    val affinity: Int,
    val feedback: AnswerFeedback? = null,
    val finished: Boolean = false,
)

/** Resultado de la ronda: el progreso ya con todas las respuestas aplicadas. */
data class RoundResult(val save: SaveGame)

/**
 * Lógica pura de la ronda de preguntas (IP-11), sin UI ni temporizador.
 * El temporizador (IP-10) solo avisa con [timeExpired]; así se puede cambiar sin tocar esta clase.
 *
 * Flujo de cada pregunta: [answer] o [timeExpired] -> se aplica el efecto y se muestra la
 * retroalimentación -> [next] pasa a la siguiente. Solo se procesa UN evento por pregunta.
 *
 * Si [save] ya trae respuestas de esta noche, la ronda continúa desde la primera pregunta sin responder.
 * Así se retoma al rotar la pantalla o al continuar una partida guardada.
 */
class QuestionRoundController(
    private val content: GameContent,
    val night: NightData,
    save: SaveGame,
    private val timeoutMessage: String = DEFAULT_TIMEOUT_MESSAGE,
) {
    init {
        require(night.questions.isNotEmpty()) { "La noche ${night.id} no tiene preguntas" }
    }

    private val _state = MutableStateFlow(initialState(save))
    val state: StateFlow<RoundState> = _state.asStateFlow()

    val character: CharacterData get() = content.character(night.characterId)

    fun stageName(affinity: Int): String = ProgressRules.stageFor(affinity, content.config).name

    fun affinityFraction(affinity: Int): Float {
        val range = content.config.maxAffinity - content.config.minAffinity
        return if (range <= 0) 0f else (affinity - content.config.minAffinity).toFloat() / range
    }

    /** El jugador eligió una respuesta. Se ignora si ya se resolvió esta pregunta o el ID no existe. */
    fun answer(answerId: String, timeSpentMs: Long) {
        val current = _state.value
        if (current.finished || current.feedback != null) return
        val answer = current.question.answers.firstOrNull { it.id == answerId } ?: return
        resolve(current, answer, timeSpentMs)
    }

    /** Se agotó el tiempo. Se ignora si el jugador ya respondió (evita el doble efecto). */
    fun timeExpired(timeSpentMs: Long) {
        val current = _state.value
        if (current.finished || current.feedback != null) return
        resolve(current, null, timeSpentMs)
    }

    /** Pasa a la siguiente pregunta. No hace nada si la actual aún no se resolvió. */
    fun next() {
        val current = _state.value
        if (current.finished || current.feedback == null) return
        val nextIndex = current.questionIndex + 1
        _state.value = if (nextIndex >= night.questions.size) {
            current.copy(finished = true)
        } else {
            stateAt(nextIndex, current.save)
        }
    }

    private fun resolve(current: RoundState, answer: AnswerData?, timeSpentMs: Long) {
        val updated = ProgressRules.recordAnswer(content, current.save, night, current.question, answer, timeSpentMs)
        val motive = answer?.let { content.motive(it.motive) }
        _state.value = current.copy(
            save = updated,
            affinity = updated.affinity[night.characterId] ?: current.affinity,
            feedback = AnswerFeedback(
                chosenAnswerId = answer?.id,
                timedOut = answer == null,
                appliedDelta = updated.answers.last().affinityDelta,
                message = answer?.feedback ?: timeoutMessage,
                motiveLabel = motive?.label,
                healthy = motive?.healthy,
            ),
        )
    }

    private fun initialState(save: SaveGame): RoundState {
        val firstUnanswered = night.questions.indexOfFirst { question ->
            save.answers.none { it.questionId == question.id }
        }
        return if (firstUnanswered == -1) {
            stateAt(night.questions.lastIndex, save).copy(finished = true)
        } else {
            stateAt(firstUnanswered, save)
        }
    }

    private fun stateAt(index: Int, save: SaveGame) = RoundState(
        questionIndex = index,
        totalQuestions = night.questions.size,
        question = night.questions[index],
        save = save,
        affinity = save.affinity[night.characterId] ?: content.config.startAffinity,
    )

    companion object {
        const val DEFAULT_TIMEOUT_MESSAGE = "Se acabó el tiempo. El silencio también cuenta."
    }
}
