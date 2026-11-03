package com.example.videojuego.world

import com.example.videojuego.data.GameJson
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.data.save.WorldSave
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CabinTest {

    /** Isla pequeña con la puerta de la cabaña en (4, 1). El jugador empieza justo debajo. */
    private val outdoor = WorldMap(
        listOf(
            "~~~~~~~~~~",
            "~..HP....~",
            "~........~",
            "~........~",
            "~........~",
            "~~~~~~~~~~",
        ),
    )

    private fun controller(indoor: Boolean = true, dayMs: Long = 10_000L) = WorldController(
        map = outdoor,
        npcs = emptyList(),
        startX = 4.5f,
        startY = 2.5f,
        dayLengthMs = dayMs,
        indoorMap = if (indoor) CabinWorld.map else null,
        indoorStartX = CabinWorld.START_X,
        indoorStartY = CabinWorld.START_Y,
    )

    private fun WorldController.inside(): WorldController {
        assertTrue(enterCabin())
        return this
    }

    private fun WorldController.withBag(coconuts: Int = 0, coal: Int = 0, chestCoconuts: Int = 0) = apply {
        restore(
            WorldSave(
                bag = mapOf("COCONUT" to coconuts, "COAL" to coal),
                chest = mapOf("COCONUT" to chestCoconuts),
            ),
        )
    }

    // ---------- Entrar y salir ----------

    @Test
    fun standingBelowTheDoor_detectsIt() {
        assertEquals(TileType.HUT_DOOR, controller().state.value.nearTile?.type)
    }

    @Test
    fun enteringTheCabin_movesThePlayerInside() {
        val c = controller().inside()
        val s = c.state.value
        assertEquals(Location.CABIN, s.location)
        assertEquals(CabinWorld.START_X, s.playerX, 0.001f)
        assertEquals(CabinWorld.START_Y, s.playerY, 0.001f)
        assertEquals(TileType.EXIT_DOOR, s.nearTile?.type)
    }

    @Test
    fun cannotEnterAwayFromTheDoor() {
        val c = controller()
        c.placeAt(7.5f, 4.5f)
        assertFalse(c.enterCabin())
        assertEquals(Location.OUTSIDE, c.state.value.location)
    }

    @Test
    fun cannotEnterWithoutAnInterior() {
        assertFalse(controller(indoor = false).enterCabin())
    }

    @Test
    fun leavingTheCabin_returnsToTheSameSpot() {
        val c = controller()
        c.placeAt(5.5f, 2.5f)
        c.placeAt(4.5f, 2.5f)
        c.inside()
        assertTrue(c.exitCabin())
        val s = c.state.value
        assertEquals(Location.OUTSIDE, s.location)
        assertEquals(4.5f, s.playerX, 0.001f)
        assertEquals(2.5f, s.playerY, 0.001f)
    }

    @Test
    fun cannotLeaveFromTheMiddleOfTheRoom() {
        val c = controller().inside()
        c.placeAt(4.5f, 3.5f)
        assertFalse(c.exitCabin())
        assertEquals(Location.CABIN, c.state.value.location)
    }

    @Test
    fun theDayClockStopsInsideTheCabin() {
        val c = controller().inside()
        c.update(0f, 0f, 4_000)
        assertEquals(10_000L, c.state.value.dayRemainingMs)
        c.placeAt(4.5f, 6.5f)
        c.exitCabin()
        c.update(0f, 0f, 4_000)
        assertEquals(6_000L, c.state.value.dayRemainingMs)
    }

    @Test
    fun insideTheCabin_wallsBlock() {
        val c = controller().inside()
        repeat(60) { c.update(0f, 1f, 16) } // hacia la pared de abajo (y = 7)
        assertTrue(c.state.value.playerY < 7f)
        repeat(300) { c.update(-1f, 0f, 16) }
        assertTrue(c.state.value.playerX >= 1f + WorldController.PLAYER_RADIUS - 0.001f)
    }

    // ---------- Cama ----------

    @Test
    fun bed_allowsSleepingByDay() {
        val c = controller().inside()
        c.placeAt(1.5f, 2.5f)
        assertEquals(TileType.BED, c.state.value.nearTile?.type)
        assertEquals(SleepCheck.OK, c.sleepCheck())
    }

    @Test
    fun bed_refusesSleepAtNight() {
        val c = controller()
        c.update(0f, 0f, 10_000) // anochece
        c.inside()
        c.placeAt(1.5f, 2.5f)
        assertEquals(SleepCheck.NIGHT, c.sleepCheck())
    }

    @Test
    fun sleepCheck_needsTheBed() {
        val c = controller().inside()
        assertEquals(SleepCheck.NOT_NEAR_BED, c.sleepCheck())
        assertEquals(SleepCheck.NOT_NEAR_BED, controller().sleepCheck())
    }

    // ---------- Cofre ----------

    private fun WorldController.atChest() = apply { placeAt(5.5f, 2.5f) }

    @Test
    fun chest_isDetectedNextToIt() {
        val c = controller().inside().atChest()
        assertEquals(TileType.CHEST, c.state.value.nearTile?.type)
    }

    @Test
    fun deposit_movesOneItemFromTheBagToTheChest() {
        val c = controller().withBag(coconuts = 3).inside().atChest()
        assertTrue(c.deposit(ItemType.COCONUT))
        assertEquals(2, c.state.value.count(ItemType.COCONUT))
        assertEquals(1, c.state.value.inChest(ItemType.COCONUT))
    }

    @Test
    fun withdraw_bringsItBack() {
        val c = controller().withBag(coconuts = 1, chestCoconuts = 4).inside().atChest()
        assertTrue(c.withdraw(ItemType.COCONUT))
        assertEquals(2, c.state.value.count(ItemType.COCONUT))
        assertEquals(3, c.state.value.inChest(ItemType.COCONUT))
    }

    @Test
    fun chest_onlyWorksNextToIt() {
        val c = controller().withBag(coconuts = 3).inside()
        assertFalse(c.canDeposit(ItemType.COCONUT))
        assertFalse(c.deposit(ItemType.COCONUT))
        assertEquals(3, c.state.value.count(ItemType.COCONUT))
    }

    @Test
    fun deposit_needsSomethingInTheBag() {
        val c = controller().withBag(chestCoconuts = 2).inside().atChest()
        assertFalse(c.deposit(ItemType.COAL))
    }

    @Test
    fun withdraw_respectsTheBagLimit() {
        val c = controller().withBag(coconuts = MAX_PER_ITEM, chestCoconuts = 5).inside().atChest()
        assertFalse(c.withdraw(ItemType.COCONUT))
        assertEquals(5, c.state.value.inChest(ItemType.COCONUT))
    }

    @Test
    fun withdraw_needsSomethingInTheChest() {
        val c = controller().withBag(coconuts = 1).inside().atChest()
        assertFalse(c.withdraw(ItemType.COCONUT))
    }

    @Test
    fun depositAll_emptiesTheBag() {
        val c = controller().withBag(coconuts = 3, coal = 2).inside().atChest()
        assertEquals(5, c.depositAll())
        assertTrue(c.state.value.inventory.values.all { it == 0 })
        assertEquals(3, c.state.value.inChest(ItemType.COCONUT))
        assertEquals(2, c.state.value.inChest(ItemType.COAL))
    }

    @Test
    fun theChestKeepsItsItemsAcrossDays() {
        val c = controller().withBag(coconuts = 2).inside().atChest()
        c.depositAll()
        c.placeAt(4.5f, 6.5f)
        c.exitCabin()
        c.update(0f, 0f, 10_000)
        assertTrue(c.startNextDay())
        assertEquals(2, c.state.value.inChest(ItemType.COCONUT))
    }

    // ---------- Guardado ----------

    @Test
    fun worldSave_roundTrips() {
        val c = controller().withBag(coconuts = 2, coal = 1, chestCoconuts = 4)
        c.update(0f, 0f, 10_000)
        c.startNextDay()
        val saved = c.toWorldSave()
        assertEquals(2, saved.day)
        assertEquals(mapOf("COCONUT" to 2, "COAL" to 1), saved.bag)
        assertEquals(mapOf("COCONUT" to 4), saved.chest)

        val other = controller()
        other.restore(saved)
        assertEquals(2, other.state.value.day)
        assertEquals(2, other.state.value.count(ItemType.COCONUT))
        assertEquals(1, other.state.value.count(ItemType.COAL))
        assertEquals(4, other.state.value.inChest(ItemType.COCONUT))
        assertEquals(DayPhase.DAY, other.state.value.phase)
        assertEquals(10_000L, other.state.value.dayRemainingMs)
    }

    @Test
    fun restore_ignoresUnknownItemsAndBadValues() {
        val c = controller()
        c.restore(WorldSave(day = 99, bag = mapOf("COCONUT" to 500, "PIEDRA_RARA" to 3), chest = mapOf("COAL" to -4)))
        assertEquals(TOTAL_DAYS, c.state.value.day)
        assertEquals(MAX_PER_ITEM, c.state.value.count(ItemType.COCONUT))
        assertEquals(0, c.state.value.inChest(ItemType.COAL))
    }

    @Test
    fun restore_putsThePlayerBackOnTheBeach() {
        val c = controller().inside()
        c.restore(WorldSave())
        assertEquals(Location.OUTSIDE, c.state.value.location)
        assertEquals(4.5f, c.state.value.playerX, 0.001f)
    }

    @Test
    fun saveGame_withWorld_survivesJson() {
        val save = SaveGame(world = WorldSave(day = 2, bag = mapOf("COAL" to 3), chest = mapOf("COCONUT" to 7)))
        val text = GameJson.encodeToString(SaveGame.serializer(), save)
        assertEquals(save, GameJson.decodeFromString(SaveGame.serializer(), text))
    }

    @Test
    fun oldSavesWithoutWorld_stillLoad() {
        val old = """{"version":1,"phase":"INTRO","affinity":{"valentina":30}}"""
        val save = GameJson.decodeFromString(SaveGame.serializer(), old)
        assertNull(save.world)
        assertEquals(30, save.affinity["valentina"])
    }

    // ---------- Datos de la cabaña y de la isla ----------

    @Test
    fun suitcases_haveTextsAndADefault() {
        assertTrue(CabinWorld.suitcaseText(7, 1).isNotBlank())
        assertEquals(CabinWorld.DEFAULT_SUITCASE_TEXT, CabinWorld.suitcaseText(0, 0))
    }

    @Test
    fun cabin_hasABedAChestSuitcasesAndOneExit() {
        val tiles = (0 until CabinWorld.map.height).flatMap { y -> (0 until CabinWorld.map.width).map { x -> CabinWorld.map.tileAt(x, y) } }
        assertEquals(2, tiles.count { it == TileType.BED })
        assertEquals(1, tiles.count { it == TileType.CHEST })
        assertTrue(tiles.count { it == TileType.SUITCASE } >= 3)
        assertEquals(1, tiles.count { it == TileType.EXIT_DOOR })
    }

    @Test
    fun cabin_everyObjectCanBeReachedOnFoot() {
        val map = CabinWorld.map
        for (y in 0 until map.height) for (x in 0 until map.width) {
            if (map.tileAt(x, y)?.interactable != true) continue
            val neighbours = listOf(x - 1 to y, x + 1 to y, x to y - 1, x to y + 1)
            assertTrue(neighbours.any { (nx, ny) -> map.isWalkable(nx, ny) }, "No se llega a ($x, $y)")
        }
        assertTrue(map.isWalkable(CabinWorld.START_X.toInt(), CabinWorld.START_Y.toInt()))
    }

    @Test
    fun island_hasOneDoorAndCanEnterFromBelow() {
        val m = IslaWorld.map
        val doors = (0 until m.height).flatMap { y -> (0 until m.width).map { x -> x to y } }
            .filter { (x, y) -> m.tileAt(x, y) == TileType.HUT_DOOR }
        assertEquals(1, doors.size)
        val (dx, dy) = doors.single()
        assertTrue(m.isWalkable(dx, dy + 1))

        val c = IslaWorld.newController()
        c.placeAt(dx + 0.5f, dy + 1.5f)
        assertNotNull(c.state.value.nearTile)
        assertTrue(c.enterCabin())
    }
}
