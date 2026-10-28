package com.example.videojuego.rounds

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class RoundConfiguratorTest {

    private val dificultad = Dificultad(
        version = 1,
        porDefecto = ParametrosRonda(tiempoSegundos = 30, numeroPreguntas = 6, peso = 2),
        personajes = mapOf(
            "a" to ParametrosRonda(tiempoSegundos = 10, numeroPreguntas = 3, peso = 5),
            "b" to ParametrosRonda(tiempoSegundos = 40),
            "malo" to ParametrosRonda(tiempoSegundos = 0, numeroPreguntas = -1, peso = 0),
        ),
    )
    private val configurador = RoundConfigurator(dificultad)

    @Test
    fun personajesDistintosDanConfiguracionesDistintas() {
        val a = configurador.paraPersonaje("a")
        val b = configurador.paraPersonaje("b")
        assertNotEquals(a.tiempoSegundos, b.tiempoSegundos)
        assertNotEquals(a.numeroPreguntas, b.numeroPreguntas)
        assertEquals(10, a.tiempoSegundos)
        assertEquals(3, a.numeroPreguntas)
        assertEquals(5, a.peso)
    }

    @Test
    fun parametroFaltanteUsaElPorDefectoDelJson() {
        val b = configurador.paraPersonaje("b")
        assertEquals(40, b.tiempoSegundos)
        assertEquals(6, b.numeroPreguntas)
        assertEquals(2, b.peso)
    }

    @Test
    fun personajeDesconocidoUsaElPorDefectoDelJson() {
        val x = configurador.paraPersonaje("nadie")
        assertEquals(30, x.tiempoSegundos)
        assertEquals(6, x.numeroPreguntas)
    }

    @Test
    fun valoresInvalidosSeTratanComoFaltantes() {
        val malo = configurador.paraPersonaje("malo")
        assertEquals(30, malo.tiempoSegundos)
        assertEquals(6, malo.numeroPreguntas)
        assertEquals(2, malo.peso)
    }

    @Test
    fun sinNingunDatoSeUsanLosValoresPorDefectoDocumentados() {
        val c = RoundConfigurator().paraPersonaje("x")
        assertEquals(ValoresPorDefecto.TIEMPO_SEGUNDOS, c.tiempoSegundos)
        assertEquals(ValoresPorDefecto.NUMERO_PREGUNTAS, c.numeroPreguntas)
        assertEquals(ValoresPorDefecto.PESO, c.peso)
    }

    @Test
    fun preguntasAJugarNoExcedeLasDisponibles() {
        val c = configurador.paraPersonaje("a") // 3 preguntas
        assertEquals(3, c.preguntasAJugar(10))
        assertEquals(2, c.preguntasAJugar(2))
        assertEquals(0, c.preguntasAJugar(-1))
    }

    @Test
    fun aplicarPesoMultiplicaElEfecto() {
        val c = configurador.paraPersonaje("a") // peso 5
        assertEquals(mapOf("afinidad" to 10, "confianza" to -5), c.aplicarPeso(mapOf("afinidad" to 2, "confianza" to -1)))
    }

    @Test
    fun jsonInvalidoDevuelveErrorSinLanzar() {
        assertIs<CargaDificultad.Error>(DificultadLoader.cargar("{ no es json }"))
        assertIs<CargaDificultad.Error>(DificultadLoader.cargar("""{"version":1,"campoRaro":3}"""))
    }

    @Test
    fun jsonMinimoEsValido() {
        assertIs<CargaDificultad.Exito>(DificultadLoader.cargar("""{"version":1}"""))
    }
}
