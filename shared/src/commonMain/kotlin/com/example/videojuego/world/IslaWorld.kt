package com.example.videojuego.world

import com.example.videojuego.narrative.Speaker

/** La isla del juego: mapa, personajes y punto de inicio. */
object IslaWorld {

    /** ~ agua, . arena, g pasto, T palmera, r roca, F fogata, H cabaña, P puerta de la cabaña. */
    val map = WorldMap(
        listOf(
            "~~~~~~~~~~~~~~~~~~~~~~~~",
            "~~~~~~~~~~....~~~~~~~~~~",
            "~~~~~~......r.....~~~~~~",
            "~~~~~.....gggg...T.~~~~~",
            "~~~....ggTggggggg....~~~",
            "~~....ggggggggggHH....~~",
            "~~...TgggggggTggHPg...~~",
            "~...ggggggggggggggggr..~",
            "~...TgggggggggggggTg...~",
            "~...gggFgggggggggggg...~",
            "~..rgggggggggggggggT...~",
            "~~...gTgggggggTgggg...~~",
            "~~....ggggTggggggg....~~",
            "~~~....gggggggggg....~~~",
            "~~~~~.....gggg.r...~~~~~",
            "~~~~~~..r.........~~~~~~",
            "~~~~~~~~~~....~~~~~~~~~~",
            "~~~~~~~~~~~~~~~~~~~~~~~~",
        ),
    )

    /** Dónde aparece el jugador: la playa del sur, donde llegó tras el naufragio. */
    const val PLAYER_START_X = 11.5f
    const val PLAYER_START_Y = 14.5f

    val npcs: List<NpcData> = listOf(
        NpcData(
            id = "valentina", name = "Valentina", speaker = Speaker.VALENTINA,
            tileX = 8, tileY = 9, nightId = "night1",
            idleLine = "Hoy me toca encargarme de la fogata. ¡Nos vemos cuando caiga la noche!",
            chatLines = listOf(
                "¡Hola! Me encanta cuando amanece en la isla.",
                "¿Dormiste bien? Yo casi no pegué el ojo.",
                "Si ves cocos en las palmeras, avísame. ¡Tengo hambre!",
            ),
        ),
        NpcData(
            id = "lucas", name = "Lucas", speaker = Speaker.LUCAS,
            tileX = 11, tileY = 3, nightId = "night2",
            idleLine = "Estoy revisando el sendero del norte. Prefiero seguir solo un rato.",
            chatLines = listOf(
                "Buenas. Estoy marcando el sendero del norte.",
                "Gracias por saludar. Ahora necesito un rato tranquilo.",
                "Las rocas del norte sirven para sacar carbón, por si te interesa.",
            ),
            dayLines = mapOf(
                2 to listOf(
                    "Hoy ando buscando buena madera. Los troncos del sendero son firmes.",
                    "El techo de la cabaña gotea. No es urgente, pero prefiero arreglarlo antes de que llueva.",
                    "Si me ayudas, bien. Si prefieres hacer otra cosa, también está bien.",
                    "Anoche dormí mejor. No sé por qué te lo cuento.",
                ),
            ),
            memoryLines = listOf(
                "n1_riesgo" to "Ayer, mientras hablabas con Valentina, algo no me cuadró. Prefiero decírtelo directo y sin drama.",
                "n1_sana" to "Anoche te vi hablar con Valentina. La escuchaste sin interrumpir. Eso se nota, aunque no lo diga mucho.",
                "pesca_buena" to "Vi el pescado que trajeron anoche. Se nota que tienes paciencia. No es fácil esperar sin apurar.",
                "pesca_floja" to "Anoche casi no pescaron nada, ¿no? Pasa. Un día malo no define nada.",
            ),
        ),
        NpcData(
            id = "lila", name = "Lila", speaker = Speaker.LILA,
            tileX = 16, tileY = 7, nightId = "night3",
            idleLine = "Estoy reforzando el refugio. Si quieres opinar, hazlo con argumentos.",
            chatLines = listOf(
                "El refugio aguanta, pero el techo está flojo. ¿Opinas distinto?",
                "Prefiero que me digas las cosas de frente, sin rodeos.",
                "Si recoges algo útil, dímelo. No me gusta adivinar.",
            ),
        ),
    )

    /**
     * Árboles del sendero que, desde el día 2, dan madera en vez de cocos. Están marcados con un tronco en la base.
     * Hay uno más de los que pide el objetivo (3) para que sobre.
     */
    val woodTrees: Set<Pair<Int, Int>> = setOf(9 to 4, 17 to 3, 13 to 6, 5 to 6)

    /** Controlador con la isla y la cabaña. Es lo que usa el juego. */
    fun newController() = WorldController(
        map = map,
        npcs = npcs,
        startX = PLAYER_START_X,
        startY = PLAYER_START_Y,
        indoorMap = CabinWorld.map,
        indoorStartX = CabinWorld.START_X,
        indoorStartY = CabinWorld.START_Y,
        woodTrees = woodTrees,
    )

    fun npc(id: String): NpcData? = npcs.firstOrNull { it.id == id }

    /** Personaje de la noche [nightId], para saber a quién hay que buscar. */
    fun npcForNight(nightId: String): NpcData? = npcs.firstOrNull { it.nightId == nightId }
}
