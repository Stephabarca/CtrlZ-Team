package com.example.videojuego.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS usa el gesto de deslizar; por ahora no se intercepta.
}
