package com.example.videojuego.diary

import com.example.videojuego.data.TestFixtures
import com.example.videojuego.data.save.AnswerRecord
import com.example.videojuego.data.save.SaveGame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiaryTest {
    private val content = TestFixtures.content()

    @Test
    fun hidesLearningUntilTheNightIsPlayed() {
        val entries = DiaryBuilder.build(content, null)
        assertEquals(3, entries.size)
        assertFalse(entries[0].met)
        assertNull(entries[0].learning)
        assertEquals("Conocidos", entries[0].stageName)
    }

    @Test
    fun showsLearningAfterAnswering() {
        val save = SaveGame(
            affinity = mapOf("valentina" to 80),
            answers = listOf(AnswerRecord("n1q1", "n1q1a1", 1000, false, 10)),
        )
        val e = DiaryBuilder.build(content, save).first()
        assertTrue(e.met)
        assertEquals("x", e.learning)
        assertEquals("Cercanía", e.stageName)
        assertEquals(0.8f, e.affinityFraction)
    }
}
