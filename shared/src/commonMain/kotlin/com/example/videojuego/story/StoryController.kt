package com.example.videojuego.story

import com.example.videojuego.history.AnswerHistory
import com.example.videojuego.story.model.Escena
import com.example.videojuego.story.model.Historia
import com.example.videojuego.story.model.LineaDialogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Estado visible de la historia: escena actual, línea mostrada y afinidad acumulada. */
data class StoryState(
    val escena: Escena,
    val indiceLinea: Int = 0,
    /** personajeId -> afinidad acumulada con las opciones elegidas. */
    val afinidad: Map<String, Int> = emptyMap(),
) {
    val linea: LineaDialogo get() = escena.lineas[indiceLinea]
    val esUltimaLinea: Boolean get() = indiceLinea >= escena.lineas.lastIndex

    /** Se muestran las opciones cuando se llegó a la última línea y la escena las tiene. */
    val mostrarOpciones: Boolean get() = esUltimaLinea && escena.opciones.isNotEmpty()

    /** La historia terminó: última línea de una escena sin opciones ni siguiente. */
    val finalizada: Boolean
        get() = esUltimaLinea && escena.opciones.isEmpty() && escena.siguienteEscenaId == null
}

/**
 * Recorre una [Historia] ya validada. Lógica pura (sin UI) para poder probarla en commonTest.
 * Si recibe un [historial], registra cada opción elegida con el tiempo que tardó el jugador.
 */
class StoryController(
    private val historia: Historia,
    private val historial: AnswerHistory? = null,
    private val rondaId: String = "noche1",
) {
    private val escenas = historia.escenas.associateBy { it.id }
    private val _state = MutableStateFlow(StoryState(escenas.getValue(historia.escenaInicial)))
    val state: StateFlow<StoryState> = _state.asStateFlow()

    private var marca: TimeMark = TimeSource.Monotonic.markNow()

    /** Siguiente línea o, al terminar la escena, la escena lineal siguiente. No hace nada si hay opciones o terminó. */
    fun avanzar() {
        val s = _state.value
        if (s.mostrarOpciones || s.finalizada) return
        val siguiente = s.escena.siguienteEscenaId
        when {
            !s.esUltimaLinea -> actualizar(s.copy(indiceLinea = s.indiceLinea + 1))
            siguiente != null -> actualizar(StoryState(escenas.getValue(siguiente), 0, s.afinidad))
        }
    }

    /** Elige una opción de la escena actual (solo si se están mostrando las opciones). */
    fun elegir(opcionId: String) {
        val s = _state.value
        if (!s.mostrarOpciones) return
        val opcion = s.escena.opciones.firstOrNull { it.id == opcionId } ?: return
        historial?.recordAnswer(rondaId, s.escena.id, opcion, marca.elapsedNow())
        val afinidad = s.afinidad.toMutableMap()
        opcion.afinidad.forEach { (personaje, delta) -> afinidad[personaje] = (afinidad[personaje] ?: 0) + delta }
        actualizar(StoryState(escenas.getValue(opcion.siguienteEscenaId), 0, afinidad))
    }

    private fun actualizar(nuevo: StoryState) {
        _state.value = nuevo
        if (nuevo.mostrarOpciones) marca = TimeSource.Monotonic.markNow()
    }
}
