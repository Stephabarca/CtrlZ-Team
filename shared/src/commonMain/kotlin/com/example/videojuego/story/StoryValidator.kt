package com.example.videojuego.story

import com.example.videojuego.story.model.Historia

/**
 * Valida la estructura narrativa de una [Historia] más allá de lo que
 * comprueba la deserialización (EP-6: "si no valida, no se integra").
 *
 * Reglas:
 *  - ids únicos (personajes, escenas, líneas y opciones);
 *  - textos no vacíos y cada escena con al menos una línea;
 *  - una escena no mezcla opciones con [siguienteEscenaId] lineal;
 *  - una escena con opciones tiene al menos 2;
 *  - las afinidades apuntan a personajes existentes;
 *  - todas las escenas son alcanzables desde la inicial;
 *  - existe al menos un final y desde toda escena se puede llegar a uno
 *    (sin ciclos sin salida);
 *  - hay al menos [minRamas] escenas de decisión (con 2+ opciones).
 */
object StoryValidator {

    fun validar(h: Historia, minRamas: Int = 2): List<String> {
        val errores = mutableListOf<String>()

        duplicados("personaje", h.personajes.map { it.id }, errores)
        duplicados("escena", h.escenas.map { it.id }, errores)
        duplicados("línea", h.escenas.flatMap { e -> e.lineas.map { it.id } }, errores)
        duplicados("opción", h.escenas.flatMap { e -> e.opciones.map { it.id } }, errores)

        val personajes = h.personajes.map { it.id }.toSet()
        val porId = h.escenas.associateBy { it.id }

        for (e in h.escenas) {
            if (e.lineas.isEmpty()) errores += "Escena '${e.id}' no tiene líneas"
            for (l in e.lineas) {
                if (l.texto.isBlank()) errores += "Línea '${l.id}' tiene texto vacío"
            }
            if (e.opciones.isNotEmpty() && e.siguienteEscenaId != null) {
                errores += "Escena '${e.id}' tiene opciones y siguienteEscenaId a la vez"
            }
            if (e.opciones.size == 1) {
                errores += "Escena '${e.id}' tiene una sola opción (use siguienteEscenaId)"
            }
            for (o in e.opciones) {
                if (o.texto.isBlank()) errores += "Opción '${o.id}' tiene texto vacío"
                for (p in o.afinidad.keys) {
                    if (p !in personajes) errores += "Opción '${o.id}': afinidad de personaje inexistente '$p'"
                }
            }
        }

        // Si hay referencias rotas, el análisis de grafo no tiene sentido.
        if (errores.isNotEmpty()) return errores
        if (h.escenaInicial !in porId) return listOf("escenaInicial '${h.escenaInicial}' no existe")

        fun destinos(id: String): List<String> {
            val e = porId.getValue(id)
            return e.opciones.map { it.siguienteEscenaId } + listOfNotNull(e.siguienteEscenaId)
        }

        // Alcanzables desde el inicio.
        val alcanzables = mutableSetOf<String>()
        val pendientes = ArrayDeque<String>().apply { add(h.escenaInicial) }
        while (pendientes.isNotEmpty()) {
            val id = pendientes.removeFirst()
            if (id !in porId || !alcanzables.add(id)) continue
            destinos(id).forEach { pendientes.add(it) }
        }
        for (e in h.escenas) {
            if (e.id !in alcanzables) errores += "Escena '${e.id}' es inalcanzable desde la inicial"
        }

        // Finales y escenas sin salida (sin camino a un final).
        val finales = h.escenas.filter { destinos(it.id).isEmpty() }.map { it.id }
        if (finales.isEmpty()) {
            errores += "La historia no tiene ningún final"
        } else {
            val conSalida = finales.toMutableSet()
            var cambio = true
            while (cambio) {
                cambio = false
                for (e in h.escenas) {
                    if (e.id !in conSalida && destinos(e.id).any { it in conSalida }) {
                        conSalida += e.id
                        cambio = true
                    }
                }
            }
            for (e in h.escenas) {
                if (e.id in alcanzables && e.id !in conSalida) {
                    errores += "Escena '${e.id}' no puede llegar a ningún final (ciclo sin salida)"
                }
            }
        }

        val ramas = h.escenas.count { it.opciones.size >= 2 }
        if (ramas < minRamas) errores += "Hay $ramas escenas de decisión; se requieren al menos $minRamas"

        return errores
    }

    private fun duplicados(tipo: String, ids: List<String>, errores: MutableList<String>) {
        ids.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            .forEach { errores += "Id de $tipo duplicado: '$it'" }
    }
}
