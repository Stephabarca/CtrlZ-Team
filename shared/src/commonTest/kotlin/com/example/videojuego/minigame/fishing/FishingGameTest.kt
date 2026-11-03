package com.example.videojuego.minigame.fishing

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FishingGameTest {

    private fun game(durationMs: Long = 30_000L) = FishingGame(durationMs, Random(42))

    private fun FishingGame.phase() = state.value.phase

    /** Avanza en pasos de [step] ms hasta que se cumpla [condition] (con tope para no colgar la prueba). */
    private fun FishingGame.advanceUntil(step: Long = 10, limit: Int = 2_000, condition: (FishingState) -> Boolean) {
        var n = 0
        while (!condition(state.value) && n++ < limit) update(step)
        assertTrue(condition(state.value), "No se llegó al estado esperado")
    }

    private fun FishingGame.untilBiting() = advanceUntil { it.phase == FishingPhase.BITING }

    // ---------- Estados ----------

    @Test
    fun startsReady_andDoesNotRunUntilStarted() {
        val g = game()
        assertEquals(FishingPhase.READY, g.phase())
        g.update(5_000)
        assertEquals(30_000L, g.state.value.remainingMs)
        g.tap()
        assertTrue(g.state.value.results.isEmpty())
    }

    @Test
    fun start_beginsWaiting() {
        val g = game()
        g.start()
        assertEquals(FishingPhase.WAITING, g.phase())
    }

    @Test
    fun afterAWait_thePicadaStarts() {
        val g = game()
        g.start()
        g.update(FishingGame.WAIT_MIN_MS - 1)
        assertEquals(FishingPhase.WAITING, g.phase())
        // Lo que falta como máximo para que termine la espera, sin pasarse de la ventana de la picada.
        g.update(FishingGame.WAIT_MAX_MS - FishingGame.WAIT_MIN_MS + 1)
        assertEquals(FishingPhase.BITING, g.phase())
    }

    // ---------- Jalar ----------

    @Test
    fun pullingBeforeTheBite_scaresTheFish() {
        val g = game()
        g.start()
        g.tap()
        assertEquals(CatchResult.EARLY, g.state.value.lastResult)
        assertEquals(0, g.state.value.points) // los puntos no bajan de cero
        g.update(FishingGame.EARLY_PAUSE_MS)
        assertEquals(FishingPhase.WAITING, g.phase())
    }

    @Test
    fun pullingAtTheCenter_isPerfect() {
        val g = game()
        g.start()
        g.untilBiting()
        g.advanceUntil { kotlin.math.abs(it.cursor - 0.5f) <= 0.02f }
        g.tap()
        assertEquals(CatchResult.PERFECT, g.state.value.lastResult)
        assertEquals(CatchResult.PERFECT.points, g.state.value.points)
        assertEquals(FishingPhase.RESULT, g.phase())
    }

    @Test
    fun pullingNearTheCenter_isGood() {
        val g = game()
        g.start()
        g.untilBiting()
        g.advanceUntil { it.cursor in 0.36f..0.4f }
        g.tap()
        assertEquals(CatchResult.GOOD, g.state.value.lastResult)
    }

    @Test
    fun pullingAtTheEdge_isWeak() {
        val g = game()
        g.start()
        g.untilBiting()
        g.update(30)
        g.tap()
        assertEquals(CatchResult.WEAK, g.state.value.lastResult)
    }

    @Test
    fun ignoringTheBite_losesTheFish() {
        val g = game()
        g.start()
        g.untilBiting()
        g.update(FishingGame.BITE_WINDOW_MS)
        assertEquals(CatchResult.LOST, g.state.value.lastResult)
        assertEquals(FishingPhase.RESULT, g.phase())
    }

    @Test
    fun afterTheResult_itGoesBackToWaiting() {
        val g = game()
        g.start()
        g.untilBiting()
        g.tap()
        g.update(FishingGame.RESULT_PAUSE_MS)
        assertEquals(FishingPhase.WAITING, g.phase())
    }

    @Test
    fun tapDuringResultPause_isIgnored() {
        val g = game()
        g.start()
        g.untilBiting()
        g.tap()
        g.tap()
        g.tap()
        assertEquals(1, g.state.value.results.size)
    }

    // ---------- Tiempo ----------

    @Test
    fun whenTimeEnds_itFinishesAndStopsAcceptingTaps() {
        val g = game(5_000)
        g.start()
        g.update(5_000)
        assertEquals(FishingPhase.FINISHED, g.phase())
        assertEquals(0L, g.state.value.remainingMs)
        val before = g.state.value.results.size
        g.tap()
        assertEquals(before, g.state.value.results.size)
    }

    @Test
    fun aHugeFrame_isHandledWithoutBreaking() {
        val g = game(10_000)
        g.start()
        g.update(1_000_000)
        assertEquals(FishingPhase.FINISHED, g.phase())
        assertEquals(0L, g.state.value.remainingMs)
    }

    @Test
    fun negativeDelta_isIgnored() {
        val g = game()
        g.start()
        g.update(-500)
        assertEquals(30_000L, g.state.value.remainingMs)
    }

    // ---------- Reglas puras ----------

    @Test
    fun cursor_goesThereAndBack() {
        assertEquals(0f, FishingGame.cursorAt(0), 0.001f)
        assertEquals(0.5f, FishingGame.cursorAt(350), 0.001f)
        assertEquals(1f, FishingGame.cursorAt(700), 0.001f)
        assertEquals(0.5f, FishingGame.cursorAt(1_050), 0.001f)
        assertEquals(0f, FishingGame.cursorAt(1_400), 0.001f)
    }

    @Test
    fun judge_usesTheZones() {
        assertEquals(CatchResult.PERFECT, FishingGame.judge(0.5f))
        assertEquals(CatchResult.PERFECT, FishingGame.judge(0.5f + FishingGame.PERFECT_HALF_WIDTH))
        assertEquals(CatchResult.GOOD, FishingGame.judge(0.5f + FishingGame.GOOD_HALF_WIDTH))
        assertEquals(CatchResult.WEAK, FishingGame.judge(0.9f))
        assertEquals(CatchResult.WEAK, FishingGame.judge(0f))
    }

    @Test
    fun score_reaches100WithSixPerfectCatches() {
        val stats = FishingGame.statsOf(List(6) { CatchResult.PERFECT })
        assertEquals(100, stats.score)
        assertEquals(6, stats.perfect)
        assertEquals(6, stats.fishCaught)
    }

    @Test
    fun score_isCappedAt100() {
        assertEquals(100, FishingGame.statsOf(List(20) { CatchResult.PERFECT }).score)
    }

    @Test
    fun score_halfwayWithThreePerfectCatches() {
        assertEquals(50, FishingGame.statsOf(List(3) { CatchResult.PERFECT }).score)
    }

    @Test
    fun score_isZeroWithNothingOrOnlyMistakes() {
        assertEquals(0, FishingGame.statsOf(emptyList()).score)
        assertEquals(0, FishingGame.statsOf(listOf(CatchResult.EARLY, CatchResult.LOST, CatchResult.EARLY)).score)
    }

    @Test
    fun score_pointsNeverGoBelowZeroBeforeAGoodCatch() {
        // EARLY al inicio no resta; después un PERFECT suma 20 de 120 -> 16.
        assertEquals(16, FishingGame.statsOf(listOf(CatchResult.EARLY, CatchResult.PERFECT)).score)
    }

    @Test
    fun stats_countEachKind() {
        val stats = FishingGame.statsOf(
            listOf(CatchResult.PERFECT, CatchResult.GOOD, CatchResult.WEAK, CatchResult.LOST, CatchResult.EARLY, CatchResult.EARLY),
        )
        assertEquals(1, stats.perfect)
        assertEquals(1, stats.good)
        assertEquals(1, stats.weak)
        assertEquals(1, stats.lost)
        assertEquals(2, stats.early)
        assertEquals(6, stats.attempts)
    }

    @Test
    fun aFullPlaythrough_scoresMatchTheGameState() {
        val g = game()
        g.start()
        repeat(3) {
            g.untilBiting()
            g.advanceUntil { kotlin.math.abs(it.cursor - 0.5f) <= 0.02f }
            g.tap()
        }
        assertEquals(60, g.state.value.points)
        assertEquals(50, g.stats().score)
    }
}
