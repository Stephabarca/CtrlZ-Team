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
import com.example.videojuego.data.rules.WorldRewards
import com.example.videojuego.data.save.GamePhase
import com.example.videojuego.data.save.GameSettings
import com.example.videojuego.data.save.LoadResult
import com.example.videojuego.data.save.WorldSave
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.demo.DEMO_KEYS
import com.example.videojuego.demo.DemoCommand
import com.example.videojuego.demo.DemoKeyMap
import com.example.videojuego.diary.DiaryBuilder
import com.example.videojuego.finale.FinaleRules
import com.example.videojuego.report.ReportBuilder
import com.example.videojuego.ui.report.ReportScreen
import com.example.videojuego.ui.storm.StormScreen
import com.example.videojuego.ui.title.TitleCard
import com.example.videojuego.ui.title.TitleCardScreen
import com.example.videojuego.ui.title.TitleCards
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import com.example.videojuego.menu.MainMenuLogic
import com.example.videojuego.minigame.build.BuildGame
import com.example.videojuego.minigame.build.BuildStats
import com.example.videojuego.minigame.fishing.FishingStats
import com.example.videojuego.minigame.hydrohopper.HydroHopperStats
import com.example.videojuego.summary.NightSummary
import com.example.videojuego.summary.NightSummaryBuilder
import com.example.videojuego.ui.build.BuildScreen
import com.example.videojuego.ui.components.FadeHost
import com.example.videojuego.ui.credits.CreditsScreen
import com.example.videojuego.ui.demo.HydroHopperDemo
import com.example.videojuego.ui.finale.EndingsScreen
import com.example.videojuego.ui.finale.FinaleScreen
import com.example.videojuego.ui.finale.FinaleStrings
import com.example.videojuego.ui.flashback.FlashbackData
import com.example.videojuego.ui.flashback.FlashbackScreen
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
import com.example.videojuego.ui.splash.SplashScreen
import com.example.videojuego.ui.summary.NightSummaryScreen
import com.example.videojuego.ui.theme.IslaPerdidaTheme
import com.example.videojuego.ui.world.WorldScreen
import com.example.videojuego.world.IslaWorld
import com.example.videojuego.world.ItemType
import com.example.videojuego.world.MemoryFlags

/** "#CAD988" -> Color. Si el texto no es válido se usa gris. */
private fun parseColor(hex: String): Color =
    hex.removePrefix("#").toLongOrNull(16)?.let { Color(0xFF000000L or it) } ?: Color.Gray

@Composable
@Preview
fun App() {
    IslaPerdidaTheme {
        val world = remember { IslaWorld.newController() }
        // Empieza con la animación de CONTROL Z; al terminar (o al saltarla) pasa al menú.
        var screen by remember { mutableStateOf("splash") }
        var nightId by remember { mutableStateOf("night1") }

        // Ajustes (velocidad del texto y volúmenes) y a qué pantalla volver al cerrarlos.
        var settings by remember { mutableStateOf<GameSettings>(GameServices.settings.load()) }
        var settingsReturn by remember { mutableStateOf("menu") }

        // Contenido del juego (JSON), cargado una vez.
        var content by remember { mutableStateOf<GameContent?>(null) }

        // Progreso: el actual, el de cuando empezó la noche (para el "antes y después") y el resultado de la pesca.
        var save by remember { mutableStateOf<SaveGame?>(null) }
        var nightStartSave by remember { mutableStateOf<SaveGame?>(null) }
        var fishing by remember { mutableStateOf<FishingStats?>(null) }
        var building by remember { mutableStateOf<BuildStats?>(null) }
        var hydroHopper by remember { mutableStateOf<HydroHopperStats?>(null) }
        // Recuerdo del personaje antes del viaje: cuál se está mostrando y cuáles ya se vieron en esta sesión.
        var flashbackFor by remember { mutableStateOf<String?>(null) }
        var flashbacksSeen by remember { mutableStateOf(emptySet<String>()) }
        // Final obtenido (para mostrarlo en los créditos) y finales vistos (galería del menú).
        var endingId by remember { mutableStateOf<String?>(null) }
        var endingsSeen by remember { mutableStateOf(GameServices.endings.seen()) }
        var summary by remember { mutableStateOf<NightSummary?>(null) }
        // Tarjeta de título (DÍA / NOCHE) y a qué pantalla se pasa al terminar.
        var titleCard by remember { mutableStateOf<TitleCard?>(null) }
        var afterTitle by remember { mutableStateOf("world") }
        // Final forzado desde las teclas de demo (null = el que decide el juego).
        var forcedEnding by remember { mutableStateOf<String?>(null) }

        // Al abrir el juego: carga el contenido y lee la partida guardada para el menú.
        var loadResult by remember { mutableStateOf<LoadResult?>(null) }
        LaunchedEffect(Unit) {
            content = GameServices.content.load()
            loadResult = GameServices.saves.load()
        }
        // Efectos de sonido: se leen una vez y usan el volumen de efectos de Ajustes.
        LaunchedEffect(Unit) { Sfx.preload() }
        Sfx.volume = settings.sfxVolume

        fun showSummary(
            c: GameContent,
            endSave: SaveGame,
            stats: FishingStats? = null,
            buildStats: BuildStats? = null,
            hydroStats: HydroHopperStats? = null,
        ) {
            val night = c.nights.first { it.id == nightId }
            summary = NightSummaryBuilder.build(
                content = c,
                night = night,
                saveAtNightStart = nightStartSave ?: ProgressRules.newGame(c),
                saveAtEnd = endSave,
                fishing = stats,
                building = buildStats,
                hydroHopper = hydroStats,
            )
            screen = "summary"
        }

        fun showTitle(card: TitleCard, next: String) {
            titleCard = card
            afterTitle = next
            screen = "title"
        }

        // Empieza una noche: tarjeta de título y luego el recuerdo del personaje (una vez, saltable) o la ronda.
        fun startNight(id: String) {
            nightId = id
            nightStartSave = save
            fishing = null
            building = null
            hydroHopper = null
            val night = content?.nights?.firstOrNull { it.id == id }
            val showFlashback = night != null && FlashbackData.shouldPlay(
                characterId = night.characterId,
                nightOrder = night.order,
                answeredQuestionIds = save?.answers.orEmpty().map { it.questionId },
                seen = flashbacksSeen,
            )
            if (showFlashback && night != null) flashbackFor = night.characterId
            val next = if (showFlashback) "flashback" else "round"
            if (night != null) showTitle(TitleCards.night(night.order, night.title), next) else screen = next
        }

        // Atajos ocultos de demo: Ctrl+Shift+tecla.
        val demoKeys = if (!DEMO_KEYS) Modifier else Modifier.onPreviewKeyEvent { e ->
            if (e.type != KeyEventType.KeyDown || !e.isCtrlPressed || !e.isShiftPressed) return@onPreviewKeyEvent false
            val ch = when (e.key) {
                Key.Two -> '2'; Key.Three -> '3'; Key.S -> 'S'; Key.V -> 'V'; Key.U -> 'U'
                Key.L -> 'L'; Key.A -> 'A'; Key.F -> 'F'; Key.R -> 'R'
                else -> return@onPreviewKeyEvent false
            }
            val cmd = DemoKeyMap.commandFor(ch) ?: return@onPreviewKeyEvent false
            when (cmd) {
                is DemoCommand.Night -> if (content != null) startNight("night${cmd.order}")
                DemoCommand.Storm -> screen = "storm"
                is DemoCommand.Finale -> {
                    forcedEnding = cmd.endingId
                    screen = "finale"
                }
                DemoCommand.Report -> screen = "report"
            }
            true
        }

        // Fondo: música del menú, playa de día o de noche (mapa) y reloj (preguntas).
        // Ajustes sigue con lo de la pantalla de origen. Usa el volumen de música de Ajustes.
        val worldState by world.state.collectAsState()

        // Cada cambio de pantalla se hace con un fundido a negro.
        Box(demoKeys) {
        FadeHost(screen) { shownScreen ->
        // La música cambia junto con la imagen, cuando la pantalla está a oscuras.
        BackgroundMusic(MusicRules.trackFor(shownScreen, settingsReturn, worldState.phase), settings.musicVolume)
        when (shownScreen) {
            "splash" -> SplashScreen(volume = settings.musicVolume, onFinished = { screen = "menu" })

            "hydrohopper_demo" -> HydroHopperDemo(onFinished = { screen = "menu" })

            "menu" -> MainMenuScreen(
                info = loadResult?.let { MainMenuLogic.info(it) },
                onContinue = {
                    val loaded = (loadResult as? LoadResult.Loaded)?.save
                    if (loaded != null) {
                        save = loaded
                        world.restore(loaded.world ?: WorldSave())
                        world.setMemory(MemoryFlags.from(loaded))
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
                    building = null
                    hydroHopper = null
                    flashbacksSeen = emptySet()
                    summary = null
                    world.restore(WorldSave())
                    world.setMemory(emptySet())
                    forcedEnding = null
                    screen = "storm"
                },
                onSettings = {
                    settingsReturn = "menu"
                    screen = "settings"
                },
                onCredits = { screen = "credits" },
                onEndings = {
                    endingsSeen = GameServices.endings.seen()
                    screen = "endings"
                },
            )

            "settings" -> SettingsScreen(
                initial = settings,
                onChange = {
                    settings = it
                    GameServices.settings.save(it)
                },
                onBack = { screen = settingsReturn },
            )

            "credits" -> CreditsScreen(
                onBack = {
                    endingId = null
                    screen = "menu"
                },
                endingTitle = endingId?.let { FinaleStrings.endingTitle(it) },
            )

            "endings" -> EndingsScreen(seen = endingsSeen, onBack = { screen = "menu" })

            "storm" -> StormScreen(onFinished = { screen = "intro" })

            "title" -> titleCard?.let { card ->
                TitleCardScreen(card = card, onFinished = { screen = afterTitle })
            }

            "intro" -> IntroScreen(onFinished = { showTitle(TitleCards.day(1, IslaWorld.npcForNight("night1")?.name), "world") })

            "world" -> WorldScreen(
                controller = world,
                onNightStart = { id -> startNight(id) },
                diary = { content?.let { DiaryBuilder.build(it, save) }.orEmpty() },
                onGift = { gift ->
                    // Un regalo suma un poco de afinidad (+1 o +3). Se guarda al dormir en la cama.
                    content?.let { c ->
                        save = WorldRewards.addAffinity(c, save ?: ProgressRules.newGame(c), gift.npcId, gift.affinityDelta)
                    }
                },
                onGoalDone = { goal ->
                    // Ayudar con el objetivo del día suma lo mismo que un regalo.
                    content?.let { c ->
                        save = WorldRewards.addAffinity(c, save ?: ProgressRules.newGame(c), goal.npcId, goal.affinityDelta)
                    }
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

            "flashback" -> FlashbackScreen(
                characterId = flashbackFor.orEmpty(),
                onFinished = {
                    flashbackFor?.let { flashbacksSeen = flashbacksSeen + it }
                    screen = "round"
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
                        // Noche 1: pesca. Noche 2: armar la fogata. Noche 3: rescate en la balsa (Hydro Hopper).
                        night?.minigameId == "pesca" -> screen = "fishing"
                        night?.minigameId == "fogata" -> screen = "build"
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
                            // Los pescados capturados se quedan en la mochila (y el sobrante en el cofre).
                            world.addToBag(ItemType.FISH, stats.fishCaught)
                            showSummary(c, updated, stats)
                        },
                    )
                }
            }

            "build" -> {
                val c = content
                val night = c?.nights?.firstOrNull { it.id == nightId }
                val current = save
                if (c != null && night != null && current != null) {
                    val minigame = c.minigame(night.minigameId)
                    val character = c.character(night.characterId)
                    BuildScreen(
                        characterName = character.name.substringBefore(' '),
                        characterColor = parseColor(character.accentColor),
                        characterId = night.characterId,
                        durationSec = minigame.durationSec,
                        level = BuildGame.levelFor(minigame.difficulty),
                        onFinished = { stats ->
                            val updated = ProgressRules.recordMinigame(c, current, minigame, stats.score)
                            save = updated
                            GameServices.saves.save(updated)
                            building = stats
                            showSummary(c, updated, null, stats)
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
                        musicVolume = settings.musicVolume,
                        onFinished = { stats ->
                            val updated = ProgressRules.recordMinigame(c, current, minigame, stats.score)
                            save = updated
                            GameServices.saves.save(updated)
                            hydroHopper = stats
                            showSummary(c, updated, null, null, stats)
                        },
                    )
                }
            }

            "finale" -> {
                val c = content
                val current = save
                // Compatibilidad = afinidad de todo el juego; si empatan, gana quien tenga mejor minijuego.
                val result = remember(current, c) {
                    val start = c?.config?.startAffinity ?: 0
                    val tie = c?.nights.orEmpty().associate { n ->
                        n.characterId to (current?.minigameResults?.lastOrNull { it.minigameId == n.minigameId }?.score ?: 0)
                    }
                    forcedEnding?.let { FinaleRules.forced(it) } ?: FinaleRules.decide(
                        affinity = current?.affinity.orEmpty(),
                        tieBreak = tie,
                        startAffinity = start,
                    )
                }
                // La historia terminó: se guarda la partida como terminada para que el menú ya no ofrezca
                // CONTINUAR (si no, volvería al mapa después de la noche 3). Un final forzado con las teclas
                // de demo no toca la partida.
                LaunchedEffect(Unit) {
                    if (forcedEnding == null && c != null && current != null && current.phase != GamePhase.ENDING) {
                        val done = ProgressRules.finish(c, current)
                        save = done
                        GameServices.saves.save(done)
                        loadResult = LoadResult.Loaded(done)
                    }
                }
                FinaleScreen(
                    result = result,
                    onFinished = {
                        // Se guarda el final visto (aparte de la partida) y se muestra en los créditos.
                        GameServices.endings.record(result.endingId)
                        endingsSeen = GameServices.endings.seen()
                        endingId = result.endingId
                        forcedEnding = null
                        // Antes de los créditos, el informe de cómo se relacionó el jugador.
                        screen = "report"
                    },
                )
            }

            "report" -> content?.let { c ->
                ReportScreen(
                    report = remember(save, c) { ReportBuilder.build(c, save) },
                    onContinue = { screen = "credits" },
                )
            }

            else -> summary?.let { s ->
                NightSummaryScreen(
                    summary = s,
                    onContinue = {
                        // Amanece: los personajes recuerdan lo de anoche (respuestas y minijuego).
                        world.setMemory(MemoryFlags.from(save))
                        if (nightId == "night3") {
                            // Fin de la noche 3: escena del barco y elección de pareja.
                            screen = "finale"
                        } else {
                            world.startNextDay()
                            // Tarjeta del nuevo día: "DÍA 2 · Con Lucas".
                            val day = world.state.value.day
                            showTitle(TitleCards.day(day, IslaWorld.npcForNight("night$day")?.name), "world")
                        }
                    },
                )
            }
        }
        } // FadeHost
        } // Box
    }
}
