package com.example.videojuego.ui.summary

/** Textos del resumen de la noche. Pendiente: migrar a composeResources/values/strings.xml. */
object SummaryStrings {
    const val Title = "RESUMEN DE LA NOCHE"
    const val Affinity = "AFINIDAD"
    const val Questions = "RONDA DE PREGUNTAS"
    const val Minigame = "MINIJUEGO"
    const val Learning = "LO QUE PRACTICASTE"
    const val Improve = "PARA LA PRÓXIMA"
    const val Continue = "CONTINUAR"
    const val NoAnswer = "Sin respuesta"
    const val Healthy = "SANA"
    const val Risk = "RIESGO"

    fun healthyAnswers(healthy: Int, total: Int) = "Respuestas sanas: $healthy de $total"
    fun averageTime(seconds: Float) = "Tiempo promedio: ${((seconds * 10).toInt() / 10f)} s"
    fun delta(value: Int) = if (value > 0) "+$value" else "$value"
    fun change(before: Int, after: Int) = "$before → $after"
    fun score(score: Int) = "Puntaje: $score / 100"
    fun catches(perfect: Int, good: Int, weak: Int, lost: Int, early: Int) =
        "Perfectas $perfect · Buenas $good · Pequeñas $weak · Perdidas $lost · Antes de tiempo $early"
}
