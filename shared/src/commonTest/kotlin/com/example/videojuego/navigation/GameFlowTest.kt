package com.example.videojuego.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameFlowTest {
    @Test
    fun firstScreenIsIntro() = assertEquals(Screen.Intro, GameFlow.first())

    @Test
    fun sequenceAdvancesInOrder() {
        assertEquals(Screen.Night1, GameFlow.next(Screen.Intro))
        assertEquals(Screen.Night2, GameFlow.next(Screen.Night1))
        assertEquals(Screen.Night3, GameFlow.next(Screen.Night2))
    }

    @Test
    fun lastScreenHasNoNext() = assertNull(GameFlow.next(Screen.Night3))

    @Test
    fun nonGameScreensHaveNoNext() {
        assertNull(GameFlow.next(Screen.Menu))
        assertNull(GameFlow.next(Screen.Settings))
    }

    @Test
    fun backConfirmationOnlyInGameScreens() {
        assertTrue(GameFlow.requiresBackConfirmation(Screen.Intro))
        assertTrue(GameFlow.requiresBackConfirmation(Screen.Night2))
        assertFalse(GameFlow.requiresBackConfirmation(Screen.Menu))
        assertFalse(GameFlow.requiresBackConfirmation(Screen.Settings))
    }
}
