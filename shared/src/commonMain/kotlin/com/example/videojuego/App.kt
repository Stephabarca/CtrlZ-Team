package com.example.videojuego

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.videojuego.data.GameServices
import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.data.save.GameSettings
import com.example.videojuego.data.save.LoadResult
import com.example.videojuego.data.save.WorldSave
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.menu.MainMenuLogic
import com.example.videojuego.minigame.fishing.FishingStats
import com.example.videojuego.minigame.hydrohopper.HydroHopperStats
import com.example.videojuego.summary.NightSummary
import com.example.videojuego.summary.NightSummaryBuilder
import com.example.videojuego.ui.credits.CreditsScreen
import com.example.videojuego.ui.demo.HydroHopperDemo
import com.example.videojuego.ui.fishing.FishingScreen
import com.example.videojuego.ui.intro.IntroScreen
import com.example.videojuego.ui.menu.MainMenuScreen
import com.example.videojuego.ui.minigame.HydroHopperScreen
import com.example.videojuego.ui.round.QuestionRoundHost
import com.example.videojuego.audio.BackgroundMusic
import com.example.videojuego.audio.MusicRules
import com.example.videojuego.audio.Sfx
import androidx.compose.runtime.collectAsState
import com.example.videojuego.ui.settings.SettingsScreen
import com.example.videojuego.ui.summary.NightSummaryScreen
import com.example.videojuego.ui.theme.IslaPerdidaTheme
import com.example.videojuego.ui.world.WorldScreen
import com.example.videojuego.world.IslaWorld

/** "#CAD988" -> Color. Si el texto no es válido se usa gris. */
private fun parseColor(hex: String): Color =
    hex.removePrefix("#").toLongOrNull(16)?.let { Color(0xFF000000L or it) } ?: Color.Gray

@Composable
@Preview
fun App() {
    IslaPerdidaTheme {
        val world = remember { IslaWorld.newController() }
        var screen by remember { mutableStateOf("menu") }
        var nightId by remember { mutableStateOf("night1") }

        // Ajustes (velocidad del texto y volúmenes) y a qué pantalla volver al cerrarlos.
        var settings by remember { mutableStateOf<GameSettings>(GameServices.settings.load()) }
        var settingsReturn by remember { mutableStateOf("menu") }

        // Contenido del juego (JSON), cargado una vez.
        var content by remember { mutableStateOf<GameContent?>(null) }

        // Progreso: el actual, el de cuando empezó la noche (para el "antes y después") y el resultado de los minijuegos.
        var save by remember { mutableStateOf<SaveGame?>(null) }
        var nightStartSave by remember { mutableStateOf<SaveGame?>(null) }
        var fishing by remember { mutableStateOf<FishingStats?>(null) }
        var hydroHopper by remember { mutableStateOf<HydroHopperStats?>(null) }
        var summary by remember { mutableStateOf<NightSummary?>(null) }

        // Al abrir el juego: carga el contenido y lee la partida guardada para el menú.
        var loadResult by remember { mutableStateOf<LoadResult?>(null) }
        LaunchedEffect(Unit) {
            content = GameServices.content.load()
            loadResult = GameServices.saves.load()
        }
        // Efectos de sonido: se leen una vez y usan el volumen de efectos de Ajustes.
        LaunchedEffect(Unit) { Sfx.preload() }
        Sfx.volume = settings.sfxVolume

        fun showSummary(c: GameContent, endSave: SaveGame, stats: FishingStats? = null, hydroStats: HydroHopperStats? = null) {
            val night = c.nights.first { it.id == nightId }
            summary = NightSummaryBuilder.build(
                content = c,
                night = night,
                saveAtNightStart = nightStartSave ?: ProgressRules.newGame(c),
                saveAtEnd = endSave,
                fishing = stats,
                hydroHopper = hydroStats,
            )
            screen = "summary"
        }

        // Fondo: música del menú (menú y créditos), playa de día o de noche (mapa) y reloj (preguntas).
        // Ajustes sigue con lo de la pantalla de origen. Usa el volumen de música de Ajustes.
        val worldState by world.state.collectAsState()
        BackgroundMusic(MusicRules.trackFor(screen, settingsReturn, worldState.phase), settings.musicVolume)

        when (screen) {
            "hydrohopper_demo" -> HydroHopperDemo(onFinished = { screen = "menu" })

            "menu" -> MainMenuScreen(
                info = loadResult?.let { MainMenuLogic.info(it) },
                onContinue = {
                    val loaded = (loadResult as? LoadResult.Loaded)?.save
                    if (loaded != null) {
                        save = loaded
                        world.restore(loaded.world ?: WorldSave())
                        screen = "world"
                    }
                },
                onNewGame = {
                    // Borra la partida anterior y empieza desde la introducción.
                    GameServices.saves.clear()
                    loadResult = LoadResult.NoSave
                    save = null
                    nightStartSave = null
                    fishing = null
                    summary = null
                    world.restore(WorldSave())
                    screen = "intro"
                },
                onSettings = {
                    settingsReturn = "menu"
                    screen = "settings"
                },
                onCredits = { screen = "credits" },
            )

            "settings" -> SettingsScreen(
                initial = settings,
                onChange = {
                    settings = it
                    GameServices.settings.save(it)
                },
                onBack = { screen = settingsReturn },
            )

            "credits" -> CreditsScreen(onBack = { screen = "menu" })

            "intro" -> IntroScreen(onFinished = { screen = "world" })

            "world" -> WorldScreen(
                controller = world,
                onNightStart = { id ->
                    nightId = id
                    nightStartSave = save
                    fishing = null
                    screen = "round"
                },
                onGift = { gift ->
                    // TODO: sumar gift.affinityDelta a la afinidad de gift.npcId (sistema de afinidad)
                },
                onBackToMenu = {
                    // Se vuelve a leer el guardado para que el menú muestre CONTINUAR con lo último que se guardó.
                    loadResult = GameServices.saves.load()
                    screen = "menu"
                },
                onOpenSettings = {
                    settingsReturn = "world"
                    screen = "settings"
                },
                onSleep = {
                    // Dormir en la cama: guarda el progreso (afinidad, respuestas) y el mapa (día, mochila, cofre).
                    val base = save ?: content?.let { ProgressRules.newGame(it) } ?: SaveGame()
                    val updated = base.copy(world = world.toWorldSave())
                    save = updated
                    GameServices.saves.save(updated)
                },
            )

            "round" -> QuestionRoundHost(
                nightId = nightId,
                save = save,
                onFinished = { result ->
                    save = result.save
                    val c = content
                    val night = c?.nights?.firstOrNull { it.id == nightId }
                    when {
                        c == null -> screen = "world"
                        night?.minigameId == "pesca" -> screen = "fishing"
                        night?.minigameId == "hydrohopper" || night?.minigameId == "memoria" -> screen = "hydrohopper"
                        else -> showSummary(c, result.save, null)
                    }
                },
                onBack = {
                    world.cancelNight()
                    screen = "world"
                },
            )

            "fishing" -> {
                val c = content
                val night = c?.nights?.firstOrNull { it.id == nightId }
                val current = save
                if (c != null && night != null && current != null) {
                    val minigame = c.minigame(night.minigameId)
                    val character = c.character(night.characterId)
                    FishingScreen(
                        characterName = character.name,
                        characterColor = parseColor(character.accentColor),
                        durationSec = minigame.durationSec,
                        onFinished = { stats ->
                            val updated = ProgressRules.recordMinigame(c, current, minigame, stats.score)
                            save = updated
                            GameServices.saves.save(updated)
                            fishing = stats
                            showSummary(c, updated, stats)
                        },
                    )
                }
            }

            "hydrohopper" -> {
                val c = content
                val night = c?.nights?.firstOrNull { it.id == nightId }
                val current = save
                if (c != null && night != null && current != null) {
                    val minigame = c.minigame(night.minigameId)
                    HydroHopperScreen(
                        durationSec = minigame.durationSec,
                        onFinished = { stats ->
                            val updated = ProgressRules.recordMinigame(c, current, minigame, stats.score)
                            save = updated
                            GameServices.saves.save(updated)
                            hydroHopper = stats
                            showSummary(c, updated, null, stats)
                        },
                    )
                }
            }

            else -> summary?.let { s ->
                NightSummaryScreen(
                    summary = s,
                    onContinue = {
                        // Amanece: día nuevo. Después de la noche 3 devuelve false (aquí iría el final del juego).
                        world.startNextDay()
                        screen = "world"
                    },
                )
            }
        }
    }
}
