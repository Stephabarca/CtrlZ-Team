package com.example.videojuego.narrative

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Opción de respuesta en un nodo de decisión. */
data class ChoiceOption(
    val id: String,
    val text: String,
    val nextNodeId: String,
    val characterId: String? = null,
    val affinityDelta: Int = 0,
    val motive: String? = null,
    val feedback: String? = null,
    val minAffinity: Int? = null,
    val requiredFlags: Map<String, Boolean> = emptyMap(),
    val setFlags: Map<String, Boolean> = emptyMap(),
) {
    /** Verifica si el jugador cumple los requisitos de afinidad y banderas para elegir esta opción. */
    fun isAvailable(affinityMap: Map<String, Int>, flagsMap: Map<String, Boolean>): Boolean {
        if ((minAffinity != null) && (characterId != null)) {
            val current = affinityMap[characterId] ?: 30
            if (current < minAffinity) return false
        }
        for ((flag, expected) in requiredFlags) {
            if (flagsMap[flag] != expected) return false
        }
        return true
    }
}

/** Nodo dentro del árbol de diálogo (EP-2). Puede ser una línea hablada o una decisión con opciones. */
sealed interface DialogueNode {
    val id: String

    data class Line(
        override val id: String,
        val speaker: Speaker,
        val text: String,
        val nextNodeId: String? = null,
        val isTutorial: Boolean = false,
        val hint: TutorialHint = TutorialHint.NONE,
    ) : DialogueNode

    data class Choice(
        override val id: String,
        val speaker: Speaker = Speaker.NARRATOR,
        val prompt: String,
        val options: List<ChoiceOption>,
        val fallbackNodeId: String? = null,
    ) : DialogueNode
}

/** Estado reactivo del motor de diálogo. */
data class DialogueEngineState(
    val scene: Map<String, DialogueNode>,
    val startNodeId: String,
    val currentNodeId: String,
    val revealedChars: Int = 0,
    val affinity: Map<String, Int> = emptyMap(),
    val flags: Map<String, Boolean> = emptyMap(),
    val isFinished: Boolean = false,
    val isEmptyScene: Boolean = false,
    val charDelayMs: Long = 30L,
    val lastError: String? = null,
) {
    val currentNode: DialogueNode?
        get() = scene[currentNodeId]

    val currentLineText: String
        get() = when (val node = currentNode) {
            is DialogueNode.Line -> node.text
            is DialogueNode.Choice -> node.prompt
            null -> ""
        }

    val totalChars: Int
        get() = currentLineText.length

    val isTextComplete: Boolean
        get() = revealedChars >= totalChars

    val displayedText: String
        get() {
            val text = currentLineText
            if (text.isEmpty()) return ""
            val count = revealedChars.coerceIn(0, text.length)
            return text.take(count)
        }

    val currentIndex: Int
        get() {
            val keys = scene.keys.toList()
            val pos = keys.indexOf(currentNodeId)
            return if (pos >= 0) pos else 0
        }
}

/**
 * Motor narrativo y de diálogo reutilizable (EP-2).
 * Soporta diálogos lineales con efecto de escritura y nodos de decisión con ramificación.
 */
class DialogueEngine(
    nodes: List<DialogueNode>,
    initialAffinity: Map<String, Int> = emptyMap(),
    initialFlags: Map<String, Boolean> = emptyMap(),
    val charDelayMs: Long = 30L,
    private val defaultFallbackNodeId: String? = null,
) {
    /** Constructor de conveniencia para listas lineales de [DialogueLine]. */
    constructor(
        lines: List<DialogueLine>,
        charDelayMs: Long = 30L,
    ) : this(
        nodes = lines.mapIndexed { index, line ->
            val nextId = if (index + 1 < lines.size) "line_${index + 1}" else null
            DialogueNode.Line(
                id = "line_$index",
                speaker = line.speaker,
                text = line.text,
                nextNodeId = nextId,
                isTutorial = line.isTutorial,
                hint = line.hint,
            )
        },
        charDelayMs = charDelayMs,
    )

    private val sceneMap: Map<String, DialogueNode> = nodes.associateBy { it.id }
    private val startId: String = nodes.firstOrNull()?.id ?: ""

    private val _state = MutableStateFlow(
        if (nodes.isEmpty()) {
            DialogueEngineState(
                scene = emptyMap(),
                startNodeId = "",
                currentNodeId = "",
                isEmptyScene = true,
                isFinished = true,
                charDelayMs = charDelayMs,
            )
        } else {
            DialogueEngineState(
                scene = sceneMap,
                startNodeId = startId,
                currentNodeId = startId,
                affinity = initialAffinity,
                flags = initialFlags,
                charDelayMs = charDelayMs,
            )
        }
    )

    val state: StateFlow<DialogueEngineState> = _state.asStateFlow()

    /** Revela el siguiente carácter en el efecto de escritura. */
    fun revealNextChar() {
        val current = _state.value
        if (current.isTextComplete || current.isFinished || current.isEmptyScene) return
        _state.value = current.copy(revealedChars = current.revealedChars + 1)
    }

    /** Completa de inmediato el texto del nodo actual (Criterio de Aceptación 1). */
    fun completeCurrentText() {
        val current = _state.value
        if (current.isFinished || current.isEmptyScene) return
        _state.value = current.copy(revealedChars = current.totalChars)
    }

    /** Maneja el toque del jugador en la pantalla. */
    fun handleTap(onSceneEnd: () -> Unit = {}, onEmptyScene: () -> Unit = {}) {
        val current = _state.value
        if (current.isEmptyScene) {
            onEmptyScene()
            return
        }
        if (current.isFinished) {
            onSceneEnd()
            return
        }

        if (!current.isTextComplete) {
            completeCurrentText()
            return
        }

        when (val node = current.currentNode) {
            is DialogueNode.Line -> {
                if (node.nextNodeId == null) {
                    _state.value = current.copy(isFinished = true)
                    onSceneEnd()
                } else {
                    navigateToNode(node.nextNodeId, onSceneEnd)
                }
            }
            is DialogueNode.Choice -> {
                // En nodos de decisión, el toque no avanza automáticamente hasta que elija una opción.
            }
            null -> {
                handleMissingNode(current.currentNodeId, onSceneEnd)
            }
        }
    }

    /** Procesa la selección de una opción de respuesta y realiza la ramificación. */
    fun selectChoice(option: ChoiceOption, onSceneEnd: () -> Unit = {}) {
        val current = _state.value
        if (current.isFinished || current.isEmptyScene) return

        val updatedAffinity = if (option.characterId != null) {
            val prev = current.affinity[option.characterId] ?: 30
            val newAffinity = (prev + option.affinityDelta).coerceIn(0, 100)
            current.affinity + (option.characterId to newAffinity)
        } else {
            current.affinity
        }

        val updatedFlags = current.flags + option.setFlags

        _state.value = current.copy(
            affinity = updatedAffinity,
            flags = updatedFlags,
        )

        navigateToNode(option.nextNodeId, onSceneEnd)
    }

    /** Omite el resto del tutorial si la línea actual es de tutorial. */
    fun skipTutorial() {
        val current = _state.value
        if (current.isFinished || current.isEmptyScene) return

        var nextNode: DialogueNode? = current.currentNode
        while (nextNode is DialogueNode.Line && nextNode.isTutorial && nextNode.nextNodeId != null) {
            nextNode = sceneMap[nextNode.nextNodeId]
        }

        if (nextNode == null || (nextNode is DialogueNode.Line && nextNode.isTutorial)) {
            _state.value = current.copy(isFinished = true)
        } else {
            _state.value = current.copy(
                currentNodeId = nextNode.id,
                revealedChars = 0,
            )
        }
    }

    private fun navigateToNode(nodeId: String, onSceneEnd: () -> Unit) {
        if (sceneMap.containsKey(nodeId)) {
            _state.value = _state.value.copy(
                currentNodeId = nodeId,
                revealedChars = 0,
                lastError = null,
            )
        } else {
            handleMissingNode(nodeId, onSceneEnd)
        }
    }

    private fun handleMissingNode(missingId: String, onSceneEnd: () -> Unit) {
        val current = _state.value
        val fallbackId = defaultFallbackNodeId
            ?: (current.currentNode as? DialogueNode.Choice)?.fallbackNodeId
            ?: sceneMap.keys.firstOrNull { it != missingId }

        val errorMsg = "Nodo de destino '$missingId' no encontrado. Continuando en nodo seguro por defecto."
        println("[DialogueEngine Error] $errorMsg")

        if (fallbackId != null && sceneMap.containsKey(fallbackId)) {
            _state.value = current.copy(
                currentNodeId = fallbackId,
                revealedChars = 0,
                lastError = errorMsg,
            )
        } else {
            _state.value = current.copy(
                isFinished = true,
                lastError = errorMsg,
            )
            onSceneEnd()
        }
    }
}
