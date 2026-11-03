package com.example.videojuego

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.videojuego.ui.intro.IntroScreen
import com.example.videojuego.ui.round.QuestionRoundHost
import com.example.videojuego.ui.theme.IslaPerdidaTheme
import com.example.videojuego.ui.world.WorldScreen
import com.example.videojuego.world.IslaWorld
import com.example.videojuego.world.WorldController

@Composable
@Preview
fun App() {
    IslaPerdidaTheme {
        val world = remember {
            WorldController(IslaWorld.map, IslaWorld.npcs, IslaWorld.PLAYER_START_X, IslaWorld.PLAYER_START_Y)
        }
        var screen by remember { mutableStateOf("intro") }
        var nightId by remember { mutableStateOf("night1") }

        when (screen) {
            "intro" -> IntroScreen(onFinished = { screen = "world" })
            "world" -> WorldScreen(
                controller = world,
                onNightStart = { id ->
                    nightId = id
                    screen = "round"
                },
                onGift = { gift ->
                    // TODO: sumar gift.affinityDelta a la afinidad de gift.npcId (sistema de afinidad)
                },
            )
            else -> QuestionRoundHost(
                nightId = nightId,
                onFinished = {
                    // Amanece: día nuevo. Después de la noche 3 devuelve false (aquí iría el final del juego).
                    world.startNextDay()
                    screen = "world"
                },
                onBack = {
                    world.cancelNight()
                    screen = "world"
                },
            )
        }
    }
}