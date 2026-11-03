package com.example.videojuego.ui.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.credits.CreditsData
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

/** Créditos del juego. El contenido se edita en CreditsData. */
@Composable
fun CreditsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(IslaColors.Primary).safeDrawingPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("CRÉDITOS", color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(CreditsData.GAME, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontSize = 14.sp)
            CreditsData.sections.forEach { section ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(3.dp, IslaColors.Ink, RectangleShape)
                        .background(IslaColors.Paper, RectangleShape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(section.title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    section.lines.forEach {
                        Text(it, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
            ActionButton(text = "VOLVER", onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
