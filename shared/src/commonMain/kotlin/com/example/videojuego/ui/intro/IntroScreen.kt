package com.example.videojuego.ui.intro

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.narrative.IntroController
import com.example.videojuego.narrative.IntroResult
import com.example.videojuego.narrative.Speaker
import com.example.videojuego.narrative.TutorialHint
import com.example.videojuego.ui.Strings
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.components.DialogBox
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.ui.theme.accent
import com.example.videojuego.ui.theme.onAccent
import kotlinx.coroutines.delay

/**
 * Fase 1 (IP-9): naufragio, presentación de los 3 supervivientes y tutorial omitible.
 * Tocar la pantalla o CONTINUAR completa el texto; con el texto completo, avanza.
 *
 * @param charDelayMs milisegundos por letra (se conectará a Ajustes en IP-37).
 * @param onFinished se llama al terminar, con el resultado de la fase.
 */
@Composable
fun IntroScreen(
    onFinished: (IntroResult) -> Unit,
    modifier: Modifier = Modifier,
    charDelayMs: Long = 30L,
) {
    val controller = remember { IntroController() }
    val state by controller.state.collectAsState()
    val line = state.line

    var revealed by remember(state.index) { mutableIntStateOf(0) }
    LaunchedEffect(state.index) {
        val total = line.text.length
        while (revealed < total) {
            delay(charDelayMs)
            revealed++
        }
    }

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished(state.toResult())
    }

    val advance: () -> Unit = {
        if (revealed < line.text.length) {
            revealed = line.text.length
        } else {
            controller.next()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Fondo provisional: se reemplaza por el arte real (IP-32).
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF9FD8F0), Color(0xFFC9EAF5), Color(0xFFF1D9A0)),
                ),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = advance,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (line.isTutorial) {
                    ActionButton(
                        text = Strings.SkipTutorial,
                        onClick = controller::skipTutorial,
                        container = IslaColors.Action,
                    )
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (line.speaker != Speaker.NARRATOR) SpritePlaceholder(line.speaker)
                if (line.hint != TutorialHint.NONE) TutorialHintView(line.hint)
            }

            DialogBox(
                speakerName = line.speaker.displayName,
                accent = line.speaker.accent(),
                onAccent = line.speaker.onAccent(),
                text = line.text.take(revealed),
            ) {
                ActionButton(text = Strings.Continue, onClick = advance)
            }
        }
    }
}

/** Sprite provisional hasta tener el arte de los personajes (IP-33). */
@Composable
private fun SpritePlaceholder(speaker: Speaker) {
    Box(
        modifier = Modifier
            .size(width = 150.dp, height = 230.dp)
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(speaker.accent(), RectangleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = speaker.displayName.uppercase(),
            color = speaker.onAccent(),
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun TutorialHintView(hint: TutorialHint) {
    Column(
        modifier = Modifier
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (hint) {
            TutorialHint.TIMER -> TimerDemo()
            TutorialHint.AFFINITY -> AffinityDemo()
            TutorialHint.NONE -> Unit
        }
    }
}

/** Barra que se vacía en bucle para mostrar cómo corre el tiempo de respuesta. */
@Composable
private fun TimerDemo() {
    val transition = rememberInfiniteTransition(label = "timerDemo")
    val progress by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "timerProgress",
    )
    Box(Modifier.size(width = 200.dp, height = 18.dp).border(3.dp, IslaColors.Ink, RectangleShape)) {
        Box(
            Modifier
                .size(width = 200.dp * progress, height = 18.dp)
                .background(IslaColors.Action, RectangleShape),
        )
    }
}

/** Corazones de ejemplo: 3 de 5 llenos. */
@Composable
private fun AffinityDemo() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(5) { i ->
            Text(
                text = "♥",
                fontSize = 30.sp,
                color = if (i < 3) IslaColors.Action else IslaColors.Ink.copy(alpha = 0.25f),
            )
        }
    }
}
