package com.example.videojuego.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AffinityHeartsTest {

    private fun assertFills(expected: List<Float>, actual: List<Float>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (e, a) -> assertEquals(e, a, 0.001f) }
    }

    @Test
    fun emptyAffinity_hasAllHeartsEmpty() {
        assertFills(listOf(0f, 0f, 0f, 0f, 0f), heartFills(0f))
    }

    @Test
    fun fullAffinity_hasAllHeartsFull() {
        assertFills(listOf(1f, 1f, 1f, 1f, 1f), heartFills(1f))
    }

    @Test
    fun halfAffinity_fillsTwoAndAHalfHearts() {
        assertFills(listOf(1f, 1f, 0.5f, 0f, 0f), heartFills(0.5f))
    }

    @Test
    fun startingAffinity_fillsOneAndAHalfHearts() {
        assertFills(listOf(1f, 0.5f, 0f, 0f, 0f), heartFills(0.3f)) // 30 de 100
    }

    @Test
    fun outOfRangeFractions_areClamped() {
        assertFills(listOf(0f, 0f, 0f, 0f, 0f), heartFills(-2f))
        assertFills(listOf(1f, 1f, 1f, 1f, 1f), heartFills(7f))
    }

    @Test
    fun customHeartCount_isRespected() {
        assertFills(listOf(1f, 0.5f, 0f), heartFills(0.5f, hearts = 3))
    }

    @Test
    fun zeroHearts_isRejected() {
        assertFailsWith<IllegalArgumentException> { heartFills(0.5f, hearts = 0) }
    }
}
