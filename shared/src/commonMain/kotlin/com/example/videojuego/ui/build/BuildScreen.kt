package com.example.videojuego.ui.build

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.audio.Sfx
import com.example.videojuego.audio.SoundEffect
import com.example.videojuego.minigame.build.BuildGame
import com.example.videojuego.minigame.build.BuildPhase
import com.example.videojuego.minigame.build.BuildState
import com.example.videojuego.minigame.build.BuildStats
import com.example.videojuego.minigame.build.Piece
import com.example.videojuego.minigame.build.StepResult
import com.example.videojuego.timer.frameDelta
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.world.formatClock
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import videojuego.shared.generated.resources.Res
import videojuego.shared.generated.resources.*

private const val MAX_FRAME_MS = 50L
private val NUMBER_KEYS = listOf(Key.One, Key.Two, Key.Three, Key.Four, Key.Five, Key.Six, Key.Seven, Key.Eight)

private val NIGHT_TOP = Color(0xFF1B2440)
private val NIGHT_LOW = Color(0xFF2E3B63)
private val FOREST = Color(0xFF16301F)
private val FOREST_LIGHT = Color(0xFF1F4128)
private val GROUND = Color(0xFF3A2D22)
private val GROUND_LIGHT = Color(0xFF4A3A2C)
private val FLAME_A = Color(0xFFFFD84D)
private val FLAME_B = Color(0xFFF28C28)
private val FLAME_C = Color(0xFFD9534F)
private val GOOD = Color(0xFF7FB069)
private val BAD = Color(0xFFD9534F)

/** Fondo claro de las casillas de piezas, para que los dibujos se vean bien. */
private val CHIP_BG = Color(0xFFF1E4C3)
private val CHIP_CURRENT_BG = Color(0xFFFFF3C9)

/** Poner en true para mostrar también el nombre debajo de cada dibujo en la bandeja. */
private const val SHOW_NAMES = false

/**
 * Minijuego "Armar la fogata" (construir juntos) de la noche 2. Muestra las instrucciones, juega [durationSec]
 * segundos y llama a [onFinished] con el resultado. Aplicar la afinidad (ProgressRules.recordMinigame) es de quien llama.
 *
 * [level] es la dificultad de la noche (1 a 3). Controles: tocar las piezas, o teclas 1 a 8 en computador.
 */
@Composable
fun BuildScreen(
    characterName: String,
    characterColor: Color,
    durationSec: Int,
    onFinished: (BuildStats) -> Unit,
    modifier: Modifier = Modifier,
    level: Int = 2,
    /** Id del personaje ("valentina", "lucas" o "lila") para dibujar su imagen. */
    characterId: String? = null,
) {
    // Mismas imágenes que usa el mapa (WorldScreen). Si cambias los nombres allá, cámbialos aquí.
    val valentinaPainter = painterResource(Res.drawable.valentina_oliva_4)
    val lucasPainter = painterResource(Res.drawable.lucas_ambar_4)
    val lilaPainter = painterResource(Res.drawable.lila_ariza_4)
    val painter: Painter? = when (characterId) {
        "valentina" -> valentinaPainter
        "lucas" -> lucasPainter
        "lila" -> lilaPainter
        else -> null
    }
    val game = remember { BuildGame(durationSec * 1000L, level) }
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
    // Sonidos: cada contador solo sube, así que suenan una vez por pieza aunque se repita el mismo resultado.
    // Pieza correcta: golpe de madera.
    LaunchedEffect(state.correct) {
        if (state.correct > 0) Sfx.play(SoundEffect.CLICK)
    }
    // Pieza equivocada o trampa: error.
    LaunchedEffect(state.wrong + state.decoys) {
        if (state.wrong + state.decoys > 0) Sfx.play(SoundEffect.FAIL)
    }
    // Se acabó el tiempo de una pieza y Lucas la puso solo.
    LaunchedEffect(state.missed) {
        if (state.missed > 0) Sfx.play(SoundEffect.NEGATIVE)
    }
    // Fogata completa (ronda cerrada por el jugador).
    LaunchedEffect(state.roundsCompleted) {
        if (state.roundsCompleted > 0) Sfx.play(SoundEffect.SUCCESS)
    }

    LaunchedEffect(state.phase == BuildPhase.FINISHED) {
        if (state.phase == BuildPhase.FINISHED) {
            delay(1400)
            currentOnFinished(game.stats())
        }
    }

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focus.requestFocus()
        } catch (ignored: IllegalStateException) {
            // El nodo todavía no está listo; los botones táctiles siguen funcionando.
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(NIGHT_TOP)
            .onKeyEvent { event ->
                val index = NUMBER_KEYS.indexOf(event.key)
                if (index >= 0 && event.type == KeyEventType.KeyDown) {
                    game.tapIndex(index)
                    true
                } else {
                    false
                }
            }
            .focusRequester(focus)
            .focusable(),
    ) {
        Canvas(Modifier.fillMaxSize()) { drawScene(state, characterColor, painter) }

        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Hud(state)

            if (state.phase == BuildPhase.READY) {
                IntroPanel(characterName, onStart = game::start)
            } else {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Banner(state)
                    RequestPanel(characterName, state)
                    StepTimer(state)
                    Tray(state, enabled = state.phase == BuildPhase.PLAYING, onPick = game::tap)
                }
            }
        }
    }
}

@Composable
private fun Hud(state: BuildState) {
    Box(
        Modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            "${BuildStrings.Time} ${formatClock(state.remainingMs)}   ${BuildStrings.Round} ${state.round}   ${BuildStrings.Points} ${state.points}",
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
        Text(BuildStrings.Title, color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(BuildStrings.intro(characterName), color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 13.sp, lineHeight = 18.sp)
        BuildStrings.Instructions.forEach {
            Text("• $it", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp, lineHeight = 17.sp)
        }
        ActionButton(text = BuildStrings.Start, onClick = onStart)
    }
}

@Composable
private fun Banner(state: BuildState) {
    val text = when (state.phase) {
        BuildPhase.ROUND_DONE -> BuildStrings.RoundDone
        BuildPhase.FINISHED -> BuildStrings.TimeUp
        BuildPhase.PLAYING -> state.lastResult?.let { BuildStrings.result(it) } ?: BuildStrings.Ready
        BuildPhase.READY -> ""
    }
    val color = when {
        state.phase == BuildPhase.ROUND_DONE -> FLAME_A
        state.phase == BuildPhase.PLAYING && (state.lastResult == StepResult.WRONG || state.lastResult == StepResult.DECOY) -> BAD
        state.phase == BuildPhase.PLAYING && state.lastResult == StepResult.CORRECT -> GOOD
        else -> Color.White
    }
    Box(
        Modifier
            .background(Color(0xCC10151F), RectangleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, color = color, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

/** La secuencia completa que pide el personaje: las ya puestas se apagan y la actual se resalta. */
@Composable
private fun RequestPanel(characterName: String, state: BuildState) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(3.dp, IslaColors.Ink, RectangleShape)
            .background(IslaColors.Paper, RectangleShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            BuildStrings.asks(characterName.uppercase()),
            color = IslaColors.Action, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp,
        )
        state.sequence.withIndex().chunked(4).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowItems.forEach { (index, piece) ->
                    val done = index < state.step
                    val current = index == state.step && state.phase == BuildPhase.PLAYING
                    Box(
                        Modifier
                            .weight(1f)
                            .border(if (current) 4.dp else 2.dp, if (current) IslaColors.Action else IslaColors.Ink, RectangleShape)
                            .background(if (current) CHIP_CURRENT_BG else CHIP_BG, RectangleShape)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PieceIcon(piece, Modifier.size(30.dp).alpha(if (done) 0.3f else 1f))
                            Text(
                                piece.label.uppercase(),
                                color = IslaColors.Ink.copy(alpha = if (done) 0.35f else 1f),
                                fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Barra del tiempo que queda para poner la pieza actual. */
@Composable
private fun StepTimer(state: BuildState) {
    val fraction = if (state.stepLimitMs > 0 && state.phase == BuildPhase.PLAYING) {
        (state.stepRemainingMs.toFloat() / state.stepLimitMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(12.dp)
            .border(2.dp, IslaColors.Ink, RectangleShape),
    ) {
        drawRect(Color(0xFF3A3550), Offset.Zero, size)
        drawRect(if (fraction > 0.3f) GOOD else BAD, Offset.Zero, Size(size.width * fraction, size.height))
    }
}

@Composable
private fun Tray(state: BuildState, enabled: Boolean, onPick: (Piece) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.tray.withIndex().chunked(4).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (index, piece) ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(if (SHOW_NAMES) 76.dp else 68.dp)
                            .border(3.dp, IslaColors.Ink, RectangleShape)
                            .background(CHIP_BG, RectangleShape)
                            .clickable(enabled = enabled) { onPick(piece) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PieceIcon(piece, Modifier.size(40.dp))
                            if (SHOW_NAMES) {
                                Text(
                                    piece.label,
                                    color = IslaColors.Ink,
                                    fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Text(
                                "${index + 1}",
                                color = IslaColors.Ink,
                                fontFamily = PixelFontFamily, fontSize = 10.sp,
                            )
                        }
                    }
                }
                repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Dibujo. Arte provisional con rectángulos; se reemplaza por sprites reales.
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drawScene(state: BuildState, personColor: Color, painter: Painter?) {
    val w = size.width
    val h = size.height
    val unit = (w / 60f).coerceAtLeast(4f)
    val horizon = h * 0.22f
    val ground = h * 0.40f

    drawRect(NIGHT_TOP, Offset.Zero, Size(w, horizon))
    drawRect(NIGHT_LOW, Offset(0f, horizon * 0.6f), Size(w, horizon * 0.4f + 1f))

    // Estrellas fijas.
    for (i in 0 until 14) {
        val x = w * ((i * 53 % 97) / 100f)
        val y = horizon * ((i * 29 % 60) / 100f)
        drawRect(Color(0xFFEFEFEF), Offset(x, y), Size(unit * 0.5f, unit * 0.5f))
    }

    // Bosque al fondo.
    var tx = 0f
    var i = 0
    while (tx < w) {
        val th = unit * (6f + (i * 7 % 5))
        drawRect(if (i % 2 == 0) FOREST else FOREST_LIGHT, Offset(tx, horizon - th * 0.2f), Size(unit * 5f, th + (ground - horizon)))
        tx += unit * 4.5f
        i++
    }
    drawRect(GROUND, Offset(0f, ground), Size(w, h - ground))
    drawRect(GROUND_LIGHT, Offset(0f, ground), Size(w, unit * 0.8f))

    // Personaje, a la izquierda.
    val px = w * 0.2f
    val feet = ground + unit * 6f
    drawRect(Color(0x33000000), Offset(px - unit * 4f, feet - unit), Size(unit * 8f, unit * 1.5f))
    if (painter != null) {
        val spriteHeight = unit * 18f
        val spriteWidth = spriteHeight * 0.75f
        translate(left = px - spriteWidth / 2f, top = feet - spriteHeight) {
            with(painter) {
                draw(size = Size(spriteWidth, spriteHeight))
            }
        }
    } else {
        drawRect(Color(0xFF2B2B3A), Offset(px - unit * 2f, feet - unit * 4f), Size(unit * 4f, unit * 4f))
        drawRect(personColor, Offset(px - unit * 3f, feet - unit * 10f), Size(unit * 6f, unit * 6f))
        drawRect(Color(0xFFF2C9A0), Offset(px - unit * 2f, feet - unit * 15f), Size(unit * 4f, unit * 5f))
        drawRect(Color(0xFF5A3B2A), Offset(px - unit * 2f, feet - unit * 16f), Size(unit * 4f, unit * 2f))
    }

    // La fogata: una pila con las piezas ya colocadas en esta ronda.
    val fx = w * 0.66f
    val base = feet
    val placed = state.sequence.take(state.step.coerceAtMost(state.sequence.size))
    val iconSide = unit * 3.8f
    placed.forEachIndexed { index, piece ->
        val col = index % 3
        val row = index / 3
        drawPieceIcon(
            piece,
            Offset(fx - iconSide * 1.5f + col * iconSide + (row % 2) * unit * 0.8f, base - iconSide * (row + 1)),
            iconSide,
        )
    }

    // Llamas cuando la ronda se completa; brasas pequeñas mientras se arma.
    val flicker = ((state.remainingMs / 120L) % 3L).toInt()
    if (state.phase == BuildPhase.ROUND_DONE) {
        val top = base - unit * (9f + flicker)
        drawRect(FLAME_C, Offset(fx - unit * 4f, top + unit * 3f), Size(unit * 8f, unit * 6f))
        drawRect(FLAME_B, Offset(fx - unit * 3f, top + unit), Size(unit * 6f, unit * 7f))
        drawRect(FLAME_A, Offset(fx - unit * 1.5f, top), Size(unit * 3f, unit * 6f))
    } else if (placed.isNotEmpty()) {
        drawRect(FLAME_B, Offset(fx - unit * 0.6f, base - iconSide * ((placed.size - 1) / 3 + 1) - unit * 1.4f), Size(unit * 1.2f, unit * 1.2f))
    }
}
