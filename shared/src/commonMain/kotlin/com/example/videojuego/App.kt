package com.example.videojuego

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.videojuego.ui.intro.IntroScreen
import com.example.videojuego.ui.theme.IslaPerdidaTheme

@Composable
@Preview
fun App() {
    IslaPerdidaTheme {
        IntroScreen(
            onFinished = { result ->
                // Más adelante: navegar a la Noche 1 (IP-4)
            },
        )
    }
}