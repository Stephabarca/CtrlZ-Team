package com.example.videojuego

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.example.videojuego.data.GameServices
import com.example.videojuego.data.content.ContentValidator
import com.example.videojuego.ui.intro.IntroScreen
import com.example.videojuego.ui.theme.IslaPerdidaTheme
import androidx.compose.material3.MaterialTheme
import com.example.videojuego.navigation.AppNavHost

@Composable
@Preview
fun App(onExit: () -> Unit = {}) {
    MaterialTheme {
        AppNavHost(onExit = onExit)
    }
    LaunchedEffect(Unit) {
        val content = GameServices.content.load()
        val problemas = ContentValidator.validate(content)
        println("ISLA_DATOS noches=${content.nights.size} problemas=$problemas")
    }
    IslaPerdidaTheme {
        IntroScreen(onFinished = { })
    }
}