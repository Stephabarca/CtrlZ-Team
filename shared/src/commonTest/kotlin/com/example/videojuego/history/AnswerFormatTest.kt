package com.example.videojuego.history

import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerFormatTest {

    @Test
    fun formatSecondsUsesOneDecimal() {
        assertEquals("0.0", formatSeconds(0))
        assertEquals("0.9", formatSeconds(950))
        assertEquals("3.2", formatSeconds(3_200))
        assertEquals("10.0", formatSeconds(10_000))
    }

    @Test
    fun formatEffectShowsSigns() {
        assertEquals("—", formatEffect(emptyMap()))
        assertEquals("mara +5", formatEffect(mapOf("mara" to 5)))
        assertEquals("leo -3", formatEffect(mapOf("leo" to -3)))
        assertEquals("mara +2, leo -2", formatEffect(mapOf("mara" to 2, "leo" to -2)))
    }
}
