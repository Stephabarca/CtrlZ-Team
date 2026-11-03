package com.example.videojuego.ui.world

/** Textos del mapa. Pendiente: migrar a composeResources/values/strings.xml. */
object WorldStrings {
    const val Talk = "HABLAR"
    const val Hint = "Camina con el joystick (o WASD). Toca palmeras y rocas para recoger, y habla con los demás"
    const val NightHint = "Ya es de noche. ¡Ve a la fogata!"
    const val NightNpc = "Ya anocheció. Acércate a la fogata y hablamos allí."
    const val Day = "DÍA"
    const val Night = "NOCHE"
    const val BagEmpty = "Mochila vacía"
    const val AlreadyGifted = "Ya le diste un regalo hoy."

    fun gift(itemLabel: String, count: Int) = "REGALAR ${itemLabel.uppercase()} (x$count)"
    fun picked(itemLabel: String) = "+1 ${itemLabel.uppercase()}"
}
