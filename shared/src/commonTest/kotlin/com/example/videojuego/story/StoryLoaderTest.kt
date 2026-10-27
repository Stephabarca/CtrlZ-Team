package com.example.videojuego.story

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class StoryLoaderTest {

    private val jsonValido = """
        {
          "version": 1,
          "escenaInicial": "intro_01",
          "personajes": [{ "id": "mara", "nombre": "Mara" }],
          "escenas": [
            {
              "id": "intro_01",
              "lineas": [{ "id": "l1", "personajeId": "mara", "texto": "Hola" }],
              "opciones": [
                { "id": "o1", "texto": "Hola", "siguienteEscenaId": "intro_02", "afinidad": { "mara": 5 } }
              ]
            },
            { "id": "intro_02", "lineas": [{ "id": "l2", "texto": "Fin" }] }
          ]
        }
    """.trimIndent()

    @Test
    fun jsonValidoSeDeserializaALosObjetosEsperados() {
        val resultado = StoryLoader.cargar(jsonValido)
        assertIs<CargaHistoria.Exito>(resultado)
        val h = resultado.historia
        assertEquals("intro_01", h.escenaInicial)
        assertEquals("Mara", h.personajes.single().nombre)
        assertEquals(2, h.escenas.size)
        assertEquals(5, h.escenas[0].opciones.single().afinidad["mara"])
        assertNull(h.escenas[0].siguienteEscenaId) // valor por defecto
    }

    @Test
    fun jsonMalFormadoDevuelveErrorSinLanzarExcepcion() {
        assertIs<CargaHistoria.Error>(StoryLoader.cargar("{ esto no es json"))
    }

    @Test
    fun faltanCamposObligatoriosDevuelveError() {
        val sinTexto = """
            {"version":1,"escenaInicial":"a","personajes":[],
             "escenas":[{"id":"a","lineas":[{"id":"l1"}]}]}
        """.trimIndent()
        assertIs<CargaHistoria.Error>(StoryLoader.cargar(sinTexto))
    }

    @Test
    fun escenaInicialInexistenteDevuelveError() {
        val json = jsonValido.replace("\"escenaInicial\": \"intro_01\"", "\"escenaInicial\": \"nada\"")
        assertIs<CargaHistoria.Error>(StoryLoader.cargar(json))
    }

    @Test
    fun opcionConEscenaInexistenteDevuelveError() {
        val json = jsonValido.replace("\"siguienteEscenaId\": \"intro_02\"", "\"siguienteEscenaId\": \"fantasma\"")
        assertIs<CargaHistoria.Error>(StoryLoader.cargar(json))
    }

    @Test
    fun cadenaVaciaDevuelveError() {
        assertIs<CargaHistoria.Error>(StoryLoader.cargar(""))
    }
}
