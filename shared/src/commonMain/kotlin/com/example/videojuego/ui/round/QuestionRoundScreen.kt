package com.example.videojuego.ui.round

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.narrative.Speaker
import com.example.videojuego.round.AnswerFeedback
import com.example.videojuego.round.QuestionRoundController
import com.example.videojuego.round.RoundResult
import com.example.videojuego.timer.CountdownTimer
import com.example.videojuego.timer.RunCountdown
import com.example.videojuego.timer.TimerState
import com.example.videojuego.ui.Strings
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.components.AffinityHearts
import com.example.videojuego.ui.components.AnswerOptionCard
import com.example.videojuego.ui.components.DialogBox
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.ui.theme.accent
import com.example.videojuego.ui.theme.onAccent

private const val LOW_TIME_SECONDS = 5L
private const val LOW_TIME_FRACTION = 0.25f

/**
 * Ronda de preguntas de una noche (IP-11) con temporizador (IP-10) y barra de corazones.
 *
 * Temporizador: cuenta regresiva por pregunta ([CountdownTimer]). Se detiene al responder,
 * no cuenta el tiempo que la app pasa en segundo plano y, al llegar a 0, aplica el efecto por defecto.
 *
 * @param initialRemainingMs tiempo restante de la primera pregunta mostrada (al restaurar tras rotar).
 * @param onProgress se llama después de cada respuesta con el progreso actualizado (para guardarlo).
 * @param onSnapshot se llama cada segundo y tras cada respuesta, con lo necesario para retomar la ronda.
 * @param onFinished se llama al terminar la última pregunta.
 */
@Composable
fun QuestionRoundScreen(
    controller: QuestionRoundController,
    onFinished: (RoundResult) -> Unit,
    modifier: Modifier = Modifier,
    initialRemainingMs: Long? = null,
    onProgress: (SaveGame) -> Unit = {},
    onSnapshot: (SaveGame, Long?) -> Unit = { _, _ -> },
) {
    val state by controller.state.collectAsState()
    val question = state.question
    val feedback = state.feedback
    val answered = feedback != null
    val limitMs = controller.night.timeLimitSec * 1000L

    // Un temporizador nuevo por pregunta. Solo la primera usa el tiempo restaurado.
    val firstQuestionId = remember { question.id }
    val timer = remember(question.id) {
        val start = if (question.id == firstQuestionId) initialRemainingMs ?: limitMs else limitMs
        CountdownTimer(totalMs = limitMs, initialRemainingMs = start)
    }
    val timerState by timer.state.collectAsState()
    RunCountdown(timer)

    LaunchedEffect(answered) {
        if (answered) timer.pause()
    }
    LaunchedEffect(timerState.expired) {
        if (timerState.expired && !answered) controller.timeExpired(timerState.elapsedMs)
    }
    LaunchedEffect(state.save.answers.size) {
        if (state.save.answers.isNotEmpty()) onProgress(state.save)
    }
    LaunchedEffect(question.id, timerState.secondsLeft, answered) {
        onSnapshot(state.save, if (answered) null else timerState.remainingMs)
    }
    LaunchedEffect(state.finished) {
        if (state.finished) onFinished(RoundResult(state.save))
    }

    val speaker = Speaker.entries.firstOrNull { it.name.equals(question.speakerId, ignoreCase = true) }
        ?: Speaker.NARRATOR

    Box(
        modifier = modifier
            .fillMaxSize()
            // Fondo provisional: se reemplaza por el arte real (IP-32).
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3B4A6B), Color(0xFF6B5B8C), Color(0xFFD9A066)),
                ),
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RoundHeader(
                counter = RoundStrings.counter(state.questionIndex + 1, state.totalQuestions),
                timer = timerState,
                affinityFraction = controller.affinityFraction(state.affinity),
                stageName = controller.stageName(state.affinity),
            )

            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (speaker != Speaker.NARRATOR) SpritePlaceholder(speaker)
            }

            if (feedback != null) DeltaChip(feedback)

            DialogBox(
                speakerName = speaker.displayName,
                accent = speaker.accent(),
                onAccent = speaker.onAccent(),
                text = feedback?.message ?: question.prompt,
            ) {
                if (feedback != null) ActionButton(text = Strings.Continue, onClick = controller::next)
            }

            if (feedback == null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.answers.forEachIndexed { index, answer ->
                        AnswerOptionCard(
                            label = ('A' + index).toString(),
                            text = answer.text,
                            onClick = { controller.answer(answer.id, timer.state.value.elapsedMs) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundHeader(
    counter: String,
    timer: TimerState,
    affinityFraction: Float,
    stageName: String,
) {
    // Cuando queda poco tiempo, el temporizador cambia a rojo (color de acción).
    val lowTime = timer.secondsLeft <= LOW_TIME_SECONDS || timer.fractionLeft <= LOW_TIME_FRACTION
    val timerColor = if (lowTime) IslaColors.Action else IslaColors.Primary

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderText(counter)
            Text(
                text = "${timer.secondsLeft}s",
                color = if (lowTime) IslaColors.Action else IslaColors.OnDark,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
        }
        PixelBar(timer.fractionLeft, timerColor)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AffinityHearts(fraction = affinityFraction)
            HeaderText(stageName.uppercase())
        }
    }
}

@Composable
private fun HeaderText(text: String) {
    Text(
        text = text,
        color = IslaColors.OnDark,
        fontFamily = PixelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
    )
}

@Composable
private fun PixelBar(fraction: Float, fill: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .background(fill, RectangleShape),
        )
    }
}

@Composable
private fun DeltaChip(feedback: AnswerFeedback) {
    val delta = feedback.appliedDelta
    val color = when {
        delta > 0 -> IslaColors.Primary
        delta < 0 -> IslaColors.Action
        else -> IslaColors.Ink
    }
    val sign = if (delta > 0) "+" else ""
    Box(
        Modifier
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(color, RectangleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = "$sign$delta  ·  ${feedback.motiveLabel ?: RoundStrings.NoAnswer}",
            color = IslaColors.OnDark,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
    }
}

/** Sprite provisional hasta tener el arte de los personajes (IP-33). */
@Composable
private fun SpritePlaceholder(speaker: Speaker) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 170.dp)
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(speaker.accent(), RectangleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = speaker.displayName.uppercase(),
            color = speaker.onAccent(),
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
    }
}
