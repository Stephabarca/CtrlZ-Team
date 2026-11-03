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
    const val Cabin = "CABAÑA"
    const val Enter = "ENTRAR"
    const val Exit = "SALIR"
    const val Sleep = "DORMIR"
    const val Chest = "COFRE"
    const val Look = "MIRAR"
    const val NoSleepAtNight = "Ya es de noche. Antes de dormir, ve a la fogata."
    const val Sleeping = "Zzz..."
    const val Saved = "PARTIDA GUARDADA"
    const val ChestTitle = "COFRE"
    const val Bag = "Mochila"
    const val InChest = "Cofre"
    const val Store = "GUARDAR"
    const val Take = "SACAR"
    const val StoreAll = "GUARDAR TODO"
    const val Close = "CERRAR"
    const val PauseTitle = "PAUSA"
    const val Resume = "SEGUIR JUGANDO"
    const val Settings = "AJUSTES"
    const val BackToMenu = "VOLVER AL MENÚ"
    const val LeaveTitle = "¿VOLVER AL MENÚ?"
    const val LeaveBody = "Perderás lo avanzado desde la última vez que dormiste en la cama."
    const val LeaveYes = "SÍ, VOLVER"
    const val LeaveNo = "CANCELAR"

    fun gift(itemLabel: String, count: Int) = "REGALAR ${itemLabel.uppercase()} (x$count)"
    fun picked(itemLabel: String) = "+1 ${itemLabel.uppercase()}"

    fun tileAction(type: com.example.videojuego.world.TileType) = when (type) {
        com.example.videojuego.world.TileType.HUT_DOOR -> Enter
        com.example.videojuego.world.TileType.EXIT_DOOR -> Exit
        com.example.videojuego.world.TileType.BED -> Sleep
        com.example.videojuego.world.TileType.CHEST -> Chest
        else -> Look
    }
}
