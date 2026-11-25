package com.example.videojuego.minigame

import com.example.videojuego.minigame.hydrohopper.HydroHopperGame
import com.example.videojuego.minigame.hydrohopper.HydroPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HydroHopperGameTest {

    @Test
    fun testGameStartAndPlayerMovement() {
        val game = HydroHopperGame(durationMs = 20_000L)
        assertEquals(HydroPhase.READY, game.state.value.phase)

        game.start()
        assertEquals(HydroPhase.PLAYING, game.state.value.phase)

        val initialX = game.state.value.playerX
        game.movePlayer(-0.1f)
        assertTrue(game.state.value.playerX < initialX)

        game.setPlayerX(0.8f)
        assertEquals(0.8f, game.state.value.playerX)
    }

    @Test
    fun testDistanceProgressAndRescueShipArrival() {
        val game = HydroHopperGame(durationMs = 10_000L)
        game.start()

        // Avanzar el tiempo a la mitad (5000ms)
        game.update(5000L)
        val state1 = game.state.value
        assertEquals(0.5f, state1.distanceProgress, 0.05f)
        assertFalse(state1.isShipVisible)

        // Avanzar al 90% del tiempo (9000ms) -> El barco debe volverse visible
        game.update(4000L)
        val state2 = game.state.value
        assertTrue(state2.isShipVisible)

        // Avanzar al final -> Entra en fase SHIP_RESCUE
        game.update(1000L)
        val state3 = game.state.value
        assertEquals(1.0f, state3.distanceProgress, 0.01f)
        assertEquals(HydroPhase.SHIP_RESCUE, state3.phase)

        // Esperar 3 segundos de celebración en el barco -> Entra en FINISHED
        game.update(3500L)
        val state4 = game.state.value
        assertEquals(HydroPhase.FINISHED, state4.phase)

        val stats = game.stats()
        assertTrue(stats.rescued)
        assertEquals(1.0f, stats.finalDistance, 0.01f)
    }
}
