package com.example.videojuego.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.videojuego.report.RelationshipReport
import com.example.videojuego.report.Verdict
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

object ReportStrings {
    const val Title = "TU FORMA DE RELACIONARTE"
    const val Strengths = "LO QUE HICISTE BIEN"
    const val Risks = "SEÑALES DE RIESGO"
    const val Tips = "PARA MEJORAR"
    const val None = "Nada que destacar."
    const val Continue = "CONTINUAR"

    fun verdictTitle(v: Verdict) = when (v) {
        Verdict.EXCELENTE -> "Relaciones sanas"
        Verdict.BUENO -> "Vas por buen camino"
        Verdict.A_MEJORAR -> "Hay cosas por trabajar"
    }

    fun verdictText(v: Verdict) = when (v) {
        Verdict.EXCELENTE -> "Escuchaste, pusiste límites y respetaste a los demás. Así se construyen vínculos que duran."
        Verdict.BUENO -> "Acertaste en muchas decisiones, pero a veces te ganaron los celos o el orgullo. Ya sabes cómo corregirlo."
        Verdict.A_MEJORAR -> "Varias respuestas mostraron conductas de riesgo. Lo importante es notarlo: se puede aprender a hacerlo distinto."
    }

    fun nightLine(title: String, healthy: Int, risky: Int, timedOut: Int) =
        "$title: $healthy sanas · $risky de riesgo" + if (timedOut > 0) " · $timedOut sin responder" else ""
}

/** Resumen de cómo se relacionó el jugador, antes de los créditos. */
@Composable
fun ReportScreen(report: RelationshipReport, onContinue: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(IslaColors.Paper).safeDrawingPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(ReportStrings.Title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(ReportStrings.verdictTitle(report.verdict), color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(ReportStrings.verdictText(report.verdict), color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)

            // Barra de respuestas sanas por noche.
            report.nights.forEach { n ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ReportStrings.nightLine(n.title.substringAfter(":", n.title).trim().ifEmpty { n.title }, n.healthy, n.risky, n.timedOut),
                        color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp)
                    Row(Modifier.fillMaxWidth().height(12.dp).border(2.dp, IslaColors.Ink, RectangleShape)) {
                        if (n.healthy > 0) Box(Modifier.weight(n.healthy.toFloat()).fillMaxSize().background(Color(0xFF4FA36A)))
                        if (n.risky > 0) Box(Modifier.weight(n.risky.toFloat()).fillMaxSize().background(Color(0xFFD9534F)))
                        if (n.timedOut > 0) Box(Modifier.weight(n.timedOut.toFloat()).fillMaxSize().background(Color(0xFF9AA0A6)))
                        if (n.total == 0) Box(Modifier.weight(1f).fillMaxSize())
                    }
                }
            }

            Section(ReportStrings.Strengths, report.strengths)
            Section(ReportStrings.Risks, report.risks)
            Section(ReportStrings.Tips, report.tips)

            ActionButton(text = ReportStrings.Continue, onClick = onContinue)
        }
    }
}

@Composable
private fun Section(title: String, items: List<String>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        if (items.isEmpty()) Text(ReportStrings.None, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp)
        items.forEach { Text("• $it", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp, lineHeight = 16.sp) }
    }
}
