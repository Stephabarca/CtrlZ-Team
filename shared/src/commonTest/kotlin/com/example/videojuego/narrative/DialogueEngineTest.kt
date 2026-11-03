package com.example.videojuego.narrative

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DialogueEngineTest {

    @Test
    fun testTenOrMoreLinesTypewriterCompletionAndAdvancement() {
        val lines = (1..12).map { index ->
            DialogueLine(
                speaker = Speaker.NARRATOR,
                text = "Esta es la linea de dialogo numero $index con texto largo.",
            )
        }

        var sceneEnded = false
        val engine = DialogueEngine(lines = lines, charDelayMs = 10L)

        val initialState = engine.state.value
        assertEquals("line_0", initialState.currentNodeId)
        assertFalse(initialState.isTextComplete)
        assertEquals(0, initialState.revealedChars)

        // Criterio de Aceptación 1: Tocar durante la escritura completa el texto de inmediato
        engine.handleTap(onSceneEnd = { sceneEnded = true })
        val stateAfterFirstTap = engine.state.value
        assertTrue(stateAfterFirstTap.isTextComplete)
        assertEquals(stateAfterFirstTap.totalChars, stateAfterFirstTap.revealedChars)
        assertEquals(stateAfterFirstTap.currentLineText, stateAfterFirstTap.displayedText)

        // Criterio de Aceptación 2: Tocar con texto completo avanza a la siguiente línea
        engine.handleTap(onSceneEnd = { sceneEnded = true })
        val stateAfterSecondTap = engine.state.value
        assertEquals("line_1", stateAfterSecondTap.currentNodeId)
        assertFalse(stateAfterSecondTap.isTextComplete)

        // Avanzar por las 12 líneas de la escena
        repeat(11) {
            engine.completeCurrentText()
            engine.handleTap(onSceneEnd = { sceneEnded = true })
        }

        assertTrue(engine.state.value.isFinished)
        assertTrue(sceneEnded)
    }

    @Test
    fun testChoiceNodeSelectionBranchingAndFlags() {
        val nodeLine0 = DialogueNode.Line(
            id = "start",
            speaker = Speaker.VALENTINA,
            text = "Hola, ¿cómo estás?",
            nextNodeId = "choice_1",
        )
        val optionA = ChoiceOption(
            id = "opt_a",
            text = "Bien, gracias.",
            nextNodeId = "node_a",
            characterId = "valentina",
            affinityDelta = 10,
            setFlags = mapOf("said_hello" to true),
        )
        val optionB = ChoiceOption(
            id = "opt_b",
            text = "No me molestes.",
            nextNodeId = "node_b",
            characterId = "valentina",
            affinityDelta = -5,
        )
        val nodeChoice = DialogueNode.Choice(
            id = "choice_1",
            speaker = Speaker.VALENTINA,
            prompt = "¿Qué respondes?",
            options = listOf(optionA, optionB),
        )
        val nodeA = DialogueNode.Line(
            id = "node_a",
            speaker = Speaker.VALENTINA,
            text = "¡Me alegro mucho!",
        )
        val nodeB = DialogueNode.Line(
            id = "node_b",
            speaker = Speaker.VALENTINA,
            text = "Está bien, perdón.",
        )

        val engine = DialogueEngine(
            nodes = listOf(nodeLine0, nodeChoice, nodeA, nodeB),
            initialAffinity = mapOf("valentina" to 30),
        )

        // Completar línea inicial y avanzar a la decisión
        engine.completeCurrentText()
        engine.handleTap()

        val choiceState = engine.state.value
        assertEquals("choice_1", choiceState.currentNodeId)
        assertTrue(choiceState.currentNode is DialogueNode.Choice)

        // Seleccionar Opción A
        engine.selectChoice(optionA)

        val stateAfterChoice = engine.state.value
        assertEquals("node_a", stateAfterChoice.currentNodeId)
        assertEquals(40, stateAfterChoice.affinity["valentina"])
        assertEquals(expected = true, actual = stateAfterChoice.flags["said_hello"])
    }

    @Test
    fun testMissingNodeFallbackExceptionHandling() {
        val nodeLine0 = DialogueNode.Line(
            id = "start",
            speaker = Speaker.NARRATOR,
            text = "Inicio de escena con nodo siguiente inexistente.",
            nextNodeId = "missing_node_999",
        )
        val fallbackNode = DialogueNode.Line(
            id = "fallback",
            speaker = Speaker.NARRATOR,
            text = "Nodo de recuperación seguro.",
        )

        val engine = DialogueEngine(
            nodes = listOf(nodeLine0, fallbackNode),
            defaultFallbackNodeId = "fallback",
        )

        engine.completeCurrentText()
        engine.handleTap()

        val state = engine.state.value
        assertEquals("fallback", state.currentNodeId)
        val error = state.lastError
        assertNotNull(error)
        assertTrue(error.contains("missing_node_999"))
    }

    @Test
    fun testEmptySceneExceptionHandling() {
        var emptySceneNotified = false
        val engine = DialogueEngine(lines = emptyList())

        val state = engine.state.value
        assertTrue(state.isEmptyScene)
        assertTrue(state.isFinished)

        // Criterio de Aceptación Excepciones: Escena vacía regresa limpiamente sin crash
        engine.handleTap(onEmptyScene = { emptySceneNotified = true })
        assertTrue(emptySceneNotified)
    }
}
