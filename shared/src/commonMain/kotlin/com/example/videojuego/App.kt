package com.example.videojuego

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.example.videojuego.data.GameServices
import com.example.videojuego.data.content.ContentValidator
import com.example.videojuego.navigation.AppNavHost
import com.example.videojuego.ui.theme.IslaPerdidaTheme

@Composable
@Preview
fun App(onExit: () -> Unit = {}) {
    IslaPerdidaTheme {
        AppNavHost(onExit = onExit)
    }
    LaunchedEffect(Unit) {
        runCatching {
            val content = GameServices.content.load()
            println("ISLA_DATOS noches=${content.nights.size} problemas=${ContentValidator.validate(content)}")
        }.onFailure { println("ISLA_DATOS error: ${it.message}") }
    }
}