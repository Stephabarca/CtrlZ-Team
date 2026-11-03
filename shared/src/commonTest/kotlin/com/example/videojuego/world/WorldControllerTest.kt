package com.example.videojuego.world

import com.example.videojuego.narrative.Speaker
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorldControllerTest {

    /** Mapa de arena rodeado de agua. */
    private fun openMap(width: Int, height: Int) = WorldMap(
        List(height) { y ->
            if (y == 0 || y == height - 1) "~".repeat(width) else "~" + ".".repeat(width - 2) + "~"
        },
    )

    private fun npc(id: String, x: Int, y: Int) = NpcData(id, id, Speaker.LUCAS, x, y, "night1", "...")

    private fun controller(
        map: WorldMap = openMap(12, 12),
        npcs: List<NpcData> = emptyList(),
        x: Float = 5.5f,
        y: Float = 5.5f,
    ) = WorldController(map, npcs, x, y)

    private fun WorldController.walk(dx: Float, dy: Float, frames: Int, frameMs: Long = 16) =
        repeat(frames) { update(dx, dy, frameMs) }

    // ---------- Movimiento ----------

    @Test
    fun movingRight_advancesBySpeedTimesTime() {
        val c = controller()
        c.update(1f, 0f, 1_000)
        assertEquals(5.5f + WorldController.SPEED_TILES_PER_SEC, c.state.value.playerX, 0.001f)
        assertEquals(5.5f, c.state.value.playerY, 0.001f)
        assertTrue(c.state.value.moving)
    }

    @Test
    fun diagonalMovement_isNotFasterThanStraight() {
        val c = controller()
        c.update(1f, 1f, 100)
        val s = c.state.value
        val distance = hypot(s.playerX - 5.5f, s.playerY - 5.5f)
        assertEquals(WorldController.SPEED_TILES_PER_SEC * 0.1f, distance, 0.001f)
    }

    @Test
    fun partialInput_walksSlower() {
        val c = controller()
        c.update(0.5f, 0f, 1_000)
        assertEquals(5.5f + WorldController.SPEED_TILES_PER_SEC * 0.5f, c.state.value.playerX, 0.001f)
    }

    @Test
    fun inputInsideDeadzone_doesNotMove() {
        val c = controller()
        c.update(0.05f, 0.05f, 1_000)
        assertEquals(5.5f, c.state.value.playerX, 0.0001f)
        assertEquals(5.5f, c.state.value.playerY, 0.0001f)
        assertEquals(false, c.state.value.moving)
    }

    @Test
    fun facing_followsTheDominantDirection() {
        val c = controller()
        c.update(-1f, 0f, 16); assertEquals(Direction.LEFT, c.state.value.facing)
        c.update(1f, 0f, 16); assertEquals(Direction.RIGHT, c.state.value.facing)
        c.update(0f, -1f, 16); assertEquals(Direction.UP, c.state.value.facing)
        c.update(0f, 1f, 16); assertEquals(Direction.DOWN, c.state.value.facing)
    }

    @Test
    fun time_accumulatesEvenWhenStanding() {
        val c = controller()
        c.update(0f, 0f, 100)
        c.update(0f, 0f, 100)
        assertEquals(200L, c.state.value.timeMs)
    }

    @Test
    fun negativeDelta_isIgnored() {
        val c = controller()
        c.update(1f, 0f, -500)
        assertEquals(5.5f, c.state.value.playerX, 0.0001f)
        assertEquals(0L, c.state.value.timeMs)
    }

    // ---------- Colisiones ----------

    @Test
    fun water_stopsThePlayerAtTheShore() {
        val c = controller(map = openMap(5, 5), x = 2.5f, y = 2.5f)
        c.walk(-1f, 0f, frames = 100)
        // La arena empieza en x = 1; el jugador no puede acercar su borde más que eso.
        assertTrue(c.state.value.playerX in 1.299f..1.36f, "x = ${c.state.value.playerX}")
    }

    @Test
    fun walkingDiagonallyIntoAWall_slidesAlongIt() {
        val c = controller(map = openMap(7, 7), x = 1.4f, y = 4.5f)
        c.walk(-1f, -1f, frames = 100)
        val s = c.state.value
        assertTrue(s.playerX >= 1.299f, "no atraviesa la pared izquierda (x = ${s.playerX})")
        assertTrue(s.playerY < 1.4f, "sigue subiendo por la pared (y = ${s.playerY})")
    }

    @Test
    fun staysInsideTheMap() {
        val c = controller(map = openMap(6, 6), x = 3.0f, y = 3.0f)
        c.walk(1f, 1f, frames = 200)
        val s = c.state.value
        assertTrue(s.playerX < 5f && s.playerY < 5f)
    }

    @Test
    fun npc_isSolid() {
        val c = controller(map = openMap(9, 5), npcs = listOf(npc("lucas", 5, 2)), x = 2.5f, y = 2.5f)
        c.walk(1f, 0f, frames = 100)
        // El centro del personaje está en x = 5.5; hay que quedarse al menos a 0.65 de distancia.
        assertTrue(c.state.value.playerX in 4.75f..4.86f, "x = ${c.state.value.playerX}")
    }

    @Test
    fun treesAndRocks_blockMovement() {
        val map = WorldMap(
            listOf(
                "~~~~~~~",
                "~.T...~",
                "~.....~",
                "~~~~~~~",
            ),
        )
        val c = WorldController(map, emptyList(), 1.5f, 1.5f)
        c.walk(1f, 0f, frames = 50)
        assertTrue(c.state.value.playerX < 1.75f, "x = ${c.state.value.playerX}")
    }

    // ---------- Interacción ----------

    @Test
    fun nearbyNpc_isDetectedOnlyWithinReach() {
        val npcs = listOf(npc("lucas", 4, 2))
        assertEquals("lucas", controller(map = openMap(9, 5), npcs = npcs, x = 3.2f, y = 2.5f).state.value.nearbyNpcId)
        assertNull(controller(map = openMap(9, 5), npcs = npcs, x = 1.5f, y = 2.5f).state.value.nearbyNpcId)
    }

    @Test
    fun walkingTowardsAnNpc_eventuallyEnablesInteraction() {
        val c = controller(map = openMap(9, 5), npcs = listOf(npc("lucas", 6, 2)), x = 2.5f, y = 2.5f)
        assertNull(c.interact())
        c.walk(1f, 0f, frames = 100)
        assertEquals("lucas", c.interact())
    }

    @Test
    fun nearestNpc_winsWhenTwoAreInReach() {
        val npcs = listOf(npc("lejos", 3, 2), npc("cerca", 5, 2))
        val c = controller(map = openMap(10, 5), npcs = npcs, x = 4.5f, y = 2.5f)
        // Está entre los dos, pero más cerca de "cerca"? Se mueve un poco hacia ese lado.
        c.update(1f, 0f, 30)
        assertEquals("cerca", c.interact())
    }

    @Test
    fun movingAway_clearsTheNearbyNpc() {
        val c = controller(map = openMap(12, 5), npcs = listOf(npc("lucas", 4, 2)), x = 3.2f, y = 2.5f)
        assertEquals("lucas", c.interact())
        c.walk(-1f, 0f, frames = 30)
        assertNull(c.interact())
    }

    // ---------- Colocar al jugador ----------

    @Test
    fun placeAt_movesToAValidSpot() {
        val c = controller()
        c.placeAt(3.5f, 3.5f)
        assertEquals(3.5f, c.state.value.playerX, 0.0001f)
    }

    @Test
    fun placeAt_ignoresBlockedSpots() {
        val c = controller()
        c.placeAt(0.5f, 0.5f) // agua
        assertEquals(5.5f, c.state.value.playerX, 0.0001f)
    }

    // ---------- Cámara ----------

    @Test
    fun camera_followsThePlayerAndStaysInsideTheMap() {
        assertEquals(0f, cameraOrigin(player = 2f, viewSize = 9f, mapSize = 24), 0.0001f)
        assertEquals(7.5f, cameraOrigin(player = 12f, viewSize = 9f, mapSize = 24), 0.0001f)
        assertEquals(15f, cameraOrigin(player = 23f, viewSize = 9f, mapSize = 24), 0.0001f)
    }

    @Test
    fun camera_centersAMapSmallerThanTheView() {
        assertEquals(-2f, cameraOrigin(player = 2f, viewSize = 9f, mapSize = 5), 0.0001f)
    }
}
