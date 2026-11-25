package com.example.videojuego.ui.world

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.example.videojuego.diary.DiaryEntry
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

object DiaryStrings {
    const val Title = "DIARIO DE LA ISLA"
    const val Close = "CERRAR"
    const val Open = "DIARIO"
    const val Unknown = "Aún no la conoces lo suficiente. Habla con ella en la fogata."
    const val Learning = "Lo que aprendiste"
    const val Boundaries = "Sus límites"
    fun stage(name: String) = "Vínculo: $name"
    fun score(n: Int) = "Minijuego: $n pts"
}

/** Diario del menú de pausa: un resumen de cada personaje y cómo va la relación. */
@Composable
fun DiaryPanel(entries: List<DiaryEntry>, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(DiaryStrings.Title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            entries.forEach { e ->
                Column(
                    Modifier.fillMaxWidth().border(2.dp, IslaColors.Ink, RectangleShape).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("${e.name} · ${e.occupation}", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(DiaryStrings.stage(e.stageName), color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp)
                    Box(Modifier.fillMaxWidth().height(8.dp).border(1.dp, IslaColors.Ink, RectangleShape)) {
                        Box(Modifier.fillMaxWidth(e.affinityFraction).height(8.dp).background(Color(0xFFE0566B)))
                    }
                    if (!e.met) {
                        Text(DiaryStrings.Unknown, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp, lineHeight = 15.sp)
                    } else {
                        Text("${DiaryStrings.Learning}: ${e.learning.orEmpty()}", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp, lineHeight = 15.sp)
                        e.boundaries.forEach {
                            Text("• $it", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                    e.minigameScore?.let { Text(DiaryStrings.score(it), color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 10.sp) }
                }
            }
        }
        ActionButton(text = DiaryStrings.Close, onClick = onClose)
    }
}
