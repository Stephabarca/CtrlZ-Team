package com.example.videojuego.story.model

import kotlinx.serialization.Serializable

/** Superviviente de la isla. [sprite] es el nombre del recurso gráfico (EP-8). */
@Serializable
data class Personaje(
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val sprite: String? = null,
)

/** Una línea de diálogo. Si [personajeId] es null, habla el narrador. */
@Serializable
data class LineaDialogo(
    val id: String,
    val personajeId: String? = null,
    val texto: String,
)

/**
 * Opción que el jugador puede elegir al final de una escena.
 * [afinidad] mapea personajeId -> cambio de afinidad (se usará en EP-4).
 */
@Serializable
data class Opcion(
    val id: String,
    val texto: String,
    val siguienteEscenaId: String,
    val afinidad: Map<String, Int> = emptyMap(),
)

/** Escena: fondo, líneas de diálogo y, o bien opciones, o bien [siguienteEscenaId] lineal. */
@Serializable
data class Escena(
    val id: String,
    val fondo: String? = null,
    val lineas: List<LineaDialogo>,
    val opciones: List<Opcion> = emptyList(),
    val siguienteEscenaId: String? = null,
)

/** Contenedor raíz del archivo JSON de la historia. */
@Serializable
data class Historia(
    val version: Int,
    val escenaInicial: String,
    val personajes: List<Personaje>,
    val escenas: List<Escena>,
)
