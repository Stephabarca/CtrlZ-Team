package com.example.videojuego.ui.round

/** Textos de la ronda de preguntas. Pendiente: migrar a composeResources/values/strings.xml. */
object RoundStrings {
    const val Loading = "CARGANDO..."
    const val Back = "VOLVER"
    const val ErrorTitle = "ALGO SALIÓ MAL"
    const val NoAnswer = "Sin respuesta"
    const val Affinity = "AFINIDAD"

    fun counter(current: Int, total: Int) = "PREGUNTA $current/$total"
}
