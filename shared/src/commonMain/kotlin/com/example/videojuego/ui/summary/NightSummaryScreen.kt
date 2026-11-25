package com.example.videojuego.ui.summary

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.summary.MinigameReview
import com.example.videojuego.summary.NightSummary
import com.example.videojuego.summary.QuestionReview
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.components.AffinityHearts
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import kotlinx.coroutines.delay

private val GOOD = Color(0xFF3F8F52)
private val BAD = Color(0xFFC9423F)

/**
 * Resumen de retroalimentación al terminar la noche: cómo salió la ronda de preguntas (qué respondiste y qué
 * demuestra cada respuesta), cómo salió el minijuego y qué conviene mejorar.
 */
@Composable
fun NightSummaryScreen(
    summary: NightSummary,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(IslaColors.Primary).safeDrawingPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // El margen derecho deja lugar al sello.
            Appear(0) { Box(Modifier.padding(end = 96.dp)) { Heading(summary) } }
            Appear(1) { AffinityCard(summary) }
            Appear(2) { QuestionsCard(summary) }
            summary.minigame?.let { Appear(3) { MinigameCard(it) } }
            Appear(4) { Card(SummaryStrings.Learning) { Body(summary.learning) } }
            if (summary.improvements.isNotEmpty()) {
                Appear(5) {
                    Card(SummaryStrings.Improve) {
                        summary.improvements.forEach { Body("• $it") }
                    }
                }
            }
            ActionButton(text = SummaryStrings.Continue, onClick = onContinue, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        NightStamp(modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 12.dp))
    }
}

/** Hace que cada tarjeta entre con un pequeño retraso: aparece y sube un poquito, una detrás de otra. */
@Composable
private fun Appear(index: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 130L)
        progress.animateTo(1f, tween(380))
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 28f
        },
    ) { content() }
}

/** Sello "NOCHE SUPERADA" que cae y rebota con un giro. */
@Composable
private fun NightStamp(modifier: Modifier = Modifier) {
    val scale = remember { Animatable(2.6f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(1000)
        alpha.snapTo(1f)
        scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
    }
    if (alpha.value > 0f) {
        Text(
            text = SummaryStrings.Stamp,
            modifier = modifier
                .graphicsLayer {
                    rotationZ = -10f
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value * 0.95f
                }
                .border(3.dp, GOOD, RectangleShape)
                .background(Color(0xE6F5F0E6), RectangleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = GOOD,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** Cuenta de 0 hasta [target] en poco más de un segundo. */
@Composable
private fun rememberCountUp(target: Int, delayMs: Long = 600L): Int {
    val value = remember { Animatable(0f) }
    LaunchedEffect(target) {
        value.snapTo(0f)
        delay(delayMs)
        value.animateTo(target.toFloat(), tween(1200))
    }
    return value.value.toInt()
}

@Composable
private fun Heading(summary: NightSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(SummaryStrings.Title, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(summary.nightTitle, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontSize = 13.sp)
        Text(summary.headline, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun AffinityCard(summary: NightSummary) {
    Card("${SummaryStrings.Affinity}: ${summary.characterName}") {
        // Los corazones empiezan como estaban al comenzar la noche y se llenan (o se vacían) hasta el valor final.
        var hearts by remember { mutableStateOf(summary.affinityBeforeFraction) }
        LaunchedEffect(summary) {
            delay(700)
            hearts = summary.affinityFraction
        }
        AffinityHearts(fraction = hearts)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                SummaryStrings.change(summary.affinityBefore, summary.affinityAfter),
                color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            )
            Text(
                SummaryStrings.delta(summary.totalAffinityChange),
                color = if (summary.totalAffinityChange >= 0) GOOD else BAD,
                fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            )
        }
        Body(summary.stageName)
    }
}

@Composable
private fun QuestionsCard(summary: NightSummary) {
    Card(SummaryStrings.Questions) {
        Body(SummaryStrings.healthyAnswers(summary.healthyCount, summary.questions.size))
        summary.averageSeconds?.let { Body(SummaryStrings.averageTime(it)) }
        summary.questions.forEach { QuestionRow(it) }
    }
}

@Composable
private fun QuestionRow(review: QuestionReview) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(2.dp, IslaColors.Ink, RectangleShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(review.prompt, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
        Body(review.chosenText ?: SummaryStrings.NoAnswer)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                SummaryStrings.delta(review.affinityDelta),
                color = if (review.affinityDelta >= 0) GOOD else BAD,
                fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
            )
            review.motiveLabel?.let { label ->
                val healthy = review.healthy == true
                Text(
                    "$label · ${if (healthy) SummaryStrings.Healthy else SummaryStrings.Risk}",
                    color = if (healthy) GOOD else BAD,
                    fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                )
            }
        }
        Body(review.feedback)
    }
}

@Composable
private fun MinigameCard(review: MinigameReview) {
    Card("${SummaryStrings.Minigame}: ${review.name}") {
        Text(
            SummaryStrings.score(rememberCountUp(review.score)),
            color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
        )
        Text(
            "${SummaryStrings.Affinity} ${SummaryStrings.delta(review.affinityGranted)}",
            color = if (review.affinityGranted >= 0) GOOD else BAD,
            fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
        )
        review.fishingStats?.let { s ->
            Body(SummaryStrings.catches(s.perfect, s.good, s.weak, s.lost, s.early))
        }
        review.building?.let { b ->
            Body(SummaryStrings.building(b.rounds, b.correct, b.wrong + b.decoys, b.missed, b.bestStreak))
        }
        review.tips.forEach { Body("• $it") }
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        content()
    }
}

@Composable
private fun Body(text: String) {
    Text(text, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)
}
