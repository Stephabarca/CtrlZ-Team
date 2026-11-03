package com.example.videojuego.world

import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IslaWorldTest {

    private val map = IslaWorld.map
    private val npcs = IslaWorld.npcs

    @Test
    fun map_hasTheExpectedSize() {
        assertEquals(24, map.width)
        assertEquals(18, map.height)
    }

    @Test
    fun island_isSurroundedByWater() {
        for (x in 0 until map.width) {
            assertFalse(map.isWalkable(x, 0))
            assertFalse(map.isWalkable(x, map.height - 1))
        }
        for (y in 0 until map.height) {
            assertFalse(map.isWalkable(0, y))
            assertFalse(map.isWalkable(map.width - 1, y))
        }
    }

    @Test
    fun playerStart_isOnWalkableGround() {
        assertTrue(map.isWalkable(floor(IslaWorld.PLAYER_START_X).toInt(), floor(IslaWorld.PLAYER_START_Y).toInt()))
    }

    @Test
    fun npcs_areTheThreeSurvivorsOnDistinctWalkableTiles() {
        assertEquals(setOf("valentina", "lucas", "lila"), npcs.map { it.id }.toSet())
        assertEquals(npcs.size, npcs.map { it.tileX to it.tileY }.toSet().size)
        npcs.forEach { assertTrue(map.isWalkable(it.tileX, it.tileY), "${it.id} está sobre un tile bloqueado") }
    }

    @Test
    fun eachNpcOwnsOneNight() {
        assertEquals(listOf("night1", "night2", "night3"), npcs.map { it.nightId }.sorted())
        assertEquals("valentina", IslaWorld.npcForNight("night1")?.id)
        assertEquals("lucas", IslaWorld.npcForNight("night2")?.id)
        assertEquals("lila", IslaWorld.npcForNight("night3")?.id)
        assertEquals(null, IslaWorld.npcForNight("night9"))
    }

    @Test
    fun idleLines_areNotBlank() {
        npcs.forEach { assertTrue(it.idleLine.isNotBlank()) }
    }

    @Test
    fun everyNpc_canBeReachedFromTheStart() {
        val npcTiles = npcs.map { it.tileX to it.tileY }.toSet()
        val start = floor(IslaWorld.PLAYER_START_X).toInt() to floor(IslaWorld.PLAYER_START_Y).toInt()
        val seen = mutableSetOf(start)
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()
            for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) {
                val next = x + dx to y + dy
                if (next !in seen && next !in npcTiles && map.isWalkable(next.first, next.second)) {
                    seen += next
                    queue.addLast(next)
                }
            }
        }
        npcs.forEach { npc ->
            val reachable = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
                .any { (dx, dy) -> (npc.tileX + dx to npc.tileY + dy) in seen }
            assertTrue(reachable, "${npc.id} no se puede alcanzar desde el inicio")
        }
    }

    @Test
    fun playerCanWalkOffTheStartWithoutGettingStuck() {
        val c = WorldController(map, npcs, IslaWorld.PLAYER_START_X, IslaWorld.PLAYER_START_Y)
        repeat(30) { c.update(0f, -1f, 16) }
        assertTrue(c.state.value.playerY < IslaWorld.PLAYER_START_Y)
    }

    // ---------- Validación del formato del mapa ----------

    @Test
    fun parsing_rejectsUnknownCharacters() {
        assertFailsWith<IllegalArgumentException> { WorldMap(listOf("~x~")) }
    }

    @Test
    fun parsing_rejectsRaggedRows() {
        assertFailsWith<IllegalArgumentException> { WorldMap(listOf("~~~", "~~")) }
    }

    @Test
    fun parsing_rejectsAnEmptyMap() {
        assertFailsWith<IllegalArgumentException> { WorldMap(emptyList()) }
    }

    @Test
    fun outOfBoundsTiles_areNotWalkable() {
        assertFalse(map.isWalkable(-1, 3))
        assertFalse(map.isWalkable(3, 999))
    }

    @Test
    fun everyNpc_hasShortChatLines() {
        for (npc in IslaWorld.npcs) {
            assertTrue(npc.chatLines.size >= 3, "${npc.id} necesita al menos 3 frases")
            assertTrue(npc.chatLines.all { it.isNotBlank() && it.length <= 90 }, "${npc.id}: frases cortas")
        }
    }

    @Test
    fun island_hasPalmsRocksAndOneFire() {
        val tiles = (0 until IslaWorld.map.height).flatMap { y -> (0 until IslaWorld.map.width).map { x -> IslaWorld.map.tileAt(x, y) } }
        assertTrue(tiles.count { it == TileType.TREE } >= 3)
        assertTrue(tiles.count { it == TileType.ROCK } >= 3)
        assertEquals(1, tiles.count { it == TileType.FIRE })
    }
}
