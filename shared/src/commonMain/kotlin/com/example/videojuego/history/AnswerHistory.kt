package com.example.videojuego.history

import com.example.videojuego.story.model.Opcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

/**
 * Historial de respuestas de la partida, consultable y observable desde la UI.
 *
 * No depende del temporizador: recibe el tiempo empleado como [Duration], así que
 * funciona con cualquier implementación de cuenta regresiva.
 *
 * Regla: cada pregunta de una ronda se registra una sola vez. Un segundo intento
 * (por ejemplo, tocar una opción justo cuando se agota el tiempo) se ignora.
 */
class AnswerHistory {
    private val _records = MutableStateFlow<List<AnswerRecord>>(emptyList())

    /** Respuestas en el orden en que se registraron. */
    val records: StateFlow<List<AnswerRecord>> = _records.asStateFlow()

    /**
     * Registra la opción elegida. El efecto se toma de [Opcion.afinidad].
     * Devuelve el registro creado, o null si esa pregunta ya estaba registrada.
     */
    fun recordAnswer(
        roundId: String,
        questionId: String,
        option: Opcion,
        timeSpent: Duration,
    ): AnswerRecord? = add(
        AnswerRecord.answered(
            roundId = roundId,
            questionId = questionId,
            optionId = option.id,
            timeSpentMillis = timeSpent.inWholeMilliseconds,
            effect = option.afinidad,
        ),
    )

    /** Registra "sin respuesta" por tiempo agotado. Null si ya estaba registrada. */
    fun recordTimeout(
        roundId: String,
        questionId: String,
        timeLimit: Duration,
    ): AnswerRecord? = add(
        AnswerRecord.timedOut(roundId, questionId, timeLimit.inWholeMilliseconds),
    )

    /** Todas las respuestas registradas. */
    fun all(): List<AnswerRecord> = _records.value

    /** Respuestas de una ronda. */
    fun forRound(roundId: String): List<AnswerRecord> =
        _records.value.filter { it.roundId == roundId }

    /** Cuántas respuestas tienen ese estado. */
    fun count(status: AnswerStatus): Int = _records.value.count { it.status == status }

    /** Suma de efectos por personaje, de toda la partida o de una ronda. */
    fun totalEffect(roundId: String? = null): Map<String, Int> {
        val source = if (roundId == null) _records.value else forRound(roundId)
        val totals = mutableMapOf<String, Int>()
        for (record in source) {
            for ((character, delta) in record.effect) {
                totals[character] = (totals[character] ?: 0) + delta
            }
        }
        return totals
    }

    /** Borra el historial (partida nueva). */
    fun clear() {
        _records.value = emptyList()
    }

    private fun add(record: AnswerRecord): AnswerRecord? {
        var added = false
        _records.update { current ->
            val duplicated = current.any {
                it.roundId == record.roundId && it.questionId == record.questionId
            }
            added = !duplicated
            if (duplicated) current else current + record
        }
        return if (added) record else null
    }
}
