package com.example.videojuego.ui.fishing

/** Textos de la pesca. Pendiente: migrar a composeResources/values/strings.xml. */
object FishingStrings {
    const val Title = "PESCA AL AMANECER"
    const val Start = "EMPEZAR"
    const val Pull = "JALAR"
    const val Time = "TIEMPO"
    const val Fish = "PECES"
    const val TimeUp = "¡SE ACABÓ EL TIEMPO!"
    const val Waiting = "Espera a que pique..."
    const val Bite = "¡PICÓ! Jala cuando la marca esté en el centro"
    val Instructions = listOf(
        "Lanza la caña y espera: el corcho se hundirá cuando pique un pez.",
        "Toca JALAR cuando la marca esté en la zona dorada del centro.",
        "Si jalas antes de que pique, los peces se asustan.",
    )

    fun intro(name: String) = "Pesca con $name para pasar un rato juntos."
    fun result(result: com.example.videojuego.minigame.fishing.CatchResult) = when (result) {
        com.example.videojuego.minigame.fishing.CatchResult.PERFECT -> "¡PERFECTO!"
        com.example.videojuego.minigame.fishing.CatchResult.GOOD -> "¡BIEN!"
        com.example.videojuego.minigame.fishing.CatchResult.WEAK -> "UN PEZ PEQUEÑO"
        com.example.videojuego.minigame.fishing.CatchResult.LOST -> "SE ESCAPÓ"
        com.example.videojuego.minigame.fishing.CatchResult.EARLY -> "¡MUY PRONTO!"
    }
}
