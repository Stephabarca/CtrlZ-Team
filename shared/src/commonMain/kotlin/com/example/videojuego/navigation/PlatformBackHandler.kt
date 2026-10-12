package com.example.videojuego.navigation

import androidx.compose.runtime.Composable

/** Intercepta el botón atrás del sistema. Cada plataforma provee su implementación. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
