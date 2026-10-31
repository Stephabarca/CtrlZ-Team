package com.example.videojuego.timer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Pruebas con reloj virtual: el tiempo "pasa" solo cuando la prueba llama a advance(). */
class CountdownTimerTest {

    @Test
    fun startsFullAndRunning() {
        val s = CountdownTimer(20_000).state.value
        assertEquals(20_000, s.remainingMs)
        assertEquals(0, s.elapsedMs)
        assertTrue(s.isRunning)
        assertFalse(s.expired)
    }

    @Test
    fun advance_reducesRemainingTime() {
        val timer = CountdownTimer(20_000)
        timer.advance(3_000)
        timer.advance(2_000)
        val s = timer.state.value
        assertEquals(15_000, s.remainingMs)
        assertEquals(5_000, s.elapsedMs)
    }

    @Test
    fun reachingZero_emitsTimeExpiredAndStops() {
        val timer = CountdownTimer(5_000)
        timer.advance(4_999)
        assertFalse(timer.state.value.expired)

        timer.advance(1)

        val s = timer.state.value
        assertEquals(0, s.remainingMs)
        assertTrue(s.expired)
        assertFalse(s.isRunning)
    }

    @Test
    fun advancingMoreThanRemaining_clampsToZero() {
        val timer = CountdownTimer(5_000)
        timer.advance(60_000)
        assertEquals(0, timer.state.value.remainingMs)
        assertTrue(timer.state.value.expired)
    }

    @Test
    fun afterExpiring_furtherTimeChangesNothing() {
        val timer = CountdownTimer(1_000)
        timer.advance(1_000)
        val expired = timer.state.value
        timer.advance(500)
        timer.resume()
        assertEquals(expired, timer.state.value)
    }

    @Test
    fun pause_stopsCountingAndResumeContinues() {
        val timer = CountdownTimer(10_000)
        timer.advance(2_000)
        timer.pause()
        timer.advance(5_000) // en pausa: no cuenta
        assertEquals(8_000, timer.state.value.remainingMs)
        assertFalse(timer.state.value.isRunning)

        timer.resume()
        timer.advance(1_000)
        assertEquals(7_000, timer.state.value.remainingMs)
    }

    @Test
    fun restoredTimer_continuesFromRemainingTime() {
        // Simula rotar la pantalla: se recrea con el tiempo que le quedaba.
        val timer = CountdownTimer(totalMs = 20_000, initialRemainingMs = 7_500)
        assertEquals(7_500, timer.state.value.remainingMs)
        assertEquals(12_500, timer.state.value.elapsedMs)

        timer.advance(7_500)
        assertTrue(timer.state.value.expired)
    }

    @Test
    fun restoredWithZeroRemaining_isAlreadyExpired() {
        val s = CountdownTimer(20_000, initialRemainingMs = 0).state.value
        assertTrue(s.expired)
        assertFalse(s.isRunning)
    }

    @Test
    fun restoredOutOfRange_isClamped() {
        assertEquals(20_000, CountdownTimer(20_000, initialRemainingMs = 99_000).state.value.remainingMs)
        assertEquals(0, CountdownTimer(20_000, initialRemainingMs = -5).state.value.remainingMs)
    }

    @Test
    fun nonPositiveDelta_isIgnored() {
        val timer = CountdownTimer(10_000)
        timer.advance(0)
        timer.advance(-500)
        assertEquals(10_000, timer.state.value.remainingMs)
    }

    @Test
    fun secondsLeft_roundsUp() {
        assertEquals(20, TimerState(20_000, 20_000, true, false).secondsLeft)
        assertEquals(20, TimerState(20_000, 19_001, true, false).secondsLeft)
        assertEquals(19, TimerState(20_000, 19_000, true, false).secondsLeft)
        assertEquals(1, TimerState(20_000, 1, true, false).secondsLeft)
        assertEquals(0, TimerState(20_000, 0, false, true).secondsLeft)
    }

    @Test
    fun fractionLeft_goesFromOneToZero() {
        val timer = CountdownTimer(10_000)
        assertEquals(1f, timer.state.value.fractionLeft, 0.001f)
        timer.advance(2_500)
        assertEquals(0.75f, timer.state.value.fractionLeft, 0.001f)
        timer.advance(7_500)
        assertEquals(0f, timer.state.value.fractionLeft, 0.001f)
    }

    @Test
    fun reset_restoresFullTimeAndClearsExpired() {
        val timer = CountdownTimer(5_000)
        timer.advance(5_000)
        timer.reset()
        val s = timer.state.value
        assertEquals(5_000, s.remainingMs)
        assertTrue(s.isRunning)
        assertFalse(s.expired)
    }

    @Test
    fun invalidDuration_isRejected() {
        assertFailsWith<IllegalArgumentException> { CountdownTimer(0) }
        assertFailsWith<IllegalArgumentException> { CountdownTimer(-1_000) }
    }

    // ---------- Segundo plano ----------

    @Test
    fun frameDelta_normalFrame_countsFully() {
        assertEquals(16, frameDelta(previousMs = 1_000, currentMs = 1_016))
    }

    @Test
    fun frameDelta_returningFromBackground_isCapped() {
        // La app estuvo 30 s en segundo plano: ese tiempo no debe descontarse.
        assertEquals(DEFAULT_MAX_FRAME_GAP_MS, frameDelta(previousMs = 1_000, currentMs = 31_000))
    }

    @Test
    fun frameDelta_neverNegative() {
        assertEquals(0, frameDelta(previousMs = 2_000, currentMs = 1_000))
    }

    @Test
    fun backgroundGap_doesNotCostTheLongWait() {
        val timer = CountdownTimer(20_000)
        timer.advance(frameDelta(0, 16))
        timer.advance(frameDelta(16, 45_016)) // 45 s en segundo plano
        timer.advance(frameDelta(45_016, 45_032))
        // Cuentan 16 + 250 + 16 ms, no 45 segundos.
        assertEquals(20_000 - 282, timer.state.value.remainingMs)
    }
}
