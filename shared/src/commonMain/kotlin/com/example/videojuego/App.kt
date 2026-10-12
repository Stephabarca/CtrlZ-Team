package com.example.videojuego

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.videojuego.navigation.AppNavHost

/** Punto de entrada común de la UI. [onExit] lo provee cada plataforma. */
@Composable
fun App(onExit: () -> Unit = {}) {
    MaterialTheme {
        AppNavHost(onExit = onExit)
    }
}
