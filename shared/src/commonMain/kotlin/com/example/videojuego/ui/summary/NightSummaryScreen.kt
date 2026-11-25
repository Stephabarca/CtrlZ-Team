package com.example.videojuego.ui.summary

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
            Heading(summary)
            AffinityCard(summary)
            QuestionsCard(summary)
            summary.minigame?.let { MinigameCard(it) }
            Card(SummaryStrings.Learning) { Body(summary.learning) }
            if (summary.improvements.isNotEmpty()) {
                Card(SummaryStrings.Improve) {
                    summary.improvements.forEach { Body("• $it") }
                }
            }
            ActionButton(text = SummaryStrings.Continue, onClick = onContinue, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
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
        AffinityHearts(fraction = summary.affinityFraction)
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
            SummaryStrings.score(review.score),
            color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp,
        )
        Text(
            "${SummaryStrings.Affinity} ${SummaryStrings.delta(review.affinityGranted)}",
            color = if (review.affinityGranted >= 0) GOOD else BAD,
            fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
        )
        val s = review.stats
        Body(SummaryStrings.catches(s.perfect, s.good, s.weak, s.lost, s.early))
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
