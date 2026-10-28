package com.example.videojuego.rounds

import kotlinx.serialization.json.Json

/**
 * Resuelve la [ConfigRonda] de cada personaje. Orden de prioridad por parámetro:
 * personaje -> "porDefecto" del JSON -> [ValoresPorDefecto].
 */
class RoundConfigurator(private val dificultad: Dificultad = Dificultad(version = 1)) {

    fun paraPersonaje(personajeId: String): ConfigRonda {
        val propio = dificultad.personajes[personajeId]
        val general = dificultad.porDefecto
        return ConfigRonda(
            personajeId = personajeId,
            tiempoSegundos = valido(propio?.tiempoSegundos)
                ?: valido(general.tiempoSegundos) ?: ValoresPorDefecto.TIEMPO_SEGUNDOS,
            numeroPreguntas = valido(propio?.numeroPreguntas)
                ?: valido(general.numeroPreguntas) ?: ValoresPorDefecto.NUMERO_PREGUNTAS,
            peso = valido(propio?.peso)
                ?: valido(general.peso) ?: ValoresPorDefecto.PESO,
        )
    }

    private fun valido(valor: Int?): Int? = valor?.takeIf { it > 0 }
}

/** Resultado de leer dificultad.json. */
sealed interface CargaDificultad {
    data class Exito(val dificultad: Dificultad) : CargaDificultad
    data class Error(val mensaje: String, val causa: Throwable? = null) : CargaDificultad
}

object DificultadLoader {
    private val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
    }

    /** Nunca lanza. Si devuelve Error, use `RoundConfigurator()` (todo por defecto). */
    fun cargar(texto: String): CargaDificultad = try {
        CargaDificultad.Exito(json.decodeFromString<Dificultad>(texto))
    } catch (e: IllegalArgumentException) {
        CargaDificultad.Error("dificultad.json inválido: ${e.message}", e)
    }
}
