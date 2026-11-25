package com.example.videojuego.world

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeatherTest {
    @Test
    fun noRainAtStartOfCycle() {
        assertEquals(0f, Weather.rain(0L))
    }

    @Test
    fun rainReachesFullAndFades() {
        val full = Weather.RAIN_START_MS + Weather.RAIN_FADE_MS + 1000L
        assertEquals(1f, Weather.rain(full))
        val end = Weather.RAIN_START_MS + Weather.RAIN_FADE_MS * 2 + Weather.RAIN_FULL_MS
        assertEquals(0f, Weather.rain(end + 1))
    }

    @Test
    fun isDeterministicAndRepeatsEveryCycle() {
        val t = Weather.RAIN_START_MS + 2000L
        assertEquals(Weather.rain(t), Weather.rain(t + Weather.CYCLE_MS))
        assertEquals(Weather.rain(t), Weather.rain(t))
    }

    @Test
    fun windStaysInRange() {
        for (ms in 0L..300_000L step 1_000L) {
            val w = Weather.wind(ms)
            assertTrue(w in 0f..1f)
        }
    }
}
