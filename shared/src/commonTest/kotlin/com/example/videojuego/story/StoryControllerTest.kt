package com.example.videojuego.story

import com.example.videojuego.history.AnswerHistory
import com.example.videojuego.story.model.Escena
import com.example.videojuego.story.model.Historia
import com.example.videojuego.story.model.LineaDialogo
import com.example.videojuego.story.model.Opcion
import com.example.videojuego.story.model.Personaje
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryControllerTest {

    private fun linea(id: String) = LineaDialogo(id, "v", "Texto $id")

    private val historia = Historia(
        version = 1,
        escenaInicial = "a",
        personajes = listOf(Personaje("v", "Valentina")),
        escenas = listOf(
            Escena("a", lineas = listOf(linea("a1"), linea("a2")), siguienteEscenaId = "b"),
            Escena(
                "b",
                lineas = listOf(linea("b1")),
                opciones = listOf(
                    Opcion("o1", "Uno", "fin1", mapOf("v" to 3)),
                    Opcion("o2", "Dos", "fin2", mapOf("v" to -1)),
                ),
            ),
            Escena("fin1", lineas = listOf(linea("f1"))),
            Escena("fin2", lineas = listOf(linea("f2"))),
        ),
    )

    @Test
    fun avanzaLineasYEscenasLineales() {
        val c = StoryController(historia)
        assertEquals("a1", c.state.value.linea.id)
        c.avanzar()
        assertEquals("a2", c.state.value.linea.id)
        c.avanzar()
        assertEquals("b", c.state.value.escena.id)
        assertTrue(c.state.value.mostrarOpciones)
    }

    @Test
    fun conOpcionesAvanzarNoHaceNada() {
        val c = StoryController(historia)
        repeat(2) { c.avanzar() }
        c.avanzar()
        assertEquals("b", c.state.value.escena.id)
    }

    @Test
    fun elegirOpcionCambiaDeRamaYSumaAfinidad() {
        val c = StoryController(historia)
        repeat(2) { c.avanzar() }
        c.elegir("o1")
        assertEquals("fin1", c.state.value.escena.id)
        assertEquals(3, c.state.value.afinidad["v"])
        assertTrue(c.state.value.finalizada)
    }

    @Test
    fun laOtraRamaLlegaAOtroFinal() {
        val c = StoryController(historia)
        repeat(2) { c.avanzar() }
        c.elegir("o2")
        assertEquals("fin2", c.state.value.escena.id)
        assertEquals(-1, c.state.value.afinidad["v"])
    }

    @Test
    fun elegirAntesDeTiempoOUnaOpcionInexistenteSeIgnora() {
        val c = StoryController(historia)
        c.elegir("o1") // aún no se muestran opciones
        assertEquals("a", c.state.value.escena.id)
        repeat(2) { c.avanzar() }
        c.elegir("no_existe")
        assertEquals("b", c.state.value.escena.id)
    }

    @Test
    fun registraLaRespuestaEnElHistorial() {
        val historial = AnswerHistory()
        val c = StoryController(historia, historial, rondaId = "noche1")
        repeat(2) { c.avanzar() }
        c.elegir("o1")
        val registros = historial.records.value
        assertEquals(1, registros.size)
        assertEquals("b", registros.single().questionId)
        assertEquals("o1", registros.single().optionId)
        assertFalse(c.state.value.mostrarOpciones)
    }
}
