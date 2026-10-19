package com.example.videojuego.narrative

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IntroState(
    val index: Int,
    val line: DialogueLine,
    val skippedTutorial: Boolean = false,
    val finished: Boolean = false,
    val metSurvivors: Set<Speaker> = emptySet(),
) {
    fun toResult() = IntroResult(skippedTutorial, metSurvivors)
}

/**
 * Lógica pura de la Fase 1 (sin UI), para poder probarla en commonTest.
 * Cuando exista el motor de diálogo completo (IP-7/IP-8), esta clase se reemplaza por él.
 */
class IntroController(private val lines: List<DialogueLine> = IntroScript.lines) {

    init {
        require(lines.isNotEmpty()) { "La intro necesita al menos una línea" }
    }

    private val _state = MutableStateFlow(stateAt(0))
    val state: StateFlow<IntroState> = _state.asStateFlow()

    /** Avanza a la siguiente línea. Tras la última, marca la intro como terminada. */
    fun next() {
        val current = _state.value
        if (current.finished) return
        val nextIndex = current.index + 1
        _state.value = if (nextIndex >= lines.size) {
            current.copy(finished = true)
        } else {
            stateAt(nextIndex, current.skippedTutorial)
        }
    }

    /** Salta el resto del tutorial. No hace nada si la línea actual no es de tutorial. */
    fun skipTutorial() {
        val current = _state.value
        if (current.finished || !current.line.isTutorial) return
        var i = current.index
        while (i < lines.size && lines[i].isTutorial) i++
        _state.value = if (i >= lines.size) {
            current.copy(skippedTutorial = true, finished = true)
        } else {
            stateAt(i, skipped = true)
        }
    }

    private fun stateAt(index: Int, skipped: Boolean = false) = IntroState(
        index = index,
        line = lines[index],
        skippedTutorial = skipped,
        metSurvivors = lines.take(index + 1).map { it.speaker }.filter { it.isSurvivor }.toSet(),
    )
}
