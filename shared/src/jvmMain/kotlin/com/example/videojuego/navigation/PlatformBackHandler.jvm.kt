package com.example.videojuego.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Escritorio no tiene botón atrás del sistema.
}