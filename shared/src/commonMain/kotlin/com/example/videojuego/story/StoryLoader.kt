package com.example.videojuego.story

import com.example.videojuego.story.model.Historia
import kotlinx.serialization.json.Json

/** Resultado de cargar la historia: éxito o error controlado. */
sealed interface CargaHistoria {
    data class Exito(val historia: Historia) : CargaHistoria
    data class Error(val mensaje: String, val causa: Throwable? = null) : CargaHistoria
}

object StoryLoader {
    private val json = Json {
        ignoreUnknownKeys = false // un campo mal escrito debe notarse
        isLenient = false
    }

    /** Nunca lanza: devuelve [CargaHistoria.Error] si el JSON es inválido o inconsistente. */
    fun cargar(texto: String): CargaHistoria = try {
        val historia = json.decodeFromString<Historia>(texto)
        val problema = validar(historia)
        if (problema != null) CargaHistoria.Error(problema) else CargaHistoria.Exito(historia)
    } catch (e: IllegalArgumentException) {
        // SerializationException hereda de IllegalArgumentException
        CargaHistoria.Error("JSON inválido o con campos faltantes: ${e.message}", e)
    }

    /**
     * Igual que [cargar], pero además aplica [StoryValidator] (EP-6).
     * Si hay algún problema narrativo, la historia NO se integra.
     */
    fun cargarValidada(texto: String, minRamas: Int = 2): CargaHistoria =
        when (val r = cargar(texto)) {
            is CargaHistoria.Error -> r
            is CargaHistoria.Exito -> {
                val errores = StoryValidator.validar(r.historia, minRamas)
                if (errores.isEmpty()) r
                else CargaHistoria.Error("La historia no valida: " + errores.joinToString("; "))
            }
        }

    private fun validar(h: Historia): String? {
        val escenas = h.escenas.map { it.id }.toSet()
        val personajes = h.personajes.map { it.id }.toSet()

        if (h.escenaInicial !in escenas) return "escenaInicial '${h.escenaInicial}' no existe"
        for (e in h.escenas) {
            e.siguienteEscenaId?.let {
                if (it !in escenas) return "Escena '${e.id}': siguiente '$it' no existe"
            }
            for (l in e.lineas) {
                l.personajeId?.let {
                    if (it !in personajes) return "Línea '${l.id}': personaje '$it' no existe"
                }
            }
            for (o in e.opciones) {
                if (o.siguienteEscenaId !in escenas) {
                    return "Opción '${o.id}': escena '${o.siguienteEscenaId}' no existe"
                }
            }
        }
        return null
    }
}
