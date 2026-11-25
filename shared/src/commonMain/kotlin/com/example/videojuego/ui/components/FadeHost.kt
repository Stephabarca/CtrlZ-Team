package com.example.videojuego.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Cambia de pantalla con un fundido a negro: cuando [target] cambia, la pantalla actual se oscurece,
 * se cambia por la nueva y se aclara. Mientras dura el fundido bloquea los toques para no pulsar nada por error.
 */
@Composable
fun <T> FadeHost(
    target: T,
    modifier: Modifier = Modifier,
    fadeMs: Int = 260,
    content: @Composable (T) -> Unit,
) {
    var shown by remember { mutableStateOf(target) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(target) {
        if (target != shown) {
            alpha.animateTo(1f, tween(fadeMs))
            shown = target
        }
        alpha.animateTo(0f, tween(fadeMs + 90))
    }

    Box(modifier.fillMaxSize()) {
        content(shown)
        if (alpha.value > 0.001f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha.value))
                    .pointerInput(Unit) { detectTapGestures { } },
            )
        }
    }
}
