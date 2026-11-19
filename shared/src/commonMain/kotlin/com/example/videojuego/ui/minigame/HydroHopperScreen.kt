package com.example.videojuego.ui.minigame

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.minigame.hydrohopper.HydroHopperGame
import com.example.videojuego.minigame.hydrohopper.HydroHopperState
import com.example.videojuego.minigame.hydrohopper.HydroHopperStats
import com.example.videojuego.minigame.hydrohopper.HydroObstacle
import com.example.videojuego.minigame.hydrohopper.HydroObstacleType
import com.example.videojuego.minigame.hydrohopper.HydroPhase
import com.example.videojuego.timer.frameDelta
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import org.jetbrains.compose.resources.painterResource
import videojuego.shared.generated.resources.Res
import videojuego.shared.generated.resources.*

private val WATER_TOP = Color(0xFF1E5B88)
private val WATER_BOTTOM = Color(0xFF0F3654)
private val WAVE_COLOR = Color(0xFF3B82B6)

@Composable
fun HydroHopperScreen(
    onFinished: (HydroHopperStats) -> Unit,
    modifier: Modifier = Modifier,
    durationSec: Int = 30,
) {
    val game = remember(durationSec) { HydroHopperGame(durationMs = durationSec * 1000L) }
    val state by game.state.collectAsState()

    val protaPainter = painterResource(Res.drawable.prota_2)
    val valentinaPainter = painterResource(Res.drawable.valentina_oliva_4)
    val lucasPainter = painterResource(Res.drawable.lucas_ambar_4)
    val lilaPainter = painterResource(Res.drawable.lila_ariza_4)

    val aletaPainter = painterResource(Res.drawable.aleta_tiburon)
    val balsaPainter = painterResource(Res.drawable.balsa)
    val botellaPainter = painterResource(Res.drawable.botella)
    val boyaPainter = painterResource(Res.drawable.boya)

    // Bucle del reloj del minijuego
    LaunchedEffect(state.phase) {
        if (state.phase != HydroPhase.PLAYING && state.phase != HydroPhase.SHIP_RESCUE) return@LaunchedEffect
        var lastMs = withFrameMillis { it }
        while (state.phase == HydroPhase.PLAYING || state.phase == HydroPhase.SHIP_RESCUE) {
            withFrameMillis { nowMs ->
                val delta = frameDelta(lastMs, nowMs)
                lastMs = nowMs
                game.update(delta)
            }
        }
    }

    val latestFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(state.phase) {
        if (state.phase == HydroPhase.FINISHED) {
            latestFinished(game.stats())
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(state.phase) {
        if (state.phase == HydroPhase.PLAYING) {
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(WATER_TOP, WATER_BOTTOM)))
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && state.phase == HydroPhase.PLAYING) {
                    when (event.key) {
                        Key.A, Key.DirectionLeft -> {
                            game.movePlayer(-0.06f)
                            true
                        }
                        Key.D, Key.DirectionRight -> {
                            game.movePlayer(0.06f)
                            true
                        }
                        Key.Spacebar, Key.W, Key.DirectionUp -> {
                            game.jump()
                            true
                        }
                        else -> false
                    }
                } else false
            },
    ) {
        when (state.phase) {
            HydroPhase.READY -> InstructionsCard(onStart = game::start)
            HydroPhase.PLAYING, HydroPhase.SHIP_RESCUE -> {
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    TopHUD(state)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val targetX = change.position.x / size.width.toFloat()
                                    game.setPlayerX(targetX)
                                }
                            },
                    ) {
                        HydroCanvas(
                            state = state,
                            protaPainter = protaPainter,
                            valentinaPainter = valentinaPainter,
                            lucasPainter = lucasPainter,
                            lilaPainter = lilaPainter,
                            aletaPainter = aletaPainter,
                            balsaPainter = balsaPainter,
                            botellaPainter = botellaPainter,
                            boyaPainter = boyaPainter,
                        )
                        if (state.isShipVisible) {
                            Text(
                                text = HydroHopperStrings.ShipAhead,
                                color = IslaColors.Action,
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp)
                                    .background(IslaColors.Paper, RectangleShape)
                                    .border(2.dp, IslaColors.Ink, RectangleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }

                    // Botones táctiles inferiores para control en móviles/pantallas táctiles
                    BottomControls(
                        onMoveLeft = { game.movePlayer(-0.08f) },
                        onMoveRight = { game.movePlayer(0.08f) },
                        onJump = { game.jump() },
                    )
                }
            }
            HydroPhase.GAME_OVER -> GameOverCard(stats = game.stats(), onRestart = { game.restart() })
            HydroPhase.FINISHED -> VictoryCard(stats = game.stats(), onContinue = { latestFinished(game.stats()) })
        }
    }
}

@Composable
private fun InstructionsCard(onStart: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, IslaColors.Ink, RectangleShape)
                .background(IslaColors.Paper, RectangleShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = HydroHopperStrings.Title,
                color = IslaColors.Primary,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = HydroHopperStrings.Subtitle,
                color = IslaColors.Ink,
                fontFamily = PixelFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, IslaColors.Ink, RectangleShape)
                    .background(Color(0xFFEBF5FB), RectangleShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(HydroHopperStrings.InstructionsTitle, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(HydroHopperStrings.Inst1, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp, lineHeight = 16.sp)
                Text(HydroHopperStrings.Inst2, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp, lineHeight = 16.sp)
                Text(HydroHopperStrings.Inst3, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 11.sp, lineHeight = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            ActionButton(
                text = HydroHopperStrings.Start,
                onClick = onStart,
                container = IslaColors.Action,
            )
        }
    }
}

@Composable
private fun TopHUD(state: HydroHopperState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IslaColors.Ink)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = HydroHopperStrings.scoreText(state.score),
                color = IslaColors.OnDark,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(state.maxHealth) { i ->
                    Text(
                        text = "♥",
                        color = if (i < state.health) IslaColors.Action else IslaColors.OnDark.copy(alpha = 0.3f),
                        fontSize = 14.sp,
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${HydroHopperStrings.DistanceLabel}: ${HydroHopperStrings.distanceText(state.distanceProgress)}",
                color = IslaColors.OnDark,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(4.dp))
            // Barra de progreso de distancia
            Box(
                Modifier
                    .width(140.dp)
                    .height(10.dp)
                    .border(1.dp, IslaColors.OnDark, RectangleShape)
                    .background(Color.Black),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(state.distanceProgress)
                        .height(10.dp)
                        .background(IslaColors.Action),
                )
            }
        }
    }
}

@Composable
private fun BottomControls(
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onJump: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IslaColors.Ink.copy(alpha = 0.85f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 95.dp, height = 44.dp)
                .border(2.dp, IslaColors.OnDark, RectangleShape)
                .background(IslaColors.Primary, RectangleShape)
                .clickable(onClick = onMoveLeft),
            contentAlignment = Alignment.Center,
        ) {
            Text("◄ IZQ", color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 44.dp)
                .border(2.dp, IslaColors.OnDark, RectangleShape)
                .background(IslaColors.Action, RectangleShape)
                .clickable(onClick = onJump),
            contentAlignment = Alignment.Center,
        ) {
            Text("▲ SALTAR", color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .size(width = 95.dp, height = 44.dp)
                .border(2.dp, IslaColors.OnDark, RectangleShape)
                .background(IslaColors.Primary, RectangleShape)
                .clickable(onClick = onMoveRight),
            contentAlignment = Alignment.Center,
        ) {
            Text("DER ►", color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HydroCanvas(
    state: HydroHopperState,
    protaPainter: Painter,
    valentinaPainter: Painter,
    lucasPainter: Painter,
    lilaPainter: Painter,
    aletaPainter: Painter,
    balsaPainter: Painter,
    botellaPainter: Painter,
    boyaPainter: Painter,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val waveOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "waveOffset",
    )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val horizonY = h * 0.16f

        // 1. Dibujar el cielo al amanecer en el fondo superior
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFE65100), Color(0xFFFF8A65), Color(0xFFFFE082)),
                startY = 0f,
                endY = horizonY + 20f,
            ),
            size = Size(w, horizonY + 30f),
        )

        // Sol saliendo en el horizonte
        drawCircle(Color(0xFFFFD54F).copy(alpha = 0.5f), 45f, Offset(w / 2f, horizonY + 5f))
        drawCircle(Color(0xFFFFF59D), 28f, Offset(w / 2f, horizonY + 5f))

        // 2. Mar con curvatura en el horizonte (curva hacia arriba al fondo)
        val seaPath = Path().apply {
            moveTo(0f, horizonY + 18f)
            quadraticTo(w / 2f, horizonY - 12f, w, horizonY + 18f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = seaPath,
            brush = Brush.verticalGradient(listOf(WATER_TOP, WATER_BOTTOM), startY = horizonY, endY = h),
        )

        // Línea dorada/brillante de espuma en la curva del horizonte
        val horizonCrest = Path().apply {
            moveTo(0f, horizonY + 18f)
            quadraticTo(w / 2f, horizonY - 12f, w, horizonY + 18f)
        }
        drawPath(path = horizonCrest, color = Color(0xFFFFE082), style = Stroke(width = 3f))

        // 3. Olas de agua curvadas en la misma dirección que el horizonte
        var yWave = horizonY + 20f + (waveOffsetY % 50f)
        while (yWave < h + 50f) {
            val waveProgress = ((yWave - horizonY) / (h - horizonY)).coerceIn(0f, 1f)
            val archHeight = 12f * (1f - waveProgress * 0.4f)
            val wavePath = Path().apply {
                moveTo(0f, yWave)
                quadraticTo(w / 2f, yWave - archHeight, w, yWave)
            }
            drawPath(
                path = wavePath,
                color = WAVE_COLOR.copy(alpha = 0.6f + waveProgress * 0.4f),
                style = Stroke(width = 2f + waveProgress * 2f),
            )
            yWave += 45f + waveProgress * 15f
        }

        // 4. Barco de rescate: aparece en el horizonte al amanecer y navega hacia el jugador
        if (state.isShipVisible) {
            val shipProgress = when (state.phase) {
                HydroPhase.SHIP_RESCUE -> 1.0f
                else -> ((state.distanceProgress - 0.80f) / 0.18f).coerceIn(0.0f, 1.0f)
            }
            val startY = horizonY - 10f
            val targetY = h * 0.52f
            val shipY = startY + (targetY - startY) * shipProgress
            val shipScale = 0.45f + shipProgress * 0.60f
            drawRescueShip(w / 2f, shipY, w * 0.70f * shipScale, shipScale)
        }

        // 4. Dibujar obstáculos flotantes con las nuevas imágenes
        for (obs in state.obstacles) {
            drawObstacle(obs, w, h, aletaPainter, botellaPainter, boyaPainter)
        }

        // 5. Dibujar la balsa con la imagen y los 4 personajes (2 adelante y 2 atrás)
        val raftX = state.playerX * w
        val raftY = h * 0.82f
        val isFlickering = state.invincibleRemainingMs > 0L && ((state.remainingMs / 100L) % 2L == 0L)
        val alphaMultiplier = if (isFlickering) 0.35f else 1.0f

        drawPlayerRaft(
            centerX = raftX,
            centerY = raftY,
            raftWidth = w * 0.28f,
            jumpHeight = state.jumpHeight,
            alphaMultiplier = alphaMultiplier,
            balsaPainter = balsaPainter,
            protaPainter = protaPainter,
            valentinaPainter = valentinaPainter,
            lucasPainter = lucasPainter,
            lilaPainter = lilaPainter,
        )
    }
}

private fun DrawScope.drawObstacle(
    obs: HydroObstacle,
    canvasWidth: Float,
    canvasHeight: Float,
    aletaPainter: Painter,
    botellaPainter: Painter,
    boyaPainter: Painter,
) {
    val cx = obs.x * canvasWidth
    val cy = obs.y * canvasHeight
    val horizonY = canvasHeight * 0.16f

    val obsProgress = ((cy - horizonY) / (canvasHeight * 0.82f - horizonY)).coerceIn(0.15f, 1.0f)
    val scale = 0.35f + obsProgress * 0.65f

    // Ajuste de escala específico: la botella (ROCK) se dibuja en un tamaño más reducido y proporcional
    val isBottle = obs.type == HydroObstacleType.ROCK
    val sizeFactor = if (isBottle) 0.55f else 1.0f
    val obsW = obs.type.width * canvasWidth * scale * (if (isBottle) 1.8f else 1.0f) * sizeFactor
    val obsH = obsW * (if (isBottle) 1.25f else 0.85f)

    val painter = when (obs.type) {
        HydroObstacleType.LOG -> aletaPainter
        HydroObstacleType.ROCK -> botellaPainter
        HydroObstacleType.BUOY -> boyaPainter
    }

    translate(left = cx - obsW / 2f, top = cy - obsH / 2f) {
        with(painter) { draw(size = Size(obsW, obsH)) }
    }
}

private fun DrawScope.drawRescueShip(centerX: Float, topY: Float, shipWidth: Float, scale: Float = 1.0f) {
    val h = 85f * scale
    val x = centerX - shipWidth / 2f

    // Casco del barco de rescate
    drawRect(Color(0xFF37474F), Offset(x, topY), Size(shipWidth, h))
    drawRect(Color(0xFFCFD8DC), Offset(x + shipWidth * 0.15f, topY - 32f * scale), Size(shipWidth * 0.7f, 32f * scale))
    drawRect(Color(0xFFD32F2F), Offset(x, topY + h - 12f * scale), Size(shipWidth, 12f * scale))
    drawRect(Color(0xFFFFD54F), Offset(x + shipWidth * 0.45f, topY - 55f * scale), Size(shipWidth * 0.1f, 23f * scale))

    // Ojos de buey del barco
    repeat(4) { i ->
        drawCircle(Color(0xFFFFD54F), 7f * scale, Offset(x + shipWidth * (0.2f + i * 0.2f), topY + 38f * scale))
    }
}

private fun DrawScope.drawPlayerRaft(
    centerX: Float,
    centerY: Float,
    raftWidth: Float,
    jumpHeight: Float,
    alphaMultiplier: Float = 1.0f,
    balsaPainter: Painter,
    protaPainter: Painter,
    valentinaPainter: Painter,
    lucasPainter: Painter,
    lilaPainter: Painter,
) {
    val raftH = 38f
    val jumpOffsetY = jumpHeight * 65f
    val rx = centerX - raftWidth / 2f
    val ry = (centerY - jumpOffsetY) - raftH / 2f

    val shadowAlpha = ((0.45f - jumpHeight * 0.25f) * alphaMultiplier).coerceIn(0.1f, 0.45f)
    drawRect(Color(0x33000000).copy(alpha = shadowAlpha), Offset(rx - 6f, centerY + 10f), Size(raftWidth + 12f, 14f))

    // Dibujar la imagen de la balsa
    translate(left = rx, top = ry - 8f) {
        with(balsaPainter) { draw(size = Size(raftWidth, raftH + 16f), alpha = alphaMultiplier) }
    }

    // Dibujar a los 4 personajes distribuidos: 2 atrás (Lucas y Lila) y 2 adelante (Protagonista y Valentina)
    val charW = 30f
    val charH = 42f

    // Fila de Atrás (Lucas e Lila)
    val backY = ry - charH * 0.70f
    translate(left = centerX - charW * 1.3f, top = backY) {
        with(lucasPainter) { draw(size = Size(charW, charH), alpha = alphaMultiplier) }
    }
    translate(left = centerX + charW * 0.3f, top = backY) {
        with(lilaPainter) { draw(size = Size(charW, charH), alpha = alphaMultiplier) }
    }

    // Fila de Adelante (Protagonista y Valentina)
    val frontY = ry - charH * 0.25f
    translate(left = centerX - charW * 1.0f, top = frontY) {
        with(protaPainter) { draw(size = Size(charW, charH), alpha = alphaMultiplier) }
    }
    translate(left = centerX + charW * 0.0f, top = frontY) {
        with(valentinaPainter) { draw(size = Size(charW, charH), alpha = alphaMultiplier) }
    }
}

@Composable
private fun GameOverCard(stats: HydroHopperStats, onRestart: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, IslaColors.Ink, RectangleShape)
                .background(IslaColors.Paper, RectangleShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "¡TE HAS QUEDADO SIN VIDAS!",
                color = IslaColors.Action,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = "Los obstáculos dañaron la balsa en el mar. ¡No te rindas y vuelve a intentarlo!",
                color = IslaColors.Ink,
                fontFamily = PixelFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, IslaColors.Ink, RectangleShape)
                    .background(Color(0xFFFADBD8), RectangleShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${HydroHopperStrings.ScoreLabel}: ${stats.score} / 100", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("• Obstáculos esquivados: ${stats.dodgedCount}", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp)
                Text("• Colisiones: ${stats.hitCount}", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            ActionButton(
                text = "REINTENTAR RESCATE",
                onClick = onRestart,
                container = IslaColors.Action,
            )
        }
    }
}

@Composable
private fun VictoryCard(stats: HydroHopperStats, onContinue: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, IslaColors.Ink, RectangleShape)
                .background(IslaColors.Paper, RectangleShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = HydroHopperStrings.VictoryTitle,
                color = IslaColors.Action,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Text(
                text = HydroHopperStrings.VictorySubtitle,
                color = IslaColors.Ink,
                fontFamily = PixelFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, IslaColors.Ink, RectangleShape)
                    .background(Color(0xFFEBF5FB), RectangleShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${HydroHopperStrings.ScoreLabel}: ${stats.score} / 100", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("• Obstáculos esquivados: ${stats.dodgedCount}", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp)
                Text("• Colisiones: ${stats.hitCount}", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            ActionButton(
                text = HydroHopperStrings.Continue,
                onClick = onContinue,
                container = IslaColors.Action,
            )
        }
    }
}
