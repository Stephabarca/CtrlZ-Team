package com.example.videojuego.ui.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.menu.MenuInfo
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.components.RopedLogs
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import kotlin.math.floor

private val MENU_BUTTON_WIDTH = 260.dp

private val SKY_TOP = Color(0xFFF2A6A0)
private val SKY_LOW = Color(0xFFF6C99A)
private val SUN = Color(0xFFFFE08A)
private val SEA_A = Color(0xFF4AA3D8)
private val SEA_B = Color(0xFF3E94C9)
private val SEA_FOAM = Color(0xFFBFE6F5)
private val SAND = Color(0xFFF1D9A0)
private val WOOD = Color(0xFF8B5E3C)
private val LEAVES = Color(0xFF2F6B3A)
private val LEAVES_LIGHT = Color(0xFF3E8A4A)

/**
 * Menú de inicio: título del juego, CONTINUAR (solo si hay partida guardada) y NUEVA PARTIDA.
 *
 * @param info lo que se sabe de la partida guardada; null mientras se lee (muestra "CARGANDO...").
 * @param onContinue carga la partida guardada.
 * @param onNewGame empieza de cero. Si había una partida, antes se pide confirmación.
 * @param onSettings abre Ajustes.
 * @param onCredits abre Créditos.
 */
@Composable
fun MainMenuScreen(
    info: MenuInfo?,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
    onSettings: () -> Unit = {},
    onCredits: () -> Unit = {},
) {
    var confirming by remember { mutableStateOf(false) }
    var time by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) time = withFrameMillis { it }
    }

    Box(modifier.fillMaxSize().background(SEA_A)) {
        Canvas(Modifier.fillMaxSize()) { drawBackdrop(time) }

        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            TitleBlock(Modifier.padding(top = 48.dp))

            Column(
                Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when {
                    info == null -> StatusLabel(MenuStrings.Loading)
                    confirming -> ConfirmPanel(
                        onYes = {
                            confirming = false
                            onNewGame()
                        },
                        onNo = { confirming = false },
                    )
                    else -> {
                        if (info.corrupt) NoticePanel(MenuStrings.CorruptTitle, MenuStrings.CorruptBody)
                        if (info.canContinue) info.description?.let { StatusLabel(it) }
                        RopedLogs(
                            buildList<@Composable () -> Unit> {
                                if (info.canContinue) add { ActionButton(text = MenuStrings.Continue, onClick = onContinue, minWidth = MENU_BUTTON_WIDTH) }
                                add {
                                    ActionButton(
                                        text = MenuStrings.NewGame,
                                        onClick = { if (info.newGameNeedsConfirmation) confirming = true else onNewGame() },
                                        minWidth = MENU_BUTTON_WIDTH,
                                    )
                                }
                                add { ActionButton(text = MenuStrings.Settings, onClick = onSettings, minWidth = MENU_BUTTON_WIDTH) }
                                add { ActionButton(text = MenuStrings.Credits, onClick = onCredits, minWidth = MENU_BUTTON_WIDTH) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleBlock(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            MenuStrings.Title,
            color = Color.White,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = androidx.compose.ui.graphics.Shadow(IslaColors.Ink, Offset(5f, 5f), 0f)),
        )
        Box(
            Modifier
                .background(Color(0xCC1F2A2C), RectangleShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                MenuStrings.Subtitle,
                color = Color.White,
                fontFamily = PixelFontFamily,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StatusLabel(text: String) {
    Box(Modifier.background(Color(0xCC1F2A2C), RectangleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(text, color = Color.White, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun NoticePanel(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(body, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun ConfirmPanel(onYes: () -> Unit, onNo: () -> Unit) {
    NoticePanel(MenuStrings.ConfirmTitle, MenuStrings.ConfirmBody)
    RopedLogs(
        listOf<@Composable () -> Unit>(
            { ActionButton(text = MenuStrings.ConfirmYes, onClick = onYes, minWidth = MENU_BUTTON_WIDTH) },
            { ActionButton(text = MenuStrings.ConfirmNo, onClick = onNo, minWidth = MENU_BUTTON_WIDTH) },
        ),
    )
}

// ---------------------------------------------------------------------------------------------
// Fondo: atardecer en la isla. Arte provisional con rectángulos; se reemplaza por arte real (IP-32/33).
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drawBackdrop(timeMs: Long) {
    val w = size.width
    val h = size.height
    val unit = (w / 48f).coerceAtLeast(4f)
    val horizon = h * 0.42f
    val shore = h * 0.74f

    drawRect(SKY_TOP, Offset.Zero, Size(w, horizon * 0.55f))
    drawRect(SKY_LOW, Offset(0f, horizon * 0.55f), Size(w, horizon * 0.45f + 1f))
    drawCircle(SUN, unit * 6f, Offset(w * 0.72f, horizon - unit * 2f))

    // Mar con franjas que cambian de tono despacio.
    val step = timeMs / 700L
    var y = horizon
    var row = 0
    while (y < shore) {
        drawRect(if ((row + step).mod(2L) == 0L) SEA_A else SEA_B, Offset(0f, y), Size(w, unit * 3f + 1f))
        if (row % 3 == 0) {
            drawRect(SEA_FOAM, Offset(w * (((row * 37 + step * 3) % 80) / 100f), y + unit), Size(unit * 4f, unit * 0.6f))
        }
        y += unit * 3f
        row++
    }
    drawRect(SAND, Offset(0f, shore), Size(w, h - shore))

    // Palmera a la izquierda.
    val baseX = floor(w * 0.16f)
    val baseY = shore + unit * 2f
    drawRect(WOOD, Offset(baseX, baseY - unit * 16f), Size(unit * 2f, unit * 16f))
    drawRect(WOOD, Offset(baseX + unit * 1.2f, baseY - unit * 20f), Size(unit * 1.6f, unit * 5f))
    val top = baseY - unit * 21f
    drawRect(LEAVES, Offset(baseX - unit * 5f, top), Size(unit * 14f, unit * 2.5f))
    drawRect(LEAVES, Offset(baseX - unit * 8f, top + unit * 2.5f), Size(unit * 20f, unit * 2.5f))
    drawRect(LEAVES_LIGHT, Offset(baseX - unit * 3f, top + unit * 0.5f), Size(unit * 4f, unit))
}
