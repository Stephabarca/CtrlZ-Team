package com.example.videojuego.ui.world

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

/**
 * Menú de pausa del mapa: seguir jugando, ajustes y volver al menú principal.
 * Volver al menú pide confirmación porque se pierde lo avanzado desde la última vez que se durmió en la cama.
 */
@Composable
fun PausePanel(
    onResume: () -> Unit,
    onSettings: (() -> Unit)?,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirming by remember { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!confirming) {
            Text(WorldStrings.PauseTitle, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            ActionButton(text = WorldStrings.Resume, onClick = onResume)
            if (onSettings != null) ActionButton(text = WorldStrings.Settings, onClick = onSettings)
            ActionButton(text = WorldStrings.BackToMenu, onClick = { confirming = true })
        } else {
            Text(WorldStrings.LeaveTitle, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(WorldStrings.LeaveBody, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)
            ActionButton(text = WorldStrings.LeaveYes, onClick = onBackToMenu)
            ActionButton(text = WorldStrings.LeaveNo, onClick = { confirming = false })
        }
    }
}
