package com.example.videojuego.ui

import androidx.lifecycle.ViewModel
import com.example.videojuego.history.AnswerHistory
import com.example.videojuego.story.model.Opcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Pregunta de prueba. TEMPORAL: la pantalla real de preguntas llega con IP-11. */
data class DemoQuestion(val id: String, val text: String, val options: List<Opcion>)

private const val DEMO_ROUND_ID = "demo_noche"

/**
 * TEMPORAL. Juega una ronda de prueba para verificar que el historial registra
 * respuestas y "sin respuesta". No usa el temporizador real: el tiempo empleado se
 * mide con un reloj simple y el "tiempo agotado" se simula con un botón.
 */
class AnswerRoundDemoViewModel(
    val questions: List<DemoQuestion>,
    private val timeLimit: Duration,
) : ViewModel() {

    val history = AnswerHistory()

    private val _index = MutableStateFlow(0)

    /** Índice de la pregunta actual. Igual a questions.size cuando la ronda terminó. */
    val index: StateFlow<Int> = _index.asStateFlow()

    private var startMark: TimeMark = TimeSource.Monotonic.markNow()

    private val currentQuestion: DemoQuestion? get() = questions.getOrNull(_index.value)

    fun answer(option: Opcion) {
        val question = currentQuestion ?: return
        val spent = startMark.elapsedNow().coerceAtMost(timeLimit)
        history.recordAnswer(DEMO_ROUND_ID, question.id, option, spent)
        next()
    }

    /** Simula que se agotó el tiempo (con el temporizador real lo dispara su evento). */
    fun simulateTimeout() {
        val question = currentQuestion ?: return
        history.recordTimeout(DEMO_ROUND_ID, question.id, timeLimit)
        next()
    }

    fun restart() {
        history.clear()
        _index.value = 0
        startMark = TimeSource.Monotonic.markNow()
    }

    private fun next() {
        _index.value += 1
        startMark = TimeSource.Monotonic.markNow()
    }
}
