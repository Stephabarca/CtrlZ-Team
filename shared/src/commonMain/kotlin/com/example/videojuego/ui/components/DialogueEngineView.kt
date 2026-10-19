package com.example.videojuego.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.narrative.ChoiceOption
import com.example.videojuego.narrative.DialogueEngine
import com.example.videojuego.narrative.DialogueNode
import com.example.videojuego.ui.Strings
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.ui.theme.accent
import com.example.videojuego.ui.theme.onAccent
import kotlinx.coroutines.delay

/**
 * Componente UI reutilizable para el motor de diálogo y narrativa (EP-2).
 * Muestra el efecto de escritura tipo máquina de escribir, avance por toques,
 * y nodos de decisión con opciones para ramificar la conversación.
 */
@Composable
fun DialogueEngineView(
    nodes: List<DialogueNode>,
    onSceneEnd: () -> Unit,
    modifier: Modifier = Modifier,
    onEmptyScene: () -> Unit = onSceneEnd,
    charDelayMs: Long = 30L,
    initialAffinity: Map<String, Int> = emptyMap(),
    initialFlags: Map<String, Boolean> = emptyMap(),
    engine: DialogueEngine = remember(nodes, charDelayMs) {
        DialogueEngine(
            nodes = nodes,
            initialAffinity = initialAffinity,
            initialFlags = initialFlags,
            charDelayMs = charDelayMs,
        )
    },
    topBarContent: @Composable () -> Unit = {},
    contentOverlay: @Composable (DialogueNode) -> Unit = {},
) {
    val state by engine.state.collectAsState()

    // Bucle para el efecto de escritura (typewriter)
    LaunchedEffect(state.currentNodeId, state.isFinished, state.isEmptyScene) {
        if (state.isEmptyScene || state.isFinished) return@LaunchedEffect
        while (!state.isTextComplete) {
            delay(state.charDelayMs)
            engine.revealNextChar()
        }
    }

    // Notificación de fin de escena
    LaunchedEffect(state.isFinished) {
        if (state.isFinished && !state.isEmptyScene) {
            onSceneEnd()
        }
    }

    val handleTapScreen = {
        engine.handleTap(onSceneEnd = onSceneEnd, onEmptyScene = onEmptyScene)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF9FD8F0), Color(0xFFC9EAF5), Color(0xFFF1D9A0)),
                ),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = handleTapScreen,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                topBarContent()
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val currentNode = state.currentNode
                if (currentNode != null) {
                    contentOverlay(currentNode)
                }
            }

            if (state.isEmptyScene) {
                EmptySceneNotice(onReturn = onEmptyScene)
            } else {
                when (val node = state.currentNode) {
                    is DialogueNode.Line -> {
                        DialogBox(
                            speakerName = node.speaker.displayName,
                            accent = node.speaker.accent(),
                            onAccent = node.speaker.onAccent(),
                            text = state.displayedText,
                        ) {
                            ActionButton(
                                text = Strings.Continue,
                                onClick = handleTapScreen,
                            )
                        }
                    }
                    is DialogueNode.Choice -> {
                        ChoiceBox(
                            speakerName = node.speaker.displayName,
                            accent = node.speaker.accent(),
                            onAccent = node.speaker.onAccent(),
                            promptText = state.displayedText,
                            options = node.options,
                            affinityMap = state.affinity,
                            flagsMap = state.flags,
                            isTextComplete = state.isTextComplete,
                            onOptionSelected = { option ->
                                engine.selectChoice(option, onSceneEnd = onSceneEnd)
                            },
                        )
                    }
                    null -> {
                        EmptySceneNotice(onReturn = onEmptyScene)
                    }
                }
            }
        }
    }
}

/** Aviso amigable cuando una escena está vacía (Criterio de Aceptación Excepciones). */
@Composable
private fun EmptySceneNotice(onReturn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Esta escena no tiene contenido.",
            color = IslaColors.Ink,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Toca para regresar.",
            color = IslaColors.Ink.copy(alpha = 0.7f),
            fontFamily = PixelFontFamily,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(16.dp))
        ActionButton(
            text = "VOLVER",
            onClick = onReturn,
            container = IslaColors.Action,
        )
    }
}

/** Caja de decisión con opciones de respuesta. */
@Composable
private fun ChoiceBox(
    speakerName: String,
    accent: Color,
    onAccent: Color,
    promptText: String,
    options: List<ChoiceOption>,
    affinityMap: Map<String, Int>,
    flagsMap: Map<String, Boolean>,
    isTextComplete: Boolean,
    onOptionSelected: (ChoiceOption) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (speakerName.isNotBlank()) {
            Box(
                modifier = Modifier
                    .border(3.dp, IslaColors.Ink, RectangleShape)
                    .background(accent, RectangleShape)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(
                    text = speakerName.uppercase(),
                    color = onAccent,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, IslaColors.Ink, RectangleShape)
                .background(IslaColors.Paper, RectangleShape)
                .padding(16.dp),
        ) {
            Text(
                text = promptText,
                color = IslaColors.Ink,
                fontFamily = PixelFontFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )

            if (isTextComplete) {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        val available = option.isAvailable(affinityMap, flagsMap)
                        ActionButton(
                            text = option.text,
                            onClick = { if (available) onOptionSelected(option) },
                            modifier = Modifier.fillMaxWidth(),
                            container = if (available) IslaColors.Primary else IslaColors.Ink.copy(alpha = 0.3f),
                        )
                    }
                }
            }
        }
    }
}
