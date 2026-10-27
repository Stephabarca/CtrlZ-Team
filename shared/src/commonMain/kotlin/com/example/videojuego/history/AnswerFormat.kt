package com.example.videojuego.history

/** Milisegundos a segundos con un decimal, por ejemplo 3200 -> "3.2". */
fun formatSeconds(millis: Long): String {
    val tenths = millis / 100
    return "${tenths / 10}.${tenths % 10}"
}

/** Efecto legible, por ejemplo {mara: 5, leo: -3} -> "mara +5, leo -3". Vacío -> "—". */
fun formatEffect(effect: Map<String, Int>): String =
    if (effect.isEmpty()) {
        "—"
    } else {
        effect.entries.joinToString(", ") { (character, delta) ->
            "$character ${if (delta > 0) "+" else ""}$delta"
        }
    }
