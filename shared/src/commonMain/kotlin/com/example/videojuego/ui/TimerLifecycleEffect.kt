package com.example.videojuego.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.videojuego.timer.CountdownTimer

/**
 * Enlaza el temporizador con el ciclo de vida: corre mientras la pantalla está
 * visible y se pausa al pasar a segundo plano o al salir de la pantalla.
 */
@Composable
fun TimerLifecycleEffect(timer: CountdownTimer) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { timer.start() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { timer.pause() }
}
