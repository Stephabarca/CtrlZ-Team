package com.example.videojuego.ui.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.narrative.ChoiceOption
import com.example.videojuego.narrative.DialogueNode
import com.example.videojuego.narrative.Speaker
import com.example.videojuego.ui.components.DialogueEngineView
import com.example.videojuego.ui.theme.IslaColors

/**
 * Pantalla de demostración aislada para probar el motor narrativo y de decisiones (EP-2)
 * sin afectar la intro ni ningún otro componente del juego.
 */
@Composable
fun DialogueEngineDemo(
    onFinished: () -> Unit = {},
) {
    var finishedNotice by remember { mutableStateOf(false) }

    val demoNodes = remember {
        listOf(
            DialogueNode.Line(
                id = "start",
                speaker = Speaker.VALENTINA,
                text = "¡Hola! Esta es una prueba aislada del motor de diálogo y decisiones (EP-2).",
                nextNodeId = "choice_1",
            ),
            DialogueNode.Choice(
                id = "choice_1",
                speaker = Speaker.VALENTINA,
                prompt = "¿Qué deseas probar en el motor de decisiones?",
                options = listOf(
                    ChoiceOption(
                        id = "opt_a",
                        text = "1. Probar ramificación amigable (+10 afinidad)",
                        nextNodeId = "friendly_path",
                        characterId = "valentina",
                        affinityDelta = 10,
                    ),
                    ChoiceOption(
                        id = "opt_b",
                        text = "2. Probar ramificación distante (-5 afinidad)",
                        nextNodeId = "cold_path",
                        characterId = "valentina",
                        affinityDelta = -5,
                    ),
                ),
            ),
            DialogueNode.Line(
                id = "friendly_path",
                speaker = Speaker.VALENTINA,
                text = "Elegiste el camino amigable. La afinidad de Valentina aumentó +10.",
            ),
            DialogueNode.Line(
                id = "cold_path",
                speaker = Speaker.VALENTINA,
                text = "Elegiste el camino distante. La afinidad de Valentina se redujo -5.",
            ),
        )
    }

    if (finishedNotice) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "¡Demostración completada exitosamente!",
                color = IslaColors.Primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        DialogueEngineView(
            nodes = demoNodes,
            onSceneEnd = {
                finishedNotice = true
                onFinished()
            },
        )
    }
}
