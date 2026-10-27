package com.example.videojuego.story

import com.example.videojuego.story.model.Escena
import com.example.videojuego.story.model.Historia
import com.example.videojuego.story.model.LineaDialogo
import com.example.videojuego.story.model.Opcion
import com.example.videojuego.story.model.Personaje
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoryValidatorTest {

    private fun linea(id: String) = LineaDialogo(id = id, personajeId = "mara", texto = "Hola")

    private fun escena(
        id: String,
        opciones: List<Opcion> = emptyList(),
        siguiente: String? = null,
    ) = Escena(id = id, lineas = listOf(linea("l_$id")), opciones = opciones, siguienteEscenaId = siguiente)

    private fun opcion(id: String, destino: String) = Opcion(id = id, texto = "Texto $id", siguienteEscenaId = destino)

    private fun historia(vararg escenas: Escena, inicial: String = "a") = Historia(
        version = 1,
        escenaInicial = inicial,
        personajes = listOf(Personaje(id = "mara", nombre = "Mara")),
        escenas = escenas.toList(),
    )

    /** a -> (b | c) -> fin; una sola decisión. */
    private fun valida() = historia(
        escena("a", opciones = listOf(opcion("o1", "b"), opcion("o2", "c"))),
        escena("b", siguiente = "fin"),
        escena("c", siguiente = "fin"),
        escena("fin"),
    )

    @Test
    fun historiaValidaNoTieneErrores() {
        assertEquals(emptyList(), StoryValidator.validar(valida(), minRamas = 1))
    }

    @Test
    fun exigeMinimoDeRamas() {
        val errores = StoryValidator.validar(valida(), minRamas = 2)
        assertTrue(errores.any { "decisión" in it })
    }

    @Test
    fun detectaEscenaInalcanzable() {
        val h = historia(
            escena("a", siguiente = "fin"),
            escena("fin"),
            escena("huerfana", siguiente = "fin"),
        )
        assertTrue(StoryValidator.validar(h, 0).any { "inalcanzable" in it })
    }

    @Test
    fun detectaCicloSinSalida() {
        val h = historia(
            escena("a", opciones = listOf(opcion("o1", "b"), opcion("o2", "fin"))),
            escena("b", siguiente = "c"),
            escena("c", siguiente = "b"),
            escena("fin"),
        )
        assertTrue(StoryValidator.validar(h, 0).any { "ciclo sin salida" in it })
    }

    @Test
    fun detectaIdsDuplicados() {
        val h = historia(escena("a", siguiente = "a2"), escena("a2"), escena("a2"))
        assertTrue(StoryValidator.validar(h, 0).any { "duplicado" in it })
    }

    @Test
    fun detectaOpcionesYSiguienteJuntos() {
        val h = historia(
            escena("a", opciones = listOf(opcion("o1", "fin"), opcion("o2", "fin")), siguiente = "fin"),
            escena("fin"),
        )
        assertTrue(StoryValidator.validar(h, 0).any { "a la vez" in it })
    }

    @Test
    fun detectaAfinidadDePersonajeInexistente() {
        val h = historia(
            escena(
                "a",
                opciones = listOf(
                    Opcion("o1", "Uno", "fin", mapOf("fantasma" to 1)),
                    opcion("o2", "fin"),
                ),
            ),
            escena("fin"),
        )
        assertTrue(StoryValidator.validar(h, 0).any { "inexistente" in it })
    }

    @Test
    fun historiaSinFinalEsInvalida() {
        val h = historia(escena("a", siguiente = "b"), escena("b", siguiente = "a"))
        assertTrue(StoryValidator.validar(h, 0).any { "ningún final" in it })
    }

    @Test
    fun cargarValidadaRechazaJsonInvalido() {
        assertTrue(StoryLoader.cargarValidada("{ esto no es json }") is CargaHistoria.Error)
    }
}
