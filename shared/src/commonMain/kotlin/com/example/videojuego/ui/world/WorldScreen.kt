package com.example.videojuego.ui.world

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.timer.frameDelta
import com.example.videojuego.ui.Strings
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.components.DialogBox
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import com.example.videojuego.ui.theme.accent
import com.example.videojuego.ui.theme.onAccent
import com.example.videojuego.world.DayPhase
import com.example.videojuego.world.Direction
import com.example.videojuego.world.GiftResult
import com.example.videojuego.world.IslaWorld
import com.example.videojuego.world.ItemType
import com.example.videojuego.world.NpcData
import com.example.videojuego.world.TileType
import com.example.videojuego.world.WorldController
import com.example.videojuego.world.WorldMap
import com.example.videojuego.world.cameraOrigin
import com.example.videojuego.world.darknessAt
import com.example.videojuego.world.formatClock
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

private const val VIEW_TILES_X = 9f
private const val MAX_FRAME_MS = 50L

/** Muestra arriba a la derecha si el teclado tiene el foco y la última tecla recibida. Poner en false al terminar de probar. */
private const val DEBUG_KEYS = true

private val MOVE_LEFT = setOf(Key.A, Key.DirectionLeft)
private val MOVE_RIGHT = setOf(Key.D, Key.DirectionRight)
private val MOVE_UP = setOf(Key.W, Key.DirectionUp)
private val MOVE_DOWN = setOf(Key.S, Key.DirectionDown)
private val MOVE_KEYS = MOVE_LEFT + MOVE_RIGHT + MOVE_UP + MOVE_DOWN
private val INTERACT_KEYS = setOf(Key.E, Key.Enter, Key.Spacebar)

private val WATER_A = Color(0xFF4AA3D8)
private val WATER_B = Color(0xFF3E94C9)
private val WATER_FOAM = Color(0xFFBFE6F5)
private val SAND = Color(0xFFF1D9A0)
private val SAND_DARK = Color(0xFFE3C687)
private val GRASS = Color(0xFF7FB069)
private val GRASS_DARK = Color(0xFF6A9A57)
private val LEAVES = Color(0xFF2F6B3A)
private val LEAVES_LIGHT = Color(0xFF3E8A4A)
private val WOOD = Color(0xFF8B5E3C)
private val ROCK = Color(0xFF8A8D91)
private val ROCK_LIGHT = Color(0xFFB3B6BA)
private val ROCK_DARK = Color(0xFF5F6266)
private val SKIN = Color(0xFFF2C9A0)
private val HAIR = Color(0xFF5A3B2A)

/** Un personaje (jugador o NPC) que se dibuja en el mapa. */
private class Actor(
    val x: Float,
    val y: Float,
    val body: Color,
    val hair: Color,
    val name: String?,
    val facing: Direction,
    val showMarker: Boolean,
)

/**
 * Mapa de la isla: el jugador camina y se acerca a los personajes para hablar con ellos.
 *
 * Controles: joystick táctil abajo a la izquierda (o WASD / flechas en computador) y el botón HABLAR
 * (o E / Enter / Espacio) cuando hay alguien cerca.
 *
 * Cada día dura 5 minutos: de día se habla con conversaciones cortas, se recogen cocos (palmeras) y carbón
 * (rocas) tocándolos, y se pueden regalar. Al acabar el tiempo cae la noche; al tocar la fogata empieza la
 * noche del día actual y se llama a [onNightStart].
 *
 * @param onNightStart se llama con el id de la noche ("night1", "night2"...). Mostrar ahí la ronda de preguntas
 *   y, al terminarla, llamar a controller.startNextDay().
 * @param onGift se llama al regalar algo; sumar ahí la afinidad ([GiftResult.affinityDelta]).
 * @param controller se puede pasar uno propio para conservar la posición y el día al ir a otra pantalla y volver.
 */
@Composable
fun WorldScreen(
    onNightStart: (nightId: String) -> Unit,
    modifier: Modifier = Modifier,
    onGift: (GiftResult) -> Unit = {},
    controller: WorldController = remember {
        WorldController(IslaWorld.map, IslaWorld.npcs, IslaWorld.PLAYER_START_X, IslaWorld.PLAYER_START_Y)
    },
    map: WorldMap = IslaWorld.map,
    npcs: List<NpcData> = IslaWorld.npcs,
) {
    val state by controller.state.collectAsState()
    var joystick by remember { mutableStateOf(Offset.Zero) }
    var keys by remember { mutableStateOf(emptySet<Key>()) }
    var interactHeld by remember { mutableStateOf(false) }
    var chat by remember { mutableStateOf<ChatDialog?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    var hasMoved by remember { mutableStateOf(false) }

    val keyboard = Offset(
        (if (keys.any { it in MOVE_RIGHT }) 1f else 0f) - (if (keys.any { it in MOVE_LEFT }) 1f else 0f),
        (if (keys.any { it in MOVE_DOWN }) 1f else 0f) - (if (keys.any { it in MOVE_UP }) 1f else 0f),
    )
    val rawInput = if (joystick != Offset.Zero) joystick else keyboard
    val currentInput by rememberUpdatedState(if (chat != null) Offset.Zero else rawInput)
    val clockPaused by rememberUpdatedState(chat != null)
    val currentOnNightStart by rememberUpdatedState(onNightStart)

    val interact: () -> Unit = {
        val npc = controller.interact()?.let { id -> npcs.firstOrNull { it.id == id } }
        if (npc != null && chat == null) {
            joystick = Offset.Zero
            val text = if (controller.state.value.isNight) WorldStrings.NightNpc else controller.talk(npc.id) ?: npc.idleLine
            chat = ChatDialog(npc, text)
        }
    }
    val currentInteract by rememberUpdatedState(interact)

    // Ciclo del juego: avanza la simulación en cada cuadro. Un salto grande (app en segundo plano) se recorta.
    LaunchedEffect(controller) {
        var last: Long? = null
        while (true) {
            val now = withFrameMillis { it }
            val previous = last
            if (previous != null) {
                controller.update(currentInput.x, currentInput.y, frameDelta(previous, now, MAX_FRAME_MS), clockPaused)
            }
            last = now
        }
    }
    LaunchedEffect(state.moving) {
        if (state.moving) hasMoved = true
    }
    // Tocar la fogata de noche empieza la noche del día actual.
    LaunchedEffect(state.nightTrigger) {
        val nightId = state.nightTrigger
        if (nightId != null) {
            controller.acknowledgeNight()
            currentOnNightStart(nightId)
        }
    }
    // Aviso corto "+1 Coco" al recoger algo.
    LaunchedEffect(state.harvestSeq) {
        val item = state.lastHarvest
        if (state.harvestSeq > 0 && item != null) {
            toast = WorldStrings.picked(item.label)
            delay(1200)
            toast = null
        }
    }

    val focusRequester = remember { FocusRequester() }
    var hasKeyboardFocus by remember { mutableStateOf(false) }
    var lastKey by remember { mutableStateOf("(ninguna)") }

    fun requestKeyboardFocus() {
        try {
            focusRequester.requestFocus()
        } catch (ignored: IllegalStateException) {
            // El nodo todavía no está listo; se vuelve a intentar.
        }
    }
    // El teclado solo funciona si esta pantalla tiene el foco. Se reintenta hasta lograrlo.
    LaunchedEffect(Unit) {
        var attempts = 0
        while (!hasKeyboardFocus && attempts < 30) {
            requestKeyboardFocus()
            delay(100)
            attempts++
        }
    }

    val measurer = rememberTextMeasurer()
    // Los estilos se crean aquí (dentro de un @Composable) porque PixelFontFamily puede cargar una fuente.
    val labelStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = PixelFontFamily)
    val markerStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = PixelFontFamily)
    val actors = npcs.map {
        Actor(it.centerX, it.centerY, it.speaker.accent(), HAIR, it.name, Direction.DOWN, false)
    } + Actor(state.playerX, state.playerY, IslaColors.Primary, HAIR, null, state.facing, false)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WATER_A)
            // Cada toque vuelve a pedir el foco del teclado, sin consumir el evento (el joystick sigue funcionando).
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Press) requestKeyboardFocus()
                    }
                }
            }
            .onKeyEvent { event ->
                val key = event.key
                if (DEBUG_KEYS) lastKey = "$key ${event.type}"
                when (event.type) {
                    KeyEventType.KeyDown -> when (key) {
                        in INTERACT_KEYS -> {
                            if (!interactHeld) {
                                interactHeld = true
                                currentInteract()
                            }
                            true
                        }
                        in MOVE_KEYS -> {
                            keys = keys + key
                            true
                        }
                        else -> false
                    }
                    KeyEventType.KeyUp -> when (key) {
                        in INTERACT_KEYS -> {
                            interactHeld = false
                            true
                        }
                        in MOVE_KEYS -> {
                            keys = keys - key
                            true
                        }
                        else -> false
                    }
                    else -> false
                }
            }
            .onFocusChanged { hasKeyboardFocus = it.isFocused }
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawWorld(
                map, actors, state.playerX, state.playerY, state.timeMs, measurer, labelStyle, markerStyle,
                darknessAt(state.phase, state.dayRemainingMs), state.isNight,
            )
        }

        Box(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            if (DEBUG_KEYS) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .background(Color(0xCC1F2A2C), RectangleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        "TECLADO: ${if (hasKeyboardFocus) "ACTIVO" else "SIN FOCO"}\n$lastKey",
                        color = if (hasKeyboardFocus) Color(0xFFCAD988) else Color(0xFFF36B6B),
                        fontFamily = PixelFontFamily,
                        fontSize = 10.sp,
                    )
                }
            }

            // Reloj e inventario.
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .border(3.dp, IslaColors.Ink, RectangleShape)
                    .background(IslaColors.Paper, RectangleShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    if (state.isNight) "${WorldStrings.Night} ${state.day}"
                    else "${WorldStrings.Day} ${state.day}  ${formatClock(state.dayRemainingMs)}",
                    color = IslaColors.Ink,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
                val bag = ItemType.entries.filter { state.count(it) > 0 }
                Text(
                    if (bag.isEmpty()) WorldStrings.BagEmpty else bag.joinToString("  ") { "${it.label} x${state.count(it)}" },
                    color = IslaColors.Ink,
                    fontFamily = PixelFontFamily,
                    fontSize = 10.sp,
                )
            }

            toast?.let {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                        .background(Color(0xCC1F2A2C), RectangleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(it, color = Color(0xFFCAD988), fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if ((!hasMoved || state.isNight) && chat == null) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(start = 120.dp, end = 120.dp)
                        .border(3.dp, IslaColors.Ink, RectangleShape)
                        .background(IslaColors.Paper, RectangleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        if (state.isNight) WorldStrings.NightHint else WorldStrings.Hint,
                        color = IslaColors.Ink,
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }

            val dialog = chat
            if (dialog == null) {
                VirtualJoystick(onChange = { joystick = it }, modifier = Modifier.align(Alignment.BottomStart))
                if (state.nearbyNpcId != null) {
                    ActionButton(
                        text = WorldStrings.Talk,
                        onClick = interact,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 40.dp),
                    )
                }
            } else {
                val npc = dialog.npc
                DialogBox(
                    speakerName = npc.name,
                    accent = npc.speaker.accent(),
                    onAccent = npc.speaker.onAccent(),
                    text = dialog.text,
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ItemType.entries.filter { controller.canGift(npc.id, it) }.forEach { item ->
                            ActionButton(
                                text = WorldStrings.gift(item.label, state.count(item)),
                                onClick = {
                                    val result = controller.giveGift(npc.id, item)
                                    if (result != null) {
                                        onGift(result)
                                        chat = dialog.copy(text = result.reaction)
                                    }
                                },
                            )
                        }
                        ActionButton(text = Strings.Continue, onClick = { chat = null })
                    }
                }
            }
        }
    }
}

/** Conversación abierta con un personaje. */
private data class ChatDialog(val npc: NpcData, val text: String)

// ---------------------------------------------------------------------------------------------
// Dibujo. Todo es arte provisional hecho con rectángulos; se reemplaza por sprites reales (IP-32/33).
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drawWorld(
    map: WorldMap,
    actors: List<Actor>,
    playerX: Float,
    playerY: Float,
    timeMs: Long,
    measurer: TextMeasurer,
    labelStyle: TextStyle,
    markerStyle: TextStyle,
    darkness: Float,
    night: Boolean,
) {
    val tile = floor(size.width / VIEW_TILES_X).coerceAtLeast(16f)
    val viewW = size.width / tile
    val viewH = size.height / tile
    val camX = cameraOrigin(playerX, viewW, map.width)
    val camY = cameraOrigin(playerY, viewH, map.height)
    fun screenX(worldX: Float) = round((worldX - camX) * tile)
    fun screenY(worldY: Float) = round((worldY - camY) * tile)

    val firstX = floor(camX).toInt().coerceAtLeast(0)
    val lastX = ceil(camX + viewW).toInt().coerceAtMost(map.width - 1)
    val firstY = floor(camY).toInt().coerceAtLeast(0)
    val lastY = ceil(camY + viewH).toInt().coerceAtMost(map.height - 1)

    for (ty in firstY..lastY) {
        for (tx in firstX..lastX) {
            val type = map.tileAt(tx, ty) ?: continue
            val roof = type == TileType.HUT && map.tileAt(tx, ty - 1) != TileType.HUT
            drawTile(type, tx, ty, screenX(tx.toFloat()), screenY(ty.toFloat()), tile, timeMs, roof)
        }
    }

    // De atrás hacia adelante, para que quien está más abajo tape al de arriba.
    for (actor in actors.sortedBy { it.y }) {
        val centerX = screenX(actor.x)
        val feetY = screenY(actor.y) + tile * 0.4f
        drawPerson(centerX, feetY, tile, actor)
        if (actor.name != null) drawLabel(measurer, actor.name, centerX, feetY - tile * 1.15f, labelStyle)
        if (actor.showMarker) {
            val bob = if ((timeMs / 350L) % 2L == 0L) 0f else tile / 16f
            drawMarker(measurer, centerX, feetY - tile * 1.55f + bob, tile, markerStyle)
        }
    }

    // Oscuridad del atardecer/noche, con resplandor alrededor de la fogata.
    if (darkness > 0f) {
        drawRect(Color(0xFF0B1030).copy(alpha = darkness), Offset.Zero, size)
        for (ty in firstY..lastY) {
            for (tx in firstX..lastX) {
                if (map.tileAt(tx, ty) != TileType.FIRE) continue
                val cx = screenX(tx + 0.5f)
                val cy = screenY(ty + 0.5f)
                val strength = darkness / 0.55f
                drawCircle(Color(0xFFF28C28).copy(alpha = 0.16f * strength), tile * 3.2f, Offset(cx, cy))
                drawCircle(Color(0xFFFFD84D).copy(alpha = 0.16f * strength), tile * 1.8f, Offset(cx, cy))
                if (night) {
                    val bob = if ((timeMs / 350L) % 2L == 0L) 0f else tile / 16f
                    drawMarker(measurer, cx, cy - tile * 1.1f + bob, tile, markerStyle)
                }
            }
        }
    }
}

private fun DrawScope.drawTile(
    type: TileType,
    tx: Int,
    ty: Int,
    sx: Float,
    sy: Float,
    tile: Float,
    timeMs: Long,
    roof: Boolean,
) {
    val u = tile / 16f
    fun fill(color: Color) = drawRect(color, Offset(sx, sy), Size(tile + 1f, tile + 1f))
    fun rect(color: Color, x: Float, y: Float, w: Float, h: Float) =
        drawRect(color, Offset(sx + x * u, sy + y * u), Size(w * u, h * u))

    val hash = (tx * 31 + ty * 17).mod(7)
    when (type) {
        TileType.WATER -> {
            val phase = ((timeMs / 700L) + tx + ty).mod(2L) == 0L
            fill(if (phase) WATER_A else WATER_B)
            if ((tx * 7 + ty * 13).mod(5) == 0) rect(WATER_FOAM, if (phase) 3f else 8f, 6f, 5f, 1f)
        }
        TileType.SAND -> {
            fill(SAND)
            if (hash == 0) rect(SAND_DARK, 5f, 9f, 2f, 1f)
            if (hash == 3) rect(SAND_DARK, 11f, 3f, 1f, 1f)
        }
        TileType.GRASS -> {
            fill(GRASS)
            if (hash == 1) rect(GRASS_DARK, 3f, 4f, 2f, 2f)
            if (hash == 4) rect(GRASS_DARK, 10f, 11f, 2f, 1f)
        }
        TileType.TREE -> {
            fill(GRASS)
            rect(WOOD, 7f, 7f, 2f, 8f)
            rect(LEAVES, 3f, 1f, 10f, 3f)
            rect(LEAVES, 1f, 4f, 14f, 3f)
            rect(LEAVES_LIGHT, 4f, 2f, 3f, 1f)
        }
        TileType.ROCK -> {
            fill(SAND)
            rect(ROCK, 3f, 6f, 10f, 8f)
            rect(ROCK_LIGHT, 4f, 7f, 4f, 2f)
            rect(ROCK_DARK, 3f, 12f, 10f, 2f)
        }
        TileType.FIRE -> {
            fill(SAND)
            val flicker = ((timeMs / 150L) % 3L).toFloat()
            rect(Color(0xFF6B4423), 3f, 12f, 10f, 2f)
            rect(Color(0xFF6B4423), 5f, 11f, 6f, 1f)
            rect(Color(0xFFF28C28), 5f, 6f + flicker, 6f, 5f - flicker)
            rect(Color(0xFFFFD84D), 7f, 8f + flicker, 2f, 3f - flicker)
        }
        TileType.HUT -> {
            fill(Color(0xFF9C6B3F))
            rect(Color(0xFF7A5230), 0f, 5f, 16f, 1f)
            rect(Color(0xFF7A5230), 0f, 10f, 16f, 1f)
            if (roof) {
                rect(IslaColors.Primary, 0f, 0f, 16f, 7f)
                rect(IslaColors.Ink, 0f, 6f, 16f, 1f)
            }
        }
    }
}

/** Persona de 1 casilla de alto con el pie en [feetY]. */
private fun DrawScope.drawPerson(centerX: Float, feetY: Float, tile: Float, actor: Actor) {
    val u = tile / 16f
    fun rect(color: Color, x: Float, y: Float, w: Float, h: Float) =
        drawRect(color, Offset(centerX + x * u, feetY + y * u), Size(w * u, h * u))

    rect(Color(0x33000000), -5f, -1f, 10f, 2f)          // sombra
    rect(Color(0xFF2B2B3A), -3f, -4f, 6f, 4f)           // piernas
    rect(actor.body, -4f, -10f, 8f, 6f)                 // cuerpo
    rect(SKIN, -3f, -15f, 6f, 5f)                       // cabeza
    rect(actor.hair, -3f, -16f, 6f, 2f)                 // pelo
    when (actor.facing) {                               // ojos según hacia dónde mira
        Direction.DOWN -> { rect(Color.Black, -2f, -13f, 1f, 1f); rect(Color.Black, 1f, -13f, 1f, 1f) }
        Direction.LEFT -> rect(Color.Black, -2f, -13f, 1f, 1f)
        Direction.RIGHT -> rect(Color.Black, 1f, -13f, 1f, 1f)
        Direction.UP -> rect(actor.hair, -3f, -15f, 6f, 3f)
    }
}

private fun DrawScope.drawLabel(
    measurer: TextMeasurer,
    text: String,
    centerX: Float,
    top: Float,
    style: TextStyle,
) {
    val layout = measurer.measure(text, style)
    val w = layout.size.width.toFloat()
    val h = layout.size.height.toFloat()
    drawRect(Color(0xB31F2A2C), Offset(centerX - w / 2f - 4f, top - 2f), Size(w + 8f, h + 4f))
    drawText(layout, color = Color.White, topLeft = Offset(centerX - w / 2f, top))
}

/** Signo de exclamación sobre el personaje con el que se puede hablar. */
private fun DrawScope.drawMarker(
    measurer: TextMeasurer,
    centerX: Float,
    top: Float,
    tile: Float,
    style: TextStyle,
) {
    val u = tile / 16f
    val layout = measurer.measure("!", style)
    val side = 10f * u
    drawRect(IslaColors.Ink, Offset(centerX - side / 2f - 2f, top - 2f), Size(side + 4f, side + 4f))
    drawRect(IslaColors.Paper, Offset(centerX - side / 2f, top), Size(side, side))
    drawText(
        layout,
        color = IslaColors.Action,
        topLeft = Offset(centerX - layout.size.width / 2f, top + (side - layout.size.height) / 2f),
    )
}
