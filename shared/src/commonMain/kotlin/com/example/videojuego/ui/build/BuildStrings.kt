package com.example.videojuego.ui.build

import com.example.videojuego.minigame.build.StepResult

/** Textos de "Armar la fogata". Pendiente: migrar a composeResources/values/strings.xml. */
object BuildStrings {
    const val Title = "ARMAR LA FOGATA"
    const val Start = "EMPEZAR"
    const val Time = "TIEMPO"
    const val Round = "RONDA"
    const val Points = "PUNTOS"
    const val Asks = "PIDE"
    const val TimeUp = "¡SE ACABÓ EL TIEMPO!"
    const val RoundDone = "¡FOGATA LISTA!"
    const val Ready = "Toca la pieza que te pide"
    val Instructions = listOf(
        "Mira qué pieza te pide y tócala en la bandeja (o usa las teclas 1 a 8).",
        "Pon las piezas en el orden que indica; con cada acierto seguido ganas más puntos.",
        "Cuidado con las piezas trampa, como la leña mojada: no sirven y restan.",
        "Cada fogata es más difícil que la anterior. Si te tardas, él la coloca por ti.",
    )

    fun intro(name: String) = "Armen juntos la fogata de esta noche con $name."
    fun asks(name: String) = "$name $Asks"
    fun result(result: StepResult) = when (result) {
        StepResult.CORRECT -> "¡BIEN!"
        StepResult.WRONG -> "ESA NO ERA"
        StepResult.DECOY -> "¡ESA NO SIRVE!"
        StepResult.MISSED -> "SE TARDÓ: LA PUSIERON POR TI"
    }
}
