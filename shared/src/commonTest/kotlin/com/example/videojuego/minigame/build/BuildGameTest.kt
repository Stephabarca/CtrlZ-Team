package com.example.videojuego.minigame.build

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildGameTest {

    private fun game(level: Int = 2, durationMs: Long = 45_000L) = BuildGame(durationMs, level, Random(7))

    private fun BuildGame.s() = state.value

    /** Toca la pieza que Lucas pide ahora. */
    private fun BuildGame.tapCorrect() = tap(s().expected!!)

    /** Una pieza buena de la bandeja que NO es la que se pide. */
    private fun BuildGame.wrongUseful() = s().tray.first { !it.decoy && it != s().expected }

    // ---------- Estados ----------

    @Test
    fun startsReady_andDoesNotRunUntilStarted() {
        val g = game()
        assertEquals(BuildPhase.READY, g.s().phase)
        g.update(5_000)
        assertEquals(45_000L, g.s().remainingMs)
        g.tap(Piece.LENA)
        assertEquals(0, g.s().points)
    }

    @Test
    fun start_beginsPlayingWithASequence() {
        val g = game(level = 2)
        g.start()
        assertEquals(BuildPhase.PLAYING, g.s().phase)
        assertEquals(1, g.s().round)
        assertEquals(RoundSpec.forTier(2).length, g.s().sequence.size)
        assertTrue(g.s().expected != null)
    }

    // ---------- Dificultad ----------

    @Test
    fun difficulty_growsWithEachTier() {
        var previous = RoundSpec.forTier(1)
        for (tier in 2..8) {
            val spec = RoundSpec.forTier(tier)
            assertTrue(spec.length >= previous.length)
            assertTrue(spec.kinds >= previous.kinds)
            assertTrue(spec.decoys >= previous.decoys)
            assertTrue(spec.stepMs <= previous.stepMs)
            previous = spec
        }
    }

    @Test
    fun difficulty_hasLimits() {
        val hardest = RoundSpec.forTier(50)
        assertEquals(RoundSpec.MAX_LENGTH, hardest.length)
        assertEquals(RoundSpec.MAX_KINDS, hardest.kinds)
        assertEquals(RoundSpec.MAX_DECOYS, hardest.decoys)
        assertEquals(RoundSpec.MIN_STEP_MS, hardest.stepMs)
        assertTrue(hardest.traySize <= 8)
    }

    @Test
    fun firstRound_isEasy_andLaterTiersAddTrapsAndShuffling() {
        val easy = RoundSpec.forTier(1)
        assertEquals(3, easy.length)
        assertEquals(0, easy.decoys)
        assertFalse(easy.reshuffle)
        val hard = RoundSpec.forTier(3)
        assertEquals(1, hard.decoys)
        assertTrue(hard.reshuffle)
    }

    @Test
    fun levelFor_mapsTheDifficultyOfTheMinigame() {
        assertEquals(1, BuildGame.levelFor("baja"))
        assertEquals(2, BuildGame.levelFor("media"))
        assertEquals(3, BuildGame.levelFor("alta"))
        assertEquals(1, BuildGame.levelFor("desconocida"))
    }

    @Test
    fun higherLevel_startsWithTraps() {
        val g = game(level = 3)
        g.start()
        assertTrue(g.s().tray.any { it.decoy })
        val low = game(level = 1)
        low.start()
        assertTrue(low.s().tray.none { it.decoy })
    }

    // ---------- Reglas ----------

    @Test
    fun correctTap_advancesAndScores() {
        val g = game()
        g.start()
        g.tapCorrect()
        assertEquals(1, g.s().step)
        assertEquals(BuildGame.CORRECT_POINTS, g.s().points)
        assertEquals(StepResult.CORRECT, g.s().lastResult)
    }

    @Test
    fun streak_addsBonusPoints() {
        val g = game(level = 2) // ronda de 4 pasos: tres aciertos no la completan
        g.start()
        repeat(3) { g.tapCorrect() }
        assertEquals(10 + 12 + 14, g.s().points)
        assertEquals(3, g.s().bestStreak)
    }

    @Test
    fun wrongTap_penalizesAndResetsStreak() {
        val g = game()
        g.start()
        g.tapCorrect()
        g.tap(g.wrongUseful())
        assertEquals(10 - BuildGame.WRONG_PENALTY, g.s().points)
        assertEquals(1, g.s().wrong)
        assertEquals(0, g.s().streak)
        assertEquals(1, g.s().step) // no avanza
        assertEquals(StepResult.WRONG, g.s().lastResult)
    }

    @Test
    fun decoyTap_penalizesMore_andIsCountedApart() {
        val g = game(level = 3)
        g.start()
        g.tapCorrect()
        val trap = g.s().tray.first { it.decoy }
        g.tap(trap)
        assertEquals(10 - BuildGame.DECOY_PENALTY, g.s().points)
        assertEquals(1, g.s().decoys)
        assertEquals(0, g.s().wrong)
        assertEquals(StepResult.DECOY, g.s().lastResult)
    }

    @Test
    fun points_neverGoBelowZero() {
        val g = game(level = 3)
        g.start()
        g.tap(g.s().tray.first { it.decoy })
        assertEquals(0, g.s().points)
    }

    @Test
    fun pieceNotInTheTray_isIgnored() {
        val g = game(level = 1)
        g.start()
        g.tap(Piece.MOJADA) // en el nivel 1 no hay trampas
        assertEquals(0, g.s().decoys)
        assertEquals(0, g.s().wrong)
    }

    @Test
    fun timeout_missesTheStep_withoutPoints() {
        val g = game()
        g.start()
        g.update(RoundSpec.forTier(2).stepMs)
        assertEquals(1, g.s().missed)
        assertEquals(1, g.s().step)
        assertEquals(0, g.s().points)
        assertEquals(StepResult.MISSED, g.s().lastResult)
    }

    @Test
    fun completingARound_givesBonus_thenStartsAHarderOne() {
        val g = game(level = 1) // ronda 1: 3 pasos
        g.start()
        repeat(3) { g.tapCorrect() }
        assertEquals(BuildPhase.ROUND_DONE, g.s().phase)
        assertEquals(1, g.s().roundsCompleted)
        assertEquals(10 + 12 + 14 + BuildGame.ROUND_BONUS + BuildGame.ROUND_BONUS_PER_ROUND * 1, g.s().points)

        g.update(BuildGame.ROUND_PAUSE_MS)
        assertEquals(BuildPhase.PLAYING, g.s().phase)
        assertEquals(2, g.s().round)
        assertEquals(2, g.s().tier)
        assertEquals(RoundSpec.forTier(2).length, g.s().sequence.size)
        assertEquals(0, g.s().step)
    }

    @Test
    fun noTapsDuringTheRoundPause() {
        val g = game(level = 1)
        g.start()
        repeat(3) { g.tapCorrect() }
        val points = g.s().points
        g.tap(Piece.LENA)
        assertEquals(points, g.s().points)
    }

    // ---------- Contenido de cada ronda ----------

    @Test
    fun trayAlwaysHasTheExpectedPiece_andNoImmediateRepeats() {
        val g = game(level = 3)
        g.start()
        repeat(60) {
            val s = g.s()
            if (s.phase == BuildPhase.PLAYING) {
                assertTrue(s.expected in s.tray)
                assertEquals(s.tray.size, s.tray.toSet().size)
                s.sequence.zipWithNext().forEach { (a, b) -> assertTrue(a != b) }
                g.tapCorrect()
            } else {
                g.update(BuildGame.ROUND_PAUSE_MS)
            }
        }
    }

    @Test
    fun trayHasNoMoreThanEightPieces() {
        val g = game(level = 3, durationMs = 600_000L)
        g.start()
        repeat(200) {
            if (g.s().phase == BuildPhase.PLAYING) g.tapCorrect() else g.update(BuildGame.ROUND_PAUSE_MS)
            assertTrue(g.s().tray.size <= 8)
        }
    }

    @Test
    fun tapIndex_picksFromTheTray() {
        val g = game()
        g.start()
        val index = g.s().tray.indexOf(g.s().expected)
        g.tapIndex(index)
        assertEquals(1, g.s().step)
        g.tapIndex(99) // fuera de la bandeja: no hace nada
        assertEquals(1, g.s().step)
    }

    // ---------- Fin y puntaje ----------

    @Test
    fun gameEnds_whenTimeRunsOut_andIgnoresTaps() {
        val g = game(durationMs = 10_000L)
        g.start()
        g.update(10_000L)
        assertEquals(BuildPhase.FINISHED, g.s().phase)
        assertEquals(0L, g.s().remainingMs)
        val points = g.s().points
        g.tap(Piece.LENA)
        assertEquals(points, g.s().points)
    }

    @Test
    fun hugeDelta_doesNotBreakTheGame() {
        val g = game()
        g.start()
        g.update(1_000_000L)
        assertEquals(BuildPhase.FINISHED, g.s().phase)
        assertEquals(0L, g.s().remainingMs)
    }

    @Test
    fun score_scalesFromZeroToOneHundred() {
        fun score(points: Int) = BuildGame.statsOf(BuildState(remainingMs = 0, points = points)).score
        assertEquals(0, score(0))
        assertEquals(50, score(BuildGame.TARGET_POINTS / 2))
        assertEquals(100, score(BuildGame.TARGET_POINTS))
        assertEquals(100, score(BuildGame.TARGET_POINTS * 3))
    }

    @Test
    fun aFastPerfectPlayer_reachesTheTop() {
        val g = game(level = 2)
        g.start()
        // 1,2 s por pieza, sin errores, durante toda la partida.
        while (g.s().phase != BuildPhase.FINISHED) {
            when (g.s().phase) {
                BuildPhase.PLAYING -> {
                    g.tapCorrect()
                    g.update(1_200L)
                }
                else -> g.update(BuildGame.ROUND_PAUSE_MS)
            }
        }
        assertEquals(100, g.stats().score)
        assertTrue(g.stats().rounds >= 4)
        assertEquals(0, g.stats().wrong)
    }

    @Test
    fun aPlayerWhoNeverTaps_scoresZero_andMissesEverything() {
        val g = game(level = 2)
        g.start()
        g.update(45_000L)
        assertEquals(0, g.stats().score)
        assertTrue(g.stats().missed > 0)
    }
}
