package com.example.videojuego.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import videojuego.shared.generated.resources.Res

/**
 * Hace sonar [track] en bucle con [volume] (el de Ajustes). Si [track] es null calla la música.
 * Pausa al salir de la app y sigue al volver. Si el archivo no se puede leer, el juego sigue en silencio.
 */
@Composable
fun BackgroundMusic(track: MusicTrack?, volume: Float) {
    val player = remember { createMusicPlayer() }
    DisposableEffect(player) { onDispose { player.stop() } }

    LaunchedEffect(track) {
        if (track == null) {
            player.stop()
        } else {
            val bytes = try {
                Res.readBytes(track.path)
            } catch (e: Exception) {
                null
            }
            if (bytes != null) player.play(bytes, volume)
        }
    }
    LaunchedEffect(volume) { player.setVolume(volume) }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { player.pause() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { player.resume() }
}
