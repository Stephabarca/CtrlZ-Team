package com.example.videojuego.ui.fishing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.minigame.fishing.CatchResult
import com.example.videojuego.minigame.fishing.FishingGame
import com.example.videojuego.minigame.fishing.FishingPhase
import com.example.videojuego.minigame.fishing.FishingState
import com.example.videojuego.minigame.fishing.FishingStats
import com.example.videojuego.timer.frameDelta
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.world.formatClock
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private const val MAX_FRAME_MS = 50L
private val PULL_KEYS = setOf(Key.E, Key.Enter, Key.Spacebar)

private val SKY = Color(0xFFF6C99A)
private val SKY_TOP = Color(0xFFF2A6A0)
private val SUN = Color(0xFFFFE08A)
private val SEA_A = Color(0xFF4AA3D8)
private val SEA_B = Color(0xFF3E94C9)
private val SEA_FOAM = Color(0xFFBFE6F5)
private val SAND = Color(0xFFF1D9A0)
private val SAND_DARK = Color(0xFFE3C687)
private val BAR_BASE = Color(0xFF3A3550)
private val BAR_WEAK = Color(0xFF7C7396)
private val BAR_GOOD = Color(0xFF7FB069)
private val BAR_PERFECT = Color(0xFFFFD84D)

/**
 * Minijuego de pesca (tap_timing) de la noche 1. Muestra las instrucciones, juega [durationSec] segundos y
 * llama a [onFinished] con el resultado. Aplicar la afinidad (ProgressRules.recordMinigame) es de quien llama.
 *
 * Controles: botón JALAR (o Espacio / E / Enter en computador).
 */
@Composable
fun FishingScreen(
    characterName: String,
    characterColor: Color,
    durationSec: Int,
    onFinished: (FishingStats) -> Unit,
    modifier: Modifier = Modifier,
) {
    val game = remember { FishingGame(durationSec * 1000L) }
    val state by game.state.collectAsState()
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(game) {
        var last: Long? = null
        while (true) {
            val now = withFrameMillis { it }
            val previous = last
            if (previous != null) game.update(frameDelta(previous, now, MAX_FRAME_MS))
            last = now
        }
    }
    LaunchedEffect(state.phase == FishingPhase.FINISHED) {
        if (state.phase == FishingPhase.FINISHED) {
            delay(1400)
            currentOnFinished(game.stats())
        }
    }

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focus.requestFocus()
        } catch (ignored: IllegalStateException) {
            // El nodo todavía no está listo; el botón táctil sigue funcionando.
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(SKY)
            .onKeyEvent { event ->
                if (event.key in PULL_KEYS && event.type == KeyEventType.KeyDown) {
                    game.tap()
                    true
                } else {
                    false
                }
            }
            .focusRequester(focus)
            .focusable(),
    ) {
        Canvas(Modifier.fillMaxSize()) { drawScene(state, characterColor) }

        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Hud(state)

            when (state.phase) {
                FishingPhase.READY -> IntroPanel(characterName, onStart = game::start)
                else -> Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Banner(state)
                    CatchBar(state)
                    ActionButton(text = FishingStrings.Pull, onClick = game::tap)
                }
            }
        }
    }
}

@Composable
private fun Hud(state: FishingState) {
    Box(
        Modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        val fish = state.results.count { it == CatchResult.PERFECT || it == CatchResult.GOOD || it == CatchResult.WEAK }
        Text(
            "${FishingStrings.Time} ${formatClock(state.remainingMs)}    ${FishingStrings.Fish} $fish",
            color = IslaColors.Ink,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun IntroPanel(characterName: String, onStart: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(FishingStrings.Title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(FishingStrings.intro(characterName), color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 13.sp, lineHeight = 18.sp)
        FishingStrings.Instructions.forEach {
            Text("• $it", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)
        }
        ActionButton(text = FishingStrings.Start, onClick = onStart)
    }
}

@Composable
private fun Banner(state: FishingState) {
    val text = when (state.phase) {
        FishingPhase.WAITING -> FishingStrings.Waiting
        FishingPhase.BITING -> FishingStrings.Bite
        FishingPhase.RESULT -> state.lastResult?.let { FishingStrings.result(it) } ?: ""
        FishingPhase.FINISHED -> FishingStrings.TimeUp
        FishingPhase.READY -> ""
    }
    Box(
        Modifier
            .background(Color(0xCC1F2A2C), RectangleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            color = if (state.phase == FishingPhase.BITING) BAR_PERFECT else Color.White,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** Barra con las zonas (débil, buena, perfecta) y la marca que va y viene mientras pica. */
@Composable
private fun CatchBar(state: FishingState) {
    val active = state.phase == FishingPhase.BITING
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(32.dp)
            .border(3.dp, IslaColors.Ink, RectangleShape),
    ) {
        val w = size.width
        val h = size.height
        drawRect(BAR_BASE, Offset.Zero, size)
        drawRect(BAR_WEAK, Offset(0f, 0f), Size(w, h))
        val good = FishingGame.GOOD_HALF_WIDTH
        val perfect = FishingGame.PERFECT_HALF_WIDTH
        drawRect(BAR_GOOD, Offset(w * (0.5f - good), 0f), Size(w * good * 2, h))
        drawRect(BAR_PERFECT, Offset(w * (0.5f - perfect), 0f), Size(w * perfect * 2, h))
        if (active) {
            val x = w * state.cursor
            drawRect(Color.White, Offset(x - 4f, -2f), Size(8f, h + 4f))
            drawRect(IslaColors.Ink, Offset(x - 1f, 0f), Size(2f, h))
        } else {
            drawRect(Color(0x66000000), Offset.Zero, size)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Dibujo. Arte provisional con rectángulos; se reemplaza por sprites reales (IP-32/33).
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drawScene(state: FishingState, personColor: Color) {
    val w = size.width
    val h = size.height
    val unit = (w / 60f).coerceAtLeast(4f)
    val horizon = h * 0.34f
    val shore = h * 0.62f

    drawRect(SKY_TOP, Offset.Zero, Size(w, horizon * 0.55f))
    drawRect(SKY, Offset(0f, horizon * 0.55f), Size(w, horizon * 0.45f + 1f))
    drawCircle(SUN, unit * 5f, Offset(w * 0.78f, horizon * 0.8f))

    // Mar con franjas que se mueven.
    val timeSteps = (state.remainingMs / 600L)
    var y = horizon
    var row = 0
    while (y < shore) {
        val color = if ((row + timeSteps).mod(2L) == 0L) SEA_A else SEA_B
        drawRect(color, Offset(0f, y), Size(w, unit * 3f + 1f))
        if (row % 3 == 0) drawRect(SEA_FOAM, Offset(w * ((row * 37 % 80) / 100f), y + unit), Size(unit * 4f, unit * 0.6f))
        y += unit * 3f
        row++
    }
    drawRect(SAND, Offset(0f, shore), Size(w, h - shore))
    drawRect(SAND_DARK, Offset(0f, shore), Size(w, unit * 0.8f))

    // Personaje en la orilla, a la izquierda.
    val px = w * 0.2f
    val feet = shore + unit * 6f
    drawRect(Color(0x33000000), Offset(px - unit * 4f, feet - unit), Size(unit * 8f, unit * 1.5f))
    drawRect(Color(0xFF2B2B3A), Offset(px - unit * 2f, feet - unit * 4f), Size(unit * 4f, unit * 4f))
    drawRect(personColor, Offset(px - unit * 3f, feet - unit * 10f), Size(unit * 6f, unit * 6f))
    drawRect(Color(0xFFF2C9A0), Offset(px - unit * 2f, feet - unit * 15f), Size(unit * 4f, unit * 5f))
    drawRect(Color(0xFF5A3B2A), Offset(px - unit * 2f, feet - unit * 16f), Size(unit * 4f, unit * 2f))

    // Caña y línea hasta el corcho.
    val bobberX = w * 0.62f
    val bobPhase = (state.biteElapsedMs.toFloat() / 90f)
    val bobberY = when (state.phase) {
        FishingPhase.WAITING -> horizon + (shore - horizon) * 0.55f + sin(bobPhase * 0.2f) * unit * 0.4f
        FishingPhase.BITING -> horizon + (shore - horizon) * 0.55f + unit * 1.6f + sin(bobPhase * PI.toFloat()) * unit * 0.8f
        else -> horizon + (shore - horizon) * 0.55f
    }
    val rodTip = Offset(px + unit * 9f, feet - unit * 24f)
    drawLine(Color(0xFF8B5E3C), Offset(px + unit * 3f, feet - unit * 9f), rodTip, strokeWidth = unit * 0.7f)
    drawLine(Color(0xFFEFEFEF), rodTip, Offset(bobberX, bobberY), strokeWidth = unit * 0.25f)
    drawCircle(Color.White, unit * 1.4f, Offset(bobberX, bobberY))
    drawCircle(Color(0xFFD9534F), unit * 1.0f, Offset(bobberX, bobberY - unit * 0.4f))

    // Ondas cuando pica; pez saltando al capturar.
    if (state.phase == FishingPhase.BITING) {
        val ring = (state.biteElapsedMs % 600L) / 600f
        drawCircle(SEA_FOAM, unit * (2f + ring * 4f), Offset(bobberX, bobberY + unit), style = androidx.compose.ui.graphics.drawscope.Stroke(unit * 0.4f))
    }
    val last = state.lastResult
    if (state.phase == FishingPhase.RESULT && last != null && last != CatchResult.LOST && last != CatchResult.EARLY) {
        val fishSize = if (last == CatchResult.WEAK) 2f else 3.5f
        drawRect(Color(0xFFF28C28), Offset(bobberX - unit * fishSize, bobberY - unit * 8f), Size(unit * fishSize * 2f, unit * fishSize))
        drawRect(Color(0xFFFFD84D), Offset(bobberX - unit * fishSize, bobberY - unit * 8f), Size(unit * fishSize * 0.8f, unit * fishSize))
    }
}
