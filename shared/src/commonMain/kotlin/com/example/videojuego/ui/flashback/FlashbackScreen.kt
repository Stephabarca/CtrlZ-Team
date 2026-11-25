package com.example.videojuego.ui.flashback

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import videojuego.shared.generated.resources.Res
import videojuego.shared.generated.resources.*
import kotlin.math.max

private const val CHARS_PER_SECOND = 38f
private const val FADE_IN_MS = 700f

/**
 * Recuerdo animado de un personaje antes del viaje: escenas con tono sepia que cambian solas (o al tocar),
 * con la narración letra por letra. Se puede saltar en cualquier momento con SALTAR (o Esc).
 *
 * Toca o pulsa Enter/Espacio: la primera vez completa el texto y la segunda pasa a la siguiente escena.
 *
 * @param onFinished se llama al terminar la última escena o al saltar.
 */
@Composable
fun FlashbackScreen(characterId: String, onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val scenes = remember(characterId) { FlashbackData.scenesFor(characterId) }
    val currentFinished by rememberUpdatedState(onFinished)
    if (scenes.isEmpty()) {
        LaunchedEffect(Unit) { currentFinished() }
        return
    }

    val painter = when (characterId) {
        "valentina" -> painterResource(Res.drawable.valentina_oliva_4)
        "lucas" -> painterResource(Res.drawable.lucas_ambar_4)
        else -> painterResource(Res.drawable.lila_ariza_4)
    }
    val displayName = when (characterId) {
        "valentina" -> "Valentina"
        "lucas" -> "Lucas"
        else -> "Lila"
    }

    var t by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        val t0 = withFrameMillis { it }
        while (true) t = withFrameMillis { it } - t0
    }

    var index by remember { mutableStateOf(0) }
    var sceneStart by remember { mutableStateOf(0L) }
    val scene = scenes[index]

    fun typedChars(): Int = (max(0L, t - sceneStart) / 1000f * CHARS_PER_SECOND).toInt().coerceAtMost(scene.text.length)

    fun goNext() {
        if (index < scenes.lastIndex) {
            index++
            sceneStart = t
        } else {
            currentFinished()
        }
    }

    // Si nadie toca la pantalla, la escena pasa sola cuando se terminó de leer.
    LaunchedEffect(index) {
        delay(FlashbackData.durationMs(scene))
        goNext()
    }

    fun tap() {
        if (typedChars() < scene.text.length) {
            sceneStart = t - 100_000L // primero se completa el texto
        } else {
            goNext()
        }
    }

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focus)
            .focusable()
            .onKeyEvent {
                if (it.type != KeyEventType.KeyDown) {
                    false
                } else when (it.key) {
                    Key.Enter, Key.Spacebar -> { tap(); true }
                    Key.Escape -> { currentFinished(); true }
                    else -> false
                }
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { tap() },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val sceneMs = max(0L, t - sceneStart)
            drawFlashbackScene(scene.art, t, sceneMs, painter)
            // Entrada desde negro al cambiar de escena.
            val fade = (1f - sceneMs / FADE_IN_MS).coerceIn(0f, 1f)
            if (fade > 0f) drawRect(Color.Black.copy(alpha = fade), size = size)
        }

        // Quién recuerda y dónde.
        Column(
            Modifier.align(Alignment.TopStart).safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "RECUERDO DE ${displayName.uppercase()}",
                modifier = Modifier.background(Color(0x99000000), RectangleShape).padding(horizontal = 8.dp, vertical = 3.dp),
                color = Color(0xFFFFE9B8), fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp,
            )
            Text(
                scene.place,
                modifier = Modifier.background(Color(0x99000000), RectangleShape).padding(horizontal = 8.dp, vertical = 3.dp),
                color = Color.White, fontFamily = PixelFontFamily, fontSize = 12.sp,
            )
        }

        // Saltar.
        Text(
            "SALTAR ▸",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(16.dp)
                .border(2.dp, Color(0xFFFFE9B8), RectangleShape)
                .background(Color(0x99000000), RectangleShape)
                .clickable { currentFinished() }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            color = Color(0xFFFFE9B8), fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp,
        )

        // Narración y puntos de avance.
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .fillMaxWidth(0.94f)
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                scenes.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == index) 10.dp else 7.dp)
                            .background(if (i <= index) Color(0xFFFFE9B8) else Color(0x66FFFFFF), RectangleShape),
                    )
                }
            }
            Text(
                scene.text.take(typedChars()),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, IslaColors.Ink, RectangleShape)
                    .background(IslaColors.Paper, RectangleShape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 13.sp, lineHeight = 19.sp,
            )
        }
    }
}
