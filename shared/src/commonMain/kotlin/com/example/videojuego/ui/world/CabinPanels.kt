package com.example.videojuego.ui.world

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.Strings
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.world.ItemType
import com.example.videojuego.world.WorldController
import com.example.videojuego.world.WorldState

@Composable
private fun PanelFrame(title: String?, modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (title != null) {
            Text(title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        content()
    }
}

/** Cofre de la cabaña: guardar lo recolectado y volver a sacarlo. */
@Composable
fun ChestPanel(
    state: WorldState,
    controller: WorldController,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PanelFrame(WorldStrings.ChestTitle, modifier) {
        ItemType.entries.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${item.label}:  ${WorldStrings.Bag} ${state.count(item)}  ·  ${WorldStrings.InChest} ${state.inChest(item)}",
                    color = IslaColors.Ink,
                    fontFamily = PixelFontFamily,
                    fontSize = 12.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (controller.canDeposit(item)) {
                        ActionButton(text = WorldStrings.Store, onClick = { controller.deposit(item) })
                    }
                    if (controller.canWithdraw(item)) {
                        ActionButton(text = WorldStrings.Take, onClick = { controller.withdraw(item) })
                    }
                }
            }
        }
        if (ItemType.entries.any { controller.canDeposit(it) }) {
            ActionButton(text = WorldStrings.StoreAll, onClick = { controller.depositAll() })
        }
        ActionButton(text = WorldStrings.Close, onClick = onClose)
    }
}

/** Texto corto (por ejemplo, lo que cuenta una maleta). */
@Composable
fun InfoPanel(text: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    PanelFrame(null, modifier) {
        Text(text, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 13.sp, lineHeight = 19.sp)
        ActionButton(text = Strings.Continue, onClick = onClose)
    }
}

/** Pantalla oscura al dormir: primero "Zzz..." y luego "Partida guardada". [stage] es 1 (durmiendo) o 2 (guardada). */
@Composable
fun SleepOverlay(stage: Int, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color(0xF00B1030)), contentAlignment = Alignment.Center) {
        Text(
            if (stage == 1) WorldStrings.Sleeping else WorldStrings.Saved,
            color = if (stage == 1) Color.White else Color(0xFFCAD988),
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}
