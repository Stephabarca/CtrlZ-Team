package com.example.videojuego.world

/** El interior de la cabaña: una cama, un cofre y las maletas que se salvaron del accidente. */
object CabinWorld {

    /** f piso, # pared, B cama, C cofre, M maleta, E puerta de salida. */
    val map = WorldMap(
        listOf(
            "##########",
            "#BBffCfMM#",
            "#ffffffff#",
            "#ffffffff#",
            "#fMffffff#",
            "#ffffffMf#",
            "#ffffffff#",
            "####E#####",
        ),
    )

    /** Dónde aparece el jugador al entrar: junto a la puerta. */
    const val START_X = 4.5f
    const val START_Y = 6.5f

    /** Lo que cuenta cada maleta al tocarla. Son decoración con un poco de historia. */
    private val suitcaseTexts = mapOf(
        (7 to 1) to "Una maleta golpeada con una etiqueta de aerolínea. Dentro hay ropa todavía húmeda y un cuaderno de dibujo.",
        (8 to 1) to "Una mochila de senderismo. Tiene un mapa doblado y una laptop que ya no enciende.",
        (2 to 4) to "Un bolso pequeño con boletos de cine arrugados. Alguien los guardó como recuerdo.",
        (7 to 5) to "Una maleta cerrada con un candado. Parece que nadie quiere hablar de lo que hay adentro.",
    )

    const val DEFAULT_SUITCASE_TEXT = "Una de las pocas cosas que se pudieron rescatar del accidente."

    fun suitcaseText(x: Int, y: Int): String = suitcaseTexts[x to y] ?: DEFAULT_SUITCASE_TEXT
}
