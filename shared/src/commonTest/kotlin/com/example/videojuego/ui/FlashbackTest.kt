package com.example.videojuego.ui

import com.example.videojuego.finale.FinaleRules
import com.example.videojuego.ui.flashback.FlashbackData
import com.example.videojuego.ui.flashback.SceneArt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FlashbackTest {
    @Test
    fun everyCharacterHasFourScenesEndingAtTheDock() {
        FinaleRules.ORDER.forEach { id ->
            val scenes = FlashbackData.scenesFor(id)
            assertEquals(4, scenes.size, id)
            assertTrue(scenes.all { it.text.isNotBlank() && it.place.isNotBlank() }, id)
            assertTrue(scenes.last().art.name.startsWith("DOCK"), id)
            assertEquals(1, scenes.count { it.art.name.startsWith("DOCK") }, id)
        }
        assertEquals(SceneArt.entries.size, FlashbackData.byCharacter.values.flatten().map { it.art }.toSet().size)
    }

    @Test
    fun unknownCharacterHasNoScenes() {
        assertTrue(FlashbackData.scenesFor("nadie").isEmpty())
    }

    @Test
    fun durationGrowsWithTextButHasMinimum() {
        val short = FlashbackData.scenesFor("lila")[0]
        assertTrue(FlashbackData.durationMs(short) >= 6000L)
        val all = FlashbackData.byCharacter.values.flatten()
        assertTrue(all.all { FlashbackData.durationMs(it) in 6000L..15_000L })
    }

    @Test
    fun playsOnlyOncePerCharacterAndNightNotAlreadyStarted() {
        assertTrue(FlashbackData.shouldPlay("lucas", 2, emptyList(), emptySet()))
        assertFalse(FlashbackData.shouldPlay("lucas", 2, emptyList(), setOf("lucas")))
        assertFalse(FlashbackData.shouldPlay("lucas", 2, listOf("n2q1"), emptySet()))
        // Las respuestas de otra noche no cuentan.
        assertTrue(FlashbackData.shouldPlay("lucas", 2, listOf("n1q1", "n1q2"), emptySet()))
        assertFalse(FlashbackData.shouldPlay("desconocido", 1, emptyList(), emptySet()))
    }
}
