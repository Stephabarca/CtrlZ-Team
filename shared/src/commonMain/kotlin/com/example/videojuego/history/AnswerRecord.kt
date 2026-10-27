package com.example.videojuego.history

import kotlinx.serialization.Serializable

/** Resultado de una pregunta: respondida, o tiempo agotado ("sin respuesta"). */
enum class AnswerStatus { Answered, TimedOut }

/**
 * Registro de una respuesta. Guarda los 4 datos que pide la historia:
 * pregunta ([questionId]), opción elegida ([optionId]), tiempo empleado
 * ([timeSpentMillis]) y efecto ([effect], cambio de afinidad por personaje).
 *
 * Si el tiempo se agotó, [status] es [AnswerStatus.TimedOut], [optionId] es null
 * y [effect] está vacío ("sin respuesta").
 *
 * [roundId] identifica la ronda (por ejemplo "noche1") para el resumen por noche.
 * Es serializable para poder guardarlo o exportarlo más adelante (EP-9, playtest).
 */
@Serializable
data class AnswerRecord(
    val roundId: String,
    val questionId: String,
    val optionId: String?,
    val timeSpentMillis: Long,
    val effect: Map<String, Int>,
    val status: AnswerStatus,
) {
    init {
        require(timeSpentMillis >= 0) { "timeSpentMillis no puede ser negativo" }
        when (status) {
            AnswerStatus.Answered ->
                require(optionId != null) { "Una respuesta necesita optionId" }
            AnswerStatus.TimedOut -> {
                require(optionId == null) { "Sin respuesta no puede tener optionId" }
                require(effect.isEmpty()) { "Sin respuesta no puede tener efecto" }
            }
        }
    }

    /** True si el jugador no alcanzó a responder. */
    val isNoAnswer: Boolean get() = status == AnswerStatus.TimedOut

    companion object {
        fun answered(
            roundId: String,
            questionId: String,
            optionId: String,
            timeSpentMillis: Long,
            effect: Map<String, Int>,
        ) = AnswerRecord(roundId, questionId, optionId, timeSpentMillis, effect, AnswerStatus.Answered)

        fun timedOut(
            roundId: String,
            questionId: String,
            timeLimitMillis: Long,
        ) = AnswerRecord(roundId, questionId, null, timeLimitMillis, emptyMap(), AnswerStatus.TimedOut)
    }
}
