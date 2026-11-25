package com.example.videojuego.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

/** iOS: por ahora sin video (pendiente AVPlayer); pasa directo al menú. */
@Composable
actual fun VideoPlayer(bytes: ByteArray, volume: Float, onFinished: () -> Unit, modifier: Modifier) {
    LaunchedEffect(Unit) { onFinished() }
}
