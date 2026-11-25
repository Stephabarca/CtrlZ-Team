package com.example.videojuego.ui.flashback

/** Qué se dibuja en cada escena de recuerdo. Los dibujos están en FlashbackArt.kt. */
enum class SceneArt {
    CINEMA, LIBRARY, DINNER, DOCK_NOTEBOOK,
    MOUNTAIN, OFFICE, ARGUMENT, DOCK_MESSAGE,
    STAGE, HALLWAY, ART_CLASS, DOCK_LETTER,
}

/** [place] es el título corto del lugar y [text] lo que se cuenta. Las comillas «así» marcan lo que dice el personaje. */
data class FlashbackScene(val art: SceneArt, val place: String, val text: String)

/**
 * Los recuerdos de cada personaje antes del viaje. Para cambiar un texto, editen solo esta lista:
 * la pantalla muestra lo que haya aquí, y cada escena dura según lo largo del texto.
 */
object FlashbackData {
    val byCharacter: Map<String, List<FlashbackScene>> = mapOf(
        "valentina" to listOf(
            FlashbackScene(
                SceneArt.CINEMA, "El cine, de noche",
                "Valentina barre las palomitas después de la última función. Una compañera le cuenta su día con lágrimas. «Tú siempre sabes qué decir.»",
            ),
            FlashbackScene(
                SceneArt.LIBRARY, "La biblioteca",
                "Subraya en un libro de psicología mientras su teléfono vibra sin parar: tres amigos, tres problemas. Ella contesta todos con una sonrisa.",
            ),
            FlashbackScene(
                SceneArt.DINNER, "La cena de cumpleaños",
                "Todos ríen y ella sonríe también, pero la mirada se le va a la ventana. Nadie le pregunta cómo está.",
            ),
            FlashbackScene(
                SceneArt.DOCK_NOTEBOOK, "El muelle",
                "Sube al avión con una mochila y un cuaderno nuevo. «Este viaje es para mí.» El mar se ve enorme.",
            ),
        ),
        "lucas" to listOf(
            FlashbackScene(
                SceneArt.MOUNTAIN, "La montaña, al amanecer",
                "Camina solo, con un mapa que él mismo dibujó. Se detiene, respira y sonríe. «Aquí no tengo que explicar nada.»",
            ),
            FlashbackScene(
                SceneArt.OFFICE, "La oficina",
                "Auriculares puestos, gráficos en pantalla. Un compañero le habla y él no lo oye. Nadie lo toma a mal, pero tampoco se acercan.",
            ),
            FlashbackScene(
                SceneArt.ARGUMENT, "La discusión",
                "Una persona cercana revisa su teléfono sin permiso. «Solo quería saber.» Lucas lo deja sobre la mesa y se va sin decir nada.",
            ),
            FlashbackScene(
                SceneArt.DOCK_MESSAGE, "El muelle",
                "Duda antes de subir al avión. Le escribe a una amiga: «Me voy unos días. Después te cuento.» Es la primera vez que avisa.",
            ),
        ),
        "lila" to listOf(
            FlashbackScene(
                SceneArt.STAGE, "El auditorio",
                "Equipo de debate, final del torneo. Lila argumenta sin una sola pausa y gana. El público aplaude.",
            ),
            FlashbackScene(
                SceneArt.HALLWAY, "El pasillo",
                "Una compañera del equipo, en silencio, se aleja de ella. Lila la sigue con la mirada, sin entender qué dijo mal.",
            ),
            FlashbackScene(
                SceneArt.ART_CLASS, "El aula de arte",
                "Ya como profesora, corrige un dibujo frente a todos con total precisión. El alumno baja la mirada.",
            ),
            FlashbackScene(
                SceneArt.DOCK_LETTER, "El muelle",
                "Guarda en su bolso una carta de disculpa sin enviar. «La mando cuando sepa qué cambiar.» El avión despega.",
            ),
        ),
    )

    fun scenesFor(characterId: String): List<FlashbackScene> = byCharacter[characterId].orEmpty()

    /** Cuánto dura una escena si nadie toca la pantalla: se lee con calma, mínimo 6 segundos. */
    fun durationMs(scene: FlashbackScene): Long = maxOf(6000L, scene.text.length * 55L + 2500L)

    /**
     * ¿Se muestra el recuerdo antes de la ronda de esta noche?
     * Sí, si el personaje tiene recuerdos, no se vio en esta sesión y el jugador todavía no respondió preguntas de esa noche
     * (así no se repite al volver a jugar una noche ya empezada).
     */
    fun shouldPlay(characterId: String, nightOrder: Int, answeredQuestionIds: List<String>, seen: Set<String>): Boolean =
        byCharacter.containsKey(characterId) &&
            characterId !in seen &&
            answeredQuestionIds.none { it.startsWith("n$nightOrder") }
}
