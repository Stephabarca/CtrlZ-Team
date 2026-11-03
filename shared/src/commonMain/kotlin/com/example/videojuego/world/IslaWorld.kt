package com.example.videojuego.world

import com.example.videojuego.narrative.Speaker

/** La isla del juego: mapa, personajes y punto de inicio. */
object IslaWorld {

    /** ~ agua, . arena, g pasto, T palmera, r roca, F fogata, H cabaña. */
    val map = WorldMap(
        listOf(
            "~~~~~~~~~~~~~~~~~~~~~~~~",
            "~~~~~~~~~~....~~~~~~~~~~",
            "~~~~~~......r.....~~~~~~",
            "~~~~~.....gggg...T.~~~~~",
            "~~~....ggTggggggg....~~~",
            "~~....ggggggggggHH....~~",
            "~~...TgggggggTggHHg...~~",
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

    fun npc(id: String): NpcData? = npcs.firstOrNull { it.id == id }

    /** Personaje de la noche [nightId], para saber a quién hay que buscar. */
    fun npcForNight(nightId: String): NpcData? = npcs.firstOrNull { it.nightId == nightId }
}
