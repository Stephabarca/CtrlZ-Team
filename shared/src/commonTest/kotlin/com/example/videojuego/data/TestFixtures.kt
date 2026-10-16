package com.example.videojuego.data

import com.example.videojuego.data.content.AffinityStage
import com.example.videojuego.data.content.AnswerData
import com.example.videojuego.data.content.CharacterData
import com.example.videojuego.data.content.DialogueLineData
import com.example.videojuego.data.content.EndingData
import com.example.videojuego.data.content.EndingRule
import com.example.videojuego.data.content.GameConfig
import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.content.MinigameData
import com.example.videojuego.data.content.MotiveData
import com.example.videojuego.data.content.NightData
import com.example.videojuego.data.content.QuestionData

/** Contenido mínimo para probar las reglas sin leer archivos. */
object TestFixtures {

    private fun character(id: String) = CharacterData(
        id = id, name = id, age = 20, occupation = "x", accentColor = "#FFFFFF", difficulty = "baja",
        learning = "x", summary = "x", boundaries = emptyList(), pendingLearning = "x",
    )

    private fun night(order: Int, characterId: String, weight: Double, minigameId: String) = NightData(
        id = "night$order", order = order, characterId = characterId, title = "Noche $order",
        backgroundId = "bg", timeLimitSec = 20, answerWeight = weight, minigameId = minigameId,
        intro = listOf(DialogueLineData("narrator", "intro")),
        questions = listOf(
            QuestionData(
                id = "n${order}q1", speakerId = characterId, prompt = "¿Pregunta?",
                answers = listOf(
                    AnswerData("n${order}q1a1", "mejor", 10, "escucha", "bien"),
                    AnswerData("n${order}q1a2", "bien", 5, "empatia", "bien"),
                    AnswerData("n${order}q1a3", "mal", -5, "minimiza", "mal"),
                    AnswerData("n${order}q1a4", "peor", -10, "celos", "mal"),
                ),
            ),
        ),
        outro = listOf(DialogueLineData("narrator", "outro")),
    )

    fun content() = GameContent(
        config = GameConfig(
            startAffinity = 30, minAffinity = 0, maxAffinity = 100, timeoutDelta = -2,
            stages = listOf(
                AffinityStage("desconocidos", "Desconocidos", 0),
                AffinityStage("conocidos", "Conocidos", 25),
                AffinityStage("amistad", "Amistad", 50),
                AffinityStage("cercania", "Cercanía", 75),
            ),
        ),
        motives = listOf(
            MotiveData("escucha", "Escucha", true),
            MotiveData("empatia", "Empatía", true),
            MotiveData("minimiza", "Minimizar", false),
            MotiveData("celos", "Celos", false),
        ),
        characters = listOf(character("valentina"), character("lucas"), character("lila")),
        nights = listOf(
            night(1, "valentina", 1.0, "pesca"),
            night(2, "lucas", 1.0, "fogata"),
            night(3, "lila", 1.2, "memoria"),
        ),
        minigames = listOf(
            MinigameData("pesca", "Pesca", "valentina", "tap_timing", 15, 30, "baja"),
            MinigameData("fogata", "Fogata", "lucas", "drag_drop", 12, 45, "media"),
            MinigameData("memoria", "Memoria", "lila", "memory", 10, 60, "alta"),
        ),
        endings = listOf(
            EndingData("juntos", "Juntos", "t", 3, EndingRule(minCharactersAbove = 2, threshold = 70, noOneBelow = 40)),
            EndingData("vinculo", "Vínculo", "t", 2, EndingRule(minCharactersAbove = 1, threshold = 75)),
            EndingData("solos", "Solos", "t", 1, EndingRule()),
        ),
    )
}
