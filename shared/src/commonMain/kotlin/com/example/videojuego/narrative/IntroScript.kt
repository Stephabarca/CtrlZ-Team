package com.example.videojuego.narrative

import com.example.videojuego.narrative.Speaker.LILA
import com.example.videojuego.narrative.Speaker.LUCAS
import com.example.videojuego.narrative.Speaker.NARRATOR
import com.example.videojuego.narrative.Speaker.VALENTINA

/**
 * Guion de la Fase 1. Está como datos Kotlin con el mismo modelo que usará el JSON (IP-6),
 * así que migrarlo a un archivo solo requiere agregar la serialización.
 *
 * Orden: naufragio -> los 3 supervivientes -> tutorial (omitible) -> cierre.
 * Los supervivientes se presentan ANTES del tutorial, así omitirlo nunca los salta.
 */
object IntroScript {
    val lines: List<DialogueLine> = listOf(
        // Naufragio
        DialogueLine(NARRATOR, "El accidente fue repentino. Lo último que recuerdas es el estruendo y el agua helada."),
        DialogueLine(NARRATOR, "Despiertas sobre la arena de una playa desconocida. Apenas amanece y no hay señales de rescate."),
        DialogueLine(NARRATOR, "No estás solo. Tres personas más lograron llegar a la orilla."),

        // Supervivientes
        DialogueLine(VALENTINA, "¡Qué alivio que despertaras! Soy Valentina. Estudio psicología, así que si necesitas hablar, aquí estoy."),
        DialogueLine(LUCAS, "Lucas. Trabajo con datos. Prefiero revisar el terreno antes de hablar de más."),
        DialogueLine(LILA, "Lila, profesora de arte. Te lo digo directo: no sé si nos rescatarán pronto, y prefiero que lo sepas."),

        // Tutorial (omitible)
        DialogueLine(NARRATOR, "Cada noche te sentarás junto a uno de ellos a conversar.", isTutorial = true),
        DialogueLine(
            NARRATOR,
            "Te harán preguntas y tendrás poco tiempo para responder. Si se acaba, el silencio también cuenta.",
            isTutorial = true,
            hint = TutorialHint.TIMER,
        ),
        DialogueLine(
            NARRATOR,
            "Lo que respondas cambia tu vínculo con cada persona. Escuchar, respetar y ser honesto lo hace crecer.",
            isTutorial = true,
            hint = TutorialHint.AFFINITY,
        ),
        DialogueLine(NARRATOR, "Los minijuegos también suman, pero nada reemplaza lo que decides decir.", isTutorial = true),

        // Cierre
        DialogueLine(NARRATOR, "Cae la tarde. Hay que organizarse para pasar la primera noche."),
        DialogueLine(VALENTINA, "¿Qué crees que deberíamos conseguir primero?"),
    )

    /** Nodos de prueba con opciones de decisión para probar el motor en la UI. */
    val choiceNodes: List<DialogueNode> = listOf(
        DialogueNode.Line(
            id = "1",
            speaker = VALENTINA,
            text = "¡Qué alivio que despertaras! ¿Cómo te sientes?",
            nextNodeId = "choice_1",
        ),
        DialogueNode.Choice(
            id = "choice_1",
            speaker = VALENTINA,
            prompt = "Elige una opción de respuesta:",
            options = listOf(
                ChoiceOption(
                    id = "opt1",
                    text = "1. Me alegra estar contigo (+10 afinidad)",
                    nextNodeId = "node_friendly",
                    characterId = "valentina",
                    affinityDelta = 10,
                ),
                ChoiceOption(
                    id = "opt2",
                    text = "2. Déjame en paz (-5 afinidad)",
                    nextNodeId = "node_cold",
                    characterId = "valentina",
                    affinityDelta = -5,
                ),
            ),
        ),
        DialogueNode.Line(
            id = "node_friendly",
            speaker = VALENTINA,
            text = "¡A mí también! Juntos saldremos de esta playa.",
        ),
        DialogueNode.Line(
            id = "node_cold",
            speaker = VALENTINA,
            text = "Entendido... iré a revisar la fogata.",
        ),
    )
}
