package com.example.videojuego.credits

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreditsRollTest {
    private val speed = 100f // px por segundo
    private val viewport = 800f
    private val content = 1200f

    @Test
    fun startsJustBelowTheScreen() {
        assertEquals(viewport, CreditsRoll.topAt(0, speed, viewport, content))
    }

    @Test
    fun movesUpAtConstantSpeed() {
        assertEquals(viewport - 100f, CreditsRoll.topAt(1_000, speed, viewport, content))
        assertEquals(viewport - 250f, CreditsRoll.topAt(2_500, speed, viewport, content))
    }

    @Test
    fun stopsWithTheEndInTheMiddle() {
        val stop = viewport / 2f - content
        assertEquals(stop, CreditsRoll.topAt(600_000, speed, viewport, content))
        // 400 px hasta la mitad + 1200 de créditos = 16 s a 100 px/s.
        assertEquals(16_000L, CreditsRoll.scrollMs(speed, viewport, content))
    }

    @Test
    fun finishesAfterThePauseAtTheEnd() {
        val end = CreditsRoll.scrollMs(speed, viewport, content)
        assertFalse(CreditsRoll.finished(end, speed, viewport, content))
        assertFalse(CreditsRoll.finished(end + CreditsRoll.HOLD_MS - 1, speed, viewport, content))
        assertTrue(CreditsRoll.finished(end + CreditsRoll.HOLD_MS, speed, viewport, content))
    }

    @Test
    fun neverFinishesBeforeTheCreditsAreMeasured() {
        assertFalse(CreditsRoll.finished(10_000_000, speed, viewport, 0f))
    }
}
