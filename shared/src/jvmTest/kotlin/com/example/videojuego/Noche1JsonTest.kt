package com.example.videojuego.story

import com.example.videojuego.story.model.Historia
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Criterios de aceptación de EP-6 sobre el archivo real noche1.json. */
class Noche1JsonTest {

    // Gradle ejecuta los tests con el directorio del módulo (shared) como base.
    private val archivo = File("src/commonMain/composeResources/files/noche1.json")

    private fun cargar(): Historia {
        val resultado = StoryLoader.cargarValidada(archivo.readText(Charsets.UTF_8), minRamas = 2)
        if (resultado is CargaHistoria.Error) error(resultado.mensaje)
        return assertIs<CargaHistoria.Exito>(resultado).historia
    }

    @Test
    fun noche1ValidaContraElEsquemaYLaEstructura() {
        cargar()
    }

    @Test
    fun noche1TieneAlMenosDosRamas() {
        val h = cargar()
        assertTrue(h.escenas.count { it.opciones.size >= 2 } >= 2)
    }

    /** Juega todos los caminos posibles y comprueba que todos terminan en un final. */
    @Test
    fun todosLosCaminosSonJugablesHastaUnFinal() {
        val h = cargar()
        val porId = h.escenas.associateBy { it.id }
        val finalesAlcanzados = mutableSetOf<String>()

        fun jugar(id: String, visitadas: Set<String>) {
            assertTrue(id !in visitadas, "Ciclo en el camino: $id")
            val e = porId.getValue(id)
            val destinos = e.opciones.map { it.siguienteEscenaId } + listOfNotNull(e.siguienteEscenaId)
            if (destinos.isEmpty()) {
                finalesAlcanzados += id
            } else {
                destinos.forEach { jugar(it, visitadas + id) }
            }
        }
        jugar(h.escenaInicial, emptySet())

        assertTrue(finalesAlcanzados.size >= 2, "Se esperaban al menos 2 finales distintos")
    }

    @Test
    fun noHayTextosConEspaciosSobrantes() {
        val h = cargar()
        val textos = h.escenas.flatMap { e -> e.lineas.map { it.texto } + e.opciones.map { it.texto } }
        for (t in textos) {
            assertEquals(t.trim(), t, "Espacios sobrantes en: $t")
            assertTrue("  " !in t, "Doble espacio en: $t")
        }
    }
}
