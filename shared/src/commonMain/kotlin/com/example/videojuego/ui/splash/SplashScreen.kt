package com.example.videojuego.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import videojuego.shared.generated.resources.Res

/** Archivo de la animación de inicio (dentro de composeResources). */
const val INTRO_VIDEO_PATH = "files/video/control_z_intro.mp4"

/** Si el video no avisa que terminó (por cualquier falla), se pasa al menú igual después de este tiempo. */
private const val MAX_SPLASH_MS = 15_000L

/**
 * Animación de CONTROL Z que se muestra al abrir el juego, antes del menú. Se salta tocando la pantalla
 * (o con una tecla en escritorio). Si el video no se puede leer o reproducir, se va directo al menú.
 * [volume] es el volumen de música de Ajustes.
 */
@Composable
fun SplashScreen(volume: Float, onFinished: () -> Unit) {
    var done by remember { mutableStateOf(false) }
    val finish = {
        if (!done) {
            done = true
            onFinished()
        }
    }

    var bytes by remember { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(Unit) {
        bytes = try {
            Res.readBytes(INTRO_VIDEO_PATH)
        } catch (e: Exception) {
            null
        }
        if (bytes == null) finish()
    }
    LaunchedEffect(Unit) {
        delay(MAX_SPLASH_MS)
        finish()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { finish() },
    ) {
        bytes?.let { VideoPlayer(bytes = it, volume = volume, onFinished = finish, modifier = Modifier.fillMaxSize()) }
    }
}

/**
 * Reproduce un video mp4 (H.264 + AAC) una vez, ajustado a la pantalla sin deformarlo, y llama a [onFinished]
 * al terminar, si falla, o si el jugador toca el video. Cada plataforma tiene su implementación.
 */
@Composable
expect fun VideoPlayer(bytes: ByteArray, volume: Float, onFinished: () -> Unit, modifier: Modifier = Modifier)
