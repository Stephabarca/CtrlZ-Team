package com.example.videojuego

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.example.videojuego.data.GameServices
import com.example.videojuego.data.content.ContentValidator
import com.example.videojuego.ui.intro.IntroScreen
import com.example.videojuego.narrative.IntroScript
import com.example.videojuego.ui.components.DialogueEngineView
import com.example.videojuego.ui.theme.IslaPerdidaTheme
import com.example.videojuego.ui.demo.DialogueEngineDemo
import com.example.videojuego.ui.theme.IslaPerdidaTheme

@Composable
@Preview
fun App() {
    LaunchedEffect(Unit) {
        val content = GameServices.content.load()
        val problemas = ContentValidator.validate(content)
        println("ISLA_DATOS noches=${content.nights.size} problemas=$problemas")
    }
    IslaPerdidaTheme {
        IntroScreen(onFinished = { })
        //DialogueEngineDemo()
    }
}