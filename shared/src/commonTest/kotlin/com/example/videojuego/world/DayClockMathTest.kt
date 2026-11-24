package com.example.videojuego.world

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DayClockMathTest {
    private val day = 180_000L

    @Test
    fun progress_startsAtZero_andEndsAtOne() {
        assertEquals(0f, DayClockMath.progress(false, day, day))
        assertEquals(0.5f, DayClockMath.progress(false, day / 2, day))
        assertEquals(1f, DayClockMath.progress(false, 0L, day))
    }

    @Test
    fun progress_atNightIsAlwaysOne() {
        assertEquals(1f, DayClockMath.progress(true, day, day))
        assertEquals(1f, DayClockMath.progress(true, 0L, day))
    }

    @Test
    fun progress_staysBetweenZeroAndOne() {
        assertEquals(0f, DayClockMath.progress(false, day * 2, day))
        assertEquals(1f, DayClockMath.progress(false, -5L, day))
        assertEquals(1f, DayClockMath.progress(false, 1_000L, 0L))
    }

    @Test
    fun progress_growsAsTimeRuns() {
        var last = -1f
        var remaining = day
        while (remaining >= 0) {
            val p = DayClockMath.progress(false, remaining, day)
            assertTrue(p >= last)
            last = p
            remaining -= 10_000L
        }
    }

    @Test
    fun zones_followTheDay() {
        assertEquals(DayZone.MORNING, DayClockMath.zone(0f))
        assertEquals(DayZone.MORNING, DayClockMath.zone(0.39f))
        assertEquals(DayZone.AFTERNOON, DayClockMath.zone(DayClockMath.MORNING_END))
        assertEquals(DayZone.AFTERNOON, DayClockMath.zone(0.82f))
        assertEquals(DayZone.NIGHT, DayClockMath.zone(DayClockMath.AFTERNOON_END))
        assertEquals(DayZone.NIGHT, DayClockMath.zone(1f))
    }

    @Test
    fun nightZoneStartsWhenTheSkyStartsToDarken() {
        // El cielo se oscurece en los últimos DUSK_MS; el reloj debe estar ya en "noche" en ese momento.
        val start = day - DUSK_MS
        val zone = DayClockMath.zone(DayClockMath.progress(false, DUSK_MS, day))
        assertTrue(start > 0)
        assertEquals(DayZone.NIGHT, zone)
    }
}
