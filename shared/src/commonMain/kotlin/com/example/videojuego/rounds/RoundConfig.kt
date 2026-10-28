package com.example.videojuego.rounds

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable

/**
 * Valores por defecto documentados (EP-3). Se usan cuando falta un parámetro
 * (o es inválido, es decir, menor o igual a cero) tanto en el personaje como en "porDefecto".
 *
 *  - tiempo por pregunta: 20 segundos
 *  - número de preguntas por noche: 5
 *  - peso de la noche (multiplica el efecto de cada respuesta): 1
 */
object ValoresPorDefecto {
    const val TIEMPO_SEGUNDOS = 20
    const val NUMERO_PREGUNTAS = 5
    const val PESO = 1
}

/** Parámetros tal como vienen del JSON. Todo es opcional: lo que falta se rellena con un valor por defecto. */
@Serializable
data class ParametrosRonda(
    val tiempoSegundos: Int? = null,
    val numeroPreguntas: Int? = null,
    val peso: Int? = null,
)

/** Contenido de dificultad.json. */
@Serializable
data class Dificultad(
    val version: Int,
    val porDefecto: ParametrosRonda = ParametrosRonda(),
    val personajes: Map<String, ParametrosRonda> = emptyMap(),
)

/** Configuración final y completa de una ronda para un superviviente. */
data class ConfigRonda(
    val personajeId: String,
    val tiempoSegundos: Int,
    val numeroPreguntas: Int,
    val peso: Int,
) {
    /** Tiempo límite por pregunta, listo para el temporizador. */
    val tiempoLimite: Duration get() = tiempoSegundos.seconds

    /** Cuántas preguntas se juegan realmente si el banco solo tiene [disponibles]. */
    fun preguntasAJugar(disponibles: Int): Int = minOf(numeroPreguntas, disponibles.coerceAtLeast(0))

    /** Multiplica el efecto de una respuesta por el peso de la noche. */
    fun aplicarPeso(efecto: Map<String, Int>): Map<String, Int> = efecto.mapValues { it.value * peso }
}
