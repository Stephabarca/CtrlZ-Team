package com.example.videojuego.ui.finale

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
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
import com.example.videojuego.finale.FinaleResult
import com.example.videojuego.finale.FinaleRoll
import com.example.videojuego.finale.FinaleRules
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import videojuego.shared.generated.resources.Res
import videojuego.shared.generated.resources.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

private enum class Phase { DECK, SUNSET, ROLL, REVEAL, END }

private val SKY_TOP = listOf(Color(0xFF5BA8E6), Color(0xFFE8845A), Color(0xFF7B3F8C), Color(0xFF1B1F4B))
private val SKY_BOTTOM = listOf(Color(0xFFBFE3F5), Color(0xFFFFC46B), Color(0xFFFF6F61), Color(0xFF5B2A6E))
private val SEA_TOP = listOf(Color(0xFF3FA7D6), Color(0xFFC96B5A), Color(0xFF5A3A78), Color(0xFF1A2250))
private val SEA_BOTTOM = listOf(Color(0xFF1C6FA8), Color(0xFF7A3E5E), Color(0xFF2D2159), Color(0xFF0B1033))
private val SUN = listOf(Color(0xFFFFF2A8), Color(0xFFFFC83D), Color(0xFFFF8A3D))
private val DECK_WOOD = listOf(Color(0xFF9A6634), Color(0xFF8A4F3A), Color(0xFF50304F), Color(0xFF241A3C))
private val HEART = Color(0xFFFF5C8A)

private val NPC_IDS = FinaleRules.ORDER
private const val PROTA_X = 0.74f
private val NPC_X = listOf(0.20f, 0.38f, 0.56f)
private const val WINNER_TARGET_X = 0.60f
private const val CHARS_PER_SECOND = 38f

private fun speakerColor(id: String?): Color = when (id) {
    "valentina" -> Color(0xFFD9546B)
    "lucas" -> Color(0xFF3E8E5A)
    "lila" -> Color(0xFF7B5EC7)
    else -> IslaColors.Ink
}

private fun ramp(p: Float, stops: List<Color>): Color {
    val x = p.coerceIn(0f, 1f) * (stops.size - 1)
    val i = x.toInt().coerceAtMost(stops.size - 2)
    return lerp(stops[i], stops[i + 1], x - i)
}

private fun mix(a: Float, b: Float, t: Float) = a + (b - a) * t

/**
 * Escena final: los cuatro en la cubierta del barco al atardecer. El sistema elige a la pareja con [result]
 * (compatibilidad de todo el juego) y la revela con una ruleta de corazones. Toca o pulsa Enter/Espacio para avanzar.
 */
@Composable
fun FinaleScreen(result: FinaleResult, onFinished: () -> Unit) {
    val protaPainter = painterResource(Res.drawable.prota_2)
    val npcPainters = listOf(
        painterResource(Res.drawable.valentina_oliva_4),
        painterResource(Res.drawable.lucas_ambar_4),
        painterResource(Res.drawable.lila_ariza_4),
    )

    var t by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        val t0 = withFrameMillis { it }
        while (true) t = withFrameMillis { it } - t0
    }

    var phase by remember { mutableStateOf(Phase.DECK) }
    var phaseStart by remember { mutableStateOf(0L) }
    var lineIdx by remember { mutableStateOf(0) }
    var lineStart by remember { mutableStateOf(0L) }

    val revealLines = remember(result) { FinaleStrings.revealLines(result.partnerId, result.closeCall, result.runnerUpId) }
    val lines = when (phase) {
        Phase.DECK -> FinaleStrings.deck
        Phase.SUNSET -> FinaleStrings.sunset
        Phase.REVEAL -> revealLines
        else -> emptyList()
    }

    fun go(p: Phase) {
        phase = p; phaseStart = t; lineIdx = 0; lineStart = t
    }

    fun visibleChars(): Int = lines.getOrNull(lineIdx)?.let {
        (max(0L, t - lineStart) / 1000f * CHARS_PER_SECOND).toInt().coerceAtMost(it.text.length)
    } ?: 0

    fun next() {
        val line = lines.getOrNull(lineIdx) ?: return
        if (visibleChars() < line.text.length) {
            lineStart = t - 100_000L // primero se completa la línea
            return
        }
        if (lineIdx < lines.lastIndex) {
            lineIdx++; lineStart = t
        } else {
            when (phase) {
                Phase.DECK -> go(Phase.SUNSET)
                Phase.SUNSET -> go(Phase.ROLL)
                Phase.REVEAL -> go(Phase.END)
                else -> Unit
            }
        }
    }

    LaunchedEffect(phase) {
        if (phase == Phase.ROLL) {
            // El corazón se detiene en la pareja: suena el éxito y, tras una pausa, empieza la revelación.
            delay(FinaleRoll.spinMs)
            Sfx.play(SoundEffect.SUCCESS)
            delay(FinaleRoll.HOLD_MS)
            go(Phase.REVEAL)
        }
    }

    val sunTarget = when (phase) {
        Phase.DECK -> 0.12f
        Phase.SUNSET -> 0.55f
        Phase.ROLL -> 0.78f
        Phase.REVEAL -> 0.9f
        Phase.END -> 0.97f
    }
    val sunP by animateFloatAsState(sunTarget, tween(7000, easing = LinearEasing))
    val barsP by animateFloatAsState(if (phase == Phase.DECK) 0f else 1f, tween(1200))

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focus)
            .focusable()
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown && (it.key == Key.Enter || it.key == Key.Spacebar)) {
                    next(); true
                } else false
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { next() },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawFinale(
                t = t,
                phase = phase,
                phaseT = t - phaseStart,
                sunP = sunP,
                barsP = barsP,
                result = result,
                protaPainter = protaPainter,
                npcPainters = npcPainters,
            )
        }

        // Título al empezar.
        if (t < 3600L) {
            val a = when {
                t < 600L -> t / 600f
                t > 2800L -> 1f - (t - 2800L) / 800f
                else -> 1f
            }.coerceIn(0f, 1f)
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    FinaleStrings.TitleTop,
                    color = Color.White.copy(alpha = a),
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                )
                Text(
                    FinaleStrings.TitleBottom,
                    color = Color(0xFFFFE9B8).copy(alpha = a),
                    fontFamily = PixelFontFamily,
                    fontSize = 12.sp,
                )
            }
        }

        // Frases de suspenso mientras gira la ruleta.
        if (phase == Phase.ROLL) {
            val el = t - phaseStart
            val caption = FinaleStrings.rollCaptions.lastOrNull { el >= it.second }?.first
            if (caption != null) {
                Text(
                    caption,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 44.dp)
                        .background(Color(0x88000000), RectangleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Color(0xFFFFF4D6),
                    fontFamily = PixelFontFamily,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Cuadro de diálogo.
        val line = lines.getOrNull(lineIdx)
        if (line != null) {
            val shown = visibleChars()
            val full = shown >= line.text.length
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.94f)
                    .padding(bottom = 14.dp)
                    .border(3.dp, IslaColors.Ink, RectangleShape)
                    .background(IslaColors.Paper, RectangleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                line.speakerId?.let {
                    Text(
                        FinaleStrings.names.getValue(it),
                        color = speakerColor(it),
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    line.text.take(shown),
                    color = IslaColors.Ink,
                    fontFamily = PixelFontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (line.speakerId == null) TextAlign.Center else TextAlign.Start,
                )
                if (full && (t / 500L) % 2L == 0L) {
                    Text(
                        FinaleStrings.Hint,
                        color = IslaColors.Ink,
                        fontFamily = PixelFontFamily,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }

        // Pantalla final: FIN y la compatibilidad con cada uno.
        if (phase == Phase.END) {
            val a = (((t - phaseStart) - 600L) / 900f).coerceIn(0f, 1f)
            val top = result.scores.values.maxOrNull()?.coerceAtLeast(1) ?: 1
            Column(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.7f)
                    .background(Color(0xBB000000), RectangleShape)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    FinaleStrings.EndTitle,
                    color = Color(0xFFFFE9B8).copy(alpha = a),
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                )
                Text(
                    FinaleStrings.EndSubtitle,
                    color = Color.White.copy(alpha = a),
                    fontFamily = PixelFontFamily,
                    fontSize = 11.sp,
                )
                NPC_IDS.forEach { id ->
                    val score = result.scores[id] ?: 0
                    val frac = (score.toFloat() / top).coerceIn(0.08f, 1f)
                    val winner = id == result.partnerId
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            FinaleStrings.names.getValue(id) + if (winner) " ♥" else "",
                            modifier = Modifier.width(110.dp),
                            color = (if (winner) HEART else Color.White).copy(alpha = a),
                            fontFamily = PixelFontFamily,
                            fontWeight = if (winner) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                        )
                        Box(Modifier.width(160.dp).height(10.dp).background(Color(0x44FFFFFF).copy(alpha = 0.27f * a))) {
                            Box(
                                Modifier
                                    .fillMaxWidth(frac * a)
                                    .height(10.dp)
                                    .background(speakerColor(id).copy(alpha = a)),
                            )
                        }
                    }
                }
                Text(
                    FinaleStrings.Continue,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .border(3.dp, IslaColors.Ink, RectangleShape)
                        .background(IslaColors.Paper, RectangleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (a >= 1f) {
                                Sfx.play(SoundEffect.CLICK)
                                onFinished()
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    color = IslaColors.Ink.copy(alpha = a),
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// Dibujo
// ---------------------------------------------------------------------------------------------------------------------

private fun DrawScope.drawFinale(
    t: Long,
    phase: Phase,
    phaseT: Long,
    sunP: Float,
    barsP: Float,
    result: FinaleResult,
    protaPainter: Painter,
    npcPainters: List<Painter>,
) {
    val w = size.width
    val h = size.height
    val horizon = h * 0.46f
    val deckTop = h * 0.62f
    val tf = t.toFloat()

    // Cielo.
    drawRect(
        Brush.verticalGradient(listOf(ramp(sunP, SKY_TOP), ramp(sunP, SKY_BOTTOM)), startY = 0f, endY = horizon),
        Offset.Zero, Size(w, horizon),
    )

    // Estrellas al final.
    if (sunP > 0.75f) {
        val starA = ((sunP - 0.75f) / 0.25f).coerceIn(0f, 1f) * 0.9f
        for (i in 0 until 26) {
            val sx = ((i * 37) % 100) / 100f * w
            val sy = ((i * 53) % 100) / 100f * horizon * 0.65f
            val tw = 0.5f + 0.5f * sin(tf / 400f + i)
            drawCircle(Color.White.copy(alpha = starA * tw), 1.6f + (i % 3), Offset(sx, sy))
        }
    }

    // Nubes que se mueven despacio.
    val cloudColor = lerp(Color.White, Color(0xFFFFB0A0), sunP.coerceIn(0f, 1f)).copy(alpha = 0.55f)
    for (i in 0 until 4) {
        val cw = w * (0.16f + 0.04f * i)
        val x = ((i * 0.31f * w + tf * 0.012f * (i + 1)) % (w + cw * 2f)) - cw
        val y = horizon * (0.14f + 0.17f * i)
        drawRoundRect(cloudColor, Offset(x, y), Size(cw, h * 0.028f), CornerRadius(h * 0.014f))
        drawRoundRect(cloudColor, Offset(x + cw * 0.2f, y - h * 0.018f), Size(cw * 0.5f, h * 0.03f), CornerRadius(h * 0.015f))
    }

    // Sol con resplandor.
    val sunR = minOf(w, h) * 0.085f
    val sunX = w * 0.5f
    val sunY = mix(horizon * 0.28f, horizon + sunR * 0.55f, sunP)
    drawCircle(
        Brush.radialGradient(listOf(Color(0xAAFFE9A8), Color.Transparent), center = Offset(sunX, sunY), radius = sunR * 4.5f),
        radius = sunR * 4.5f, center = Offset(sunX, sunY),
    )
    drawCircle(ramp(sunP, SUN), sunR, Offset(sunX, sunY))

    // Mar.
    drawRect(
        Brush.verticalGradient(listOf(ramp(sunP, SEA_TOP), ramp(sunP, SEA_BOTTOM)), startY = horizon, endY = h),
        Offset(0f, horizon), Size(w, h - horizon),
    )
    // Reflejo del sol.
    for (i in 0 until 11) {
        val ry = horizon + i * (deckTop - horizon) * 0.1f
        val rw = sunR * (2.2f - i * 0.14f) * (0.75f + 0.25f * sin(tf / 300f + i))
        val ra = (1f - i / 12f) * (1f - sunP * 0.45f)
        drawRect(Color(0xFFFFD27A).copy(alpha = ra * 0.8f), Offset(sunX - rw / 2f, ry), Size(rw, 3f + i * 0.5f))
    }
    // Olas.
    val waveColor = Color.White.copy(alpha = 0.16f)
    for (i in 0 until 9) {
        val frac = (i + 1) / 9f
        val wy = horizon + (deckTop - horizon) * frac * frac
        var x = -30f + (i % 2) * 18f
        while (x < w) {
            val dx = sin(tf / 700f + i + x * 0.02f) * 8f
            drawLine(waveColor, Offset(x + dx, wy), Offset(x + dx + 22f, wy), strokeWidth = 1.5f + i * 0.3f)
            x += 46f
        }
    }

    // Barco: palo con bandera, cubierta de madera y baranda.
    val deckColor = ramp(sunP, DECK_WOOD)
    drawRect(deckColor, Offset(0f, deckTop), Size(w, h - deckTop))
    val plank = lerp(deckColor, Color.Black, 0.28f)
    var py = deckTop + h * 0.035f
    var row = 0
    while (py < h) {
        drawLine(plank, Offset(0f, py), Offset(w, py), strokeWidth = 2f)
        val off = if (row % 2 == 0) 0f else w * 0.06f
        var px = off
        while (px < w) {
            drawLine(plank, Offset(px, py), Offset(px, py + h * 0.035f), strokeWidth = 2f)
            px += w * 0.12f
        }
        py += h * 0.035f
        row++
    }
    val rail = lerp(Color(0xFF5A3418), Color.Black, sunP * 0.5f)
    val railTop = deckTop - h * 0.075f
    drawRect(rail, Offset(0f, deckTop - h * 0.018f), Size(w, h * 0.018f))
    for (i in 0..10) {
        drawRect(rail, Offset(w * i / 10f - 3f, railTop), Size(6f, h * 0.075f))
    }
    drawRect(rail, Offset(0f, railTop - 3f), Size(w, 7f))
    // Palo.
    val mastX = w * 0.07f
    drawRect(rail, Offset(mastX - 5f, h * 0.04f), Size(10f, deckTop - h * 0.04f))
    drawLine(rail.copy(alpha = 0.7f), Offset(mastX, h * 0.06f), Offset(w * 0.30f, railTop), strokeWidth = 2f)
    drawLine(rail.copy(alpha = 0.7f), Offset(mastX, h * 0.06f), Offset(w * 0.30f + 0f, deckTop), strokeWidth = 1.5f)
    val flag = Path().apply {
        val fy = h * 0.05f
        moveTo(mastX + 5f, fy)
        lineTo(mastX + 5f + w * 0.07f, fy + 4f + sin(tf / 250f) * 4f)
        lineTo(mastX + 5f + w * 0.05f, fy + h * 0.025f + sin(tf / 250f + 1f) * 3f)
        lineTo(mastX + 5f, fy + h * 0.04f)
        close()
    }
    drawPath(flag, Color(0xFFD9546B))
    // Salvavidas.
    val lc = Offset(w * 0.93f, deckTop + h * 0.07f)
    drawCircle(Color(0xFFE23D3D), h * 0.04f, lc, style = Stroke(h * 0.016f))
    for (k in 0 until 4) {
        val ang = k * PI.toFloat() / 2f
        val ox = kotlin.math.cos(ang) * h * 0.04f
        val oy = sin(ang) * h * 0.04f
        drawCircle(Color.White, h * 0.009f, lc + Offset(ox, oy))
    }

    // Personajes.
    val feetY = h * 0.88f
    val spriteH = h * 0.30f
    val spriteW = spriteH * 0.75f
    val dusk = 0.28f * sunP
    val tint = ColorFilter.tint(Color(0xFFFF7A3D).copy(alpha = dusk), BlendMode.SrcAtop)
    val winnerSlot = NPC_IDS.indexOf(result.partnerId).coerceAtLeast(0)
    val inReveal = phase == Phase.REVEAL || phase == Phase.END

    fun lift(i: Int, extra: Float = 0f): Float =
        if (phase == Phase.DECK) abs(sin(tf / 260f + i * 1.3f)) * h * 0.035f
        else sin(tf / 900f + i) * h * 0.004f + extra

    fun sprite(p: Painter, cx: Float, lifted: Float) {
        drawOval(Color(0x44000000), Offset(cx - spriteW * 0.38f, feetY - 4f), Size(spriteW * 0.76f, 9f))
        translate(left = cx - spriteW / 2f, top = feetY - spriteH - lifted) {
            with(p) { draw(size = Size(spriteW, spriteH), colorFilter = tint) }
        }
    }

    val walk = if (inReveal) {
        val p = ((phaseT) / 1800f).coerceIn(0f, 1f)
        p * p * (3f - 2f * p)
    } else 0f

    for (i in NPC_IDS.indices) {
        val baseX = w * NPC_X[i]
        val cx = if (inReveal && i == winnerSlot) mix(baseX, w * WINNER_TARGET_X, walk) else baseX
        val extra = if (inReveal && i == winnerSlot) abs(sin(tf / 420f)) * h * 0.012f else 0f
        sprite(npcPainters[i], cx, lift(i + 1, extra))
    }
    sprite(protaPainter, w * PROTA_X, lift(0))

    // Corazones de la ruleta.
    if (phase == Phase.ROLL) {
        val active = FinaleRoll.slotAt(phaseT, winnerSlot, NPC_IDS.size)
        val landed = FinaleRoll.landed(phaseT)
        for (i in NPC_IDS.indices) {
            val cx = w * NPC_X[i]
            val cy = feetY - spriteH - h * 0.06f
            val on = i == active
            val pulse = if (on && landed) 1f + 0.18f * sin(tf / 110f) else 1f
            val s = h * (if (on) 0.042f else 0.026f) * pulse
            if (on) {
                drawCircle(
                    Brush.radialGradient(listOf(HEART.copy(alpha = 0.55f), Color.Transparent), center = Offset(cx, cy), radius = s * 3.2f),
                    radius = s * 3.2f, center = Offset(cx, cy),
                )
            }
            drawPath(heartPath(cx, cy, s), if (on) HEART else Color(0x66FFFFFF))
        }
    }

    // Revelación: destello y corazones que suben.
    if (inReveal) {
        val flash = (1f - phaseT / 900f).coerceIn(0f, 1f) * 0.6f
        if (flash > 0f) drawRect(Color(0xFFFFE9B8).copy(alpha = flash), Offset.Zero, size)
        val midX = w * (WINNER_TARGET_X + PROTA_X) / 2f
        for (i in 0 until 16) {
            val life = ((tf / 1000f) * 0.35f + i / 16f) % 1f
            val hx = midX + sin(i * 2.1f + life * 6f) * w * 0.08f + (i % 5 - 2) * w * 0.012f
            val hy = feetY - spriteH * 0.6f - life * h * 0.38f
            val s = w * 0.012f * (0.7f + (i % 3) * 0.3f)
            drawPath(heartPath(hx, hy, s), HEART.copy(alpha = (1f - life).coerceIn(0f, 1f)))
        }
    }

    // Barras de cine (arriba y abajo) después de la primera escena, y entrada desde negro.
    val bar = h * 0.07f * barsP
    if (bar > 0f) {
        drawRect(Color.Black, Offset.Zero, Size(w, bar))
        drawRect(Color.Black, Offset(0f, h - bar), Size(w, bar))
    }
    val fade = (1f - tf / 1500f).coerceIn(0f, 1f)
    if (fade > 0f) drawRect(Color.Black.copy(alpha = fade), Offset.Zero, size)
}

private fun heartPath(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + s * 0.9f)
    cubicTo(cx - s * 1.6f, cy - s * 0.1f, cx - s * 0.9f, cy - s * 1.2f, cx, cy - s * 0.35f)
    cubicTo(cx + s * 0.9f, cy - s * 1.2f, cx + s * 1.6f, cy - s * 0.1f, cx, cy + s * 0.9f)
    close()
}
