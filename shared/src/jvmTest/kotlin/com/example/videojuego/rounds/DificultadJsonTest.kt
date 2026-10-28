package com.example.videojuego.rounds

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Criterio de aceptación EP-3: cada personaje se siente distinto sin tocar código. */
class DificultadJsonTest {

    private val archivo = File("src/commonMain/composeResources/files/dificultad.json")

    private fun configurador(): RoundConfigurator {
        val r = DificultadLoader.cargar(archivo.readText(Charsets.UTF_8))
        return RoundConfigurator(assertIs<CargaDificultad.Exito>(r).dificultad)
    }

    @Test
    fun cadaSuperviviente_tieneConfiguracionPropia() {
        val c = configurador()
        val rondas = listOf("valentina", "lucas", "lila").map { c.paraPersonaje(it) }
        assertEquals(3, rondas.map { it.tiempoSegundos }.toSet().size, "Tiempos distintos")
        assertEquals(3, rondas.map { it.numeroPreguntas }.toSet().size, "Número de preguntas distinto")
        assertEquals(3, rondas.map { it.peso }.toSet().size, "Pesos distintos")
    }

    @Test
    fun laDificultadSubeConElPersonaje() {
        val c = configurador()
        val v = c.paraPersonaje("valentina")
        val l = c.paraPersonaje("lila")
        assertTrue(v.tiempoSegundos > l.tiempoSegundos)
        assertTrue(v.peso < l.peso)
    }
}
