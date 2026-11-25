package com.example.videojuego.finale

import com.example.videojuego.ui.world.POP_LIFE_MS
import com.example.videojuego.ui.world.popAlpha
import com.example.videojuego.ui.world.popProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PopTest {
    @Test
    fun progressGoesFromZeroToOne() {
        assertEquals(0f, popProgress(0))
        assertEquals(1f, popProgress(POP_LIFE_MS))
        assertEquals(1f, popProgress(POP_LIFE_MS * 3))
        assertEquals(0f, popProgress(-50))
    }

    @Test
    fun alphaStaysFullThenFades() {
        assertEquals(1f, popAlpha(0f))
        assertEquals(1f, popAlpha(0.6f))
        assertTrue(popAlpha(0.8f) in 0.4f..0.6f)
        assertEquals(0f, popAlpha(1f))
    }
}
