package com.example.videojuego.finale

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FinaleTest {
    @Test
    fun winsHighestAffinity() {
        val r = FinaleRules.decide(mapOf("valentina" to 10, "lucas" to 22, "lila" to 15))
        assertEquals("lucas", r.partnerId)
        assertEquals("lila", r.runnerUpId)
        assertFalse(r.closeCall)
    }

    @Test
    fun closeCallWhenMarginSmall() {
        val r = FinaleRules.decide(mapOf("valentina" to 20, "lucas" to 18, "lila" to 5))
        assertEquals("valentina", r.partnerId)
        assertTrue(r.closeCall)
        assertEquals("lucas", r.runnerUpId)
    }

    @Test
    fun missingAffinityUsesStart() {
        val r = FinaleRules.decide(mapOf("lila" to 3), startAffinity = 5)
        assertEquals(5, r.scores["valentina"])
        assertEquals("valentina", r.partnerId) // 5 = 5 > 3, empate de valentina/lucas -> orden
    }

    @Test
    fun tieBrokenByMinigameThenOrder() {
        val aff = mapOf("valentina" to 10, "lucas" to 10, "lila" to 10)
        assertEquals("valentina", FinaleRules.decide(aff).partnerId)
        assertEquals("lila", FinaleRules.decide(aff, tieBreak = mapOf("lila" to 80, "lucas" to 40)).partnerId)
    }

    @Test
    fun rollAlwaysLandsOnWinner() {
        for (winner in 0..2) {
            val seq = FinaleRoll.sequence(winner, 3)
            assertEquals(FinaleRoll.STEPS, seq.size)
            assertEquals(winner, seq.last())
            // Siempre salta a otro personaje (nunca se queda en el mismo dos veces seguidas).
            seq.zipWithNext().forEach { (a, b) -> assertTrue(a != b) }
            assertEquals(winner, FinaleRoll.slotAt(FinaleRoll.spinMs, winner, 3))
            assertEquals(winner, FinaleRoll.slotAt(FinaleRoll.totalMs, winner, 3))
            assertEquals(seq.first(), FinaleRoll.slotAt(0L, winner, 3))
        }
        assertTrue(FinaleRoll.landed(FinaleRoll.spinMs))
        assertFalse(FinaleRoll.landed(FinaleRoll.spinMs - 1))
    }
}
