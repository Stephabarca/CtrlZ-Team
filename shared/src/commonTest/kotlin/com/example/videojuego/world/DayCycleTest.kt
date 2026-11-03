package com.example.videojuego.world

import com.example.videojuego.narrative.Speaker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DayCycleTest {

    /** Arena con una palmera en (4,2), una roca en (7,2) y la fogata en (4,8). */
    private fun map() = WorldMap(
        listOf(
            "~~~~~~~~~~",
            "~........~",
            "~...T..r.~",
            "~........~",
            "~........~",
            "~........~",
            "~........~",
            "~........~",
            "~...F....~",
            "~~~~~~~~~~",
        ),
    )

    private fun npc() = NpcData(
        "valentina", "Valentina", Speaker.VALENTINA, 2, 5, "night1", "espera",
        chatLines = listOf("uno", "dos"),
    )

    private fun controller(x: Float = 4.5f, y: Float = 5.5f, dayMs: Long = 10_000L) =
        WorldController(map(), listOf(npc()), x, y, dayLengthMs = dayMs, totalDays = 3)

    /** Camina en pasos de 16 ms (como en el juego); un paso grande chocaría y no avanzaría. */
    private fun WorldController.walkTo(dx: Float, dy: Float, ms: Long) =
        repeat(maxOf(1L, ms / 16L).toInt()) { update(dx, dy, 16) }

    // ---------- Reloj ----------

    @Test
    fun dayStartsAtFiveMinutes() {
        val c = WorldController(map(), emptyList(), 4.5f, 5.5f)
        assertEquals(3 * 60 * 1000L, c.state.value.dayRemainingMs)
        assertEquals(DayPhase.DAY, c.state.value.phase)
        assertEquals(1, c.state.value.day)
    }

    @Test
    fun clock_countsDown() {
        val c = controller()
        c.update(0f, 0f, 4_000)
        assertEquals(6_000L, c.state.value.dayRemainingMs)
        assertEquals(DayPhase.DAY, c.state.value.phase)
    }

    @Test
    fun whenTimeRunsOut_itBecomesNight() {
        val c = controller()
        c.update(0f, 0f, 10_000)
        assertEquals(0L, c.state.value.dayRemainingMs)
        assertEquals(DayPhase.NIGHT, c.state.value.phase)
    }

    @Test
    fun clock_doesNotGoBelowZero() {
        val c = controller()
        c.update(0f, 0f, 50_000)
        assertEquals(0L, c.state.value.dayRemainingMs)
    }

    @Test
    fun pausedClock_doesNotAdvance() {
        val c = controller()
        c.update(0f, 0f, 4_000, clockPaused = true)
        assertEquals(10_000L, c.state.value.dayRemainingMs)
    }

    @Test
    fun formatClock_roundsUp() {
        assertEquals("5:00", formatClock(300_000))
        assertEquals("4:59", formatClock(299_000))
        assertEquals("5:00", formatClock(299_001))
        assertEquals("0:01", formatClock(1))
        assertEquals("0:00", formatClock(0))
        assertEquals("0:00", formatClock(-5))
    }

    @Test
    fun darkness_isZeroByDayAndGrowsAtDusk() {
        assertEquals(0f, darknessAt(DayPhase.DAY, DAY_LENGTH_MS), 0.0001f)
        assertEquals(0f, darknessAt(DayPhase.DAY, DUSK_MS), 0.0001f)
        val half = darknessAt(DayPhase.DAY, DUSK_MS / 2)
        assertTrue(half > 0f)
        assertTrue(half < darknessAt(DayPhase.NIGHT, 0))
        assertEquals(darknessAt(DayPhase.NIGHT, 0), darknessAt(DayPhase.DAY, 0), 0.0001f)
    }

    // ---------- Recolección ----------

    @Test
    fun touchingAPalm_givesACoconut() {
        val c = controller(4.5f, 3.5f)
        c.walkTo(0f, -1f, 200) // sube hasta pegarse a la palmera de (4,2)
        assertEquals(1, c.state.value.count(ItemType.COCONUT))
        assertEquals(ItemType.COCONUT, c.state.value.lastHarvest)
        assertEquals(1, c.state.value.harvestSeq)
    }

    @Test
    fun touchingARock_givesCoal() {
        val c = controller(7.5f, 3.5f)
        c.walkTo(0f, -1f, 200)
        assertEquals(1, c.state.value.count(ItemType.COAL))
    }

    @Test
    fun eachPalm_givesOnlyOneItemPerDay() {
        val c = controller(4.5f, 3.5f)
        repeat(30) { c.walkTo(0f, -1f, 50) }
        repeat(30) { c.walkTo(0f, 1f, 50) }
        repeat(30) { c.walkTo(0f, -1f, 50) }
        assertEquals(1, c.state.value.count(ItemType.COCONUT))
    }

    @Test
    fun nothingIsHarvestedAtNight() {
        val c = controller(4.5f, 3.5f)
        c.update(0f, 0f, 10_000) // anochece
        c.walkTo(0f, -1f, 200)
        assertEquals(0, c.state.value.count(ItemType.COCONUT))
    }

    @Test
    fun walkingAwayFromTrees_collectsNothing() {
        val c = controller()
        c.walkTo(0f, 1f, 300)
        assertTrue(c.state.value.inventory.isEmpty())
    }

    // ---------- Regalos ----------

    private fun controllerWithBag(): WorldController {
        val c = controller(4.5f, 3.5f)
        c.walkTo(0f, -1f, 200) // coco
        c.placeAt(3.5f, 5.5f)  // al lado de Valentina
        return c
    }

    @Test
    fun gift_needsTheItemAndBeingClose() {
        val c = controller(4.5f, 5.5f)
        assertFalse(c.canGift("valentina", ItemType.COCONUT)) // sin coco
        val d = controllerWithBag()
        assertTrue(d.canGift("valentina", ItemType.COCONUT))
        d.placeAt(7.5f, 6.5f)
        assertFalse(d.canGift("valentina", ItemType.COCONUT)) // lejos
    }

    @Test
    fun gift_spendsTheItemAndReturnsTheAffinity() {
        val c = controllerWithBag()
        val result = c.giveGift("valentina", ItemType.COCONUT)
        assertNotNull(result)
        assertEquals(GiftRules.MAX_GIFT_DELTA, result.affinityDelta)
        assertEquals(0, c.state.value.count(ItemType.COCONUT))
        assertTrue("valentina" in c.state.value.giftedNpcIds)
    }

    @Test
    fun onlyOneGiftPerNpcPerDay_andItRenewsTomorrow() {
        val c = controllerWithBag()
        c.giveGift("valentina", ItemType.COCONUT)
        // Otro coco: la palmera ya se recogió hoy, así que se simula con otra roca.
        c.placeAt(7.5f, 3.5f)
        c.walkTo(0f, -1f, 200)
        c.placeAt(3.5f, 5.5f)
        assertEquals(1, c.state.value.count(ItemType.COAL))
        assertFalse(c.canGift("valentina", ItemType.COAL))
        assertNull(c.giveGift("valentina", ItemType.COAL))
        c.update(0f, 0f, 10_000)
        assertTrue(c.startNextDay())
        c.placeAt(3.5f, 5.5f)
        assertTrue(c.canGift("valentina", ItemType.COAL))
    }

    @Test
    fun giftsCannotBeGivenAtNight() {
        val c = controllerWithBag()
        c.update(0f, 0f, 10_000)
        assertFalse(c.canGift("valentina", ItemType.COCONUT))
    }

    @Test
    fun likedItemGivesMoreThanOthers() {
        assertTrue(GiftRules.affinityDelta("valentina", ItemType.COCONUT) > GiftRules.affinityDelta("valentina", ItemType.COAL))
        assertTrue(GiftRules.affinityDelta("lucas", ItemType.COAL) > GiftRules.affinityDelta("lucas", ItemType.COCONUT))
        for (n in IslaWorld.npcs) for (i in ItemType.entries) {
            assertTrue(GiftRules.affinityDelta(n.id, i) in 1..GiftRules.MAX_GIFT_DELTA)
            assertTrue(GiftRules.reaction(n.id, i).isNotBlank())
        }
    }

    // ---------- Conversaciones cortas ----------

    @Test
    fun talk_cyclesThroughTheLines() {
        val c = controller()
        assertEquals("uno", c.talk("valentina"))
        assertEquals("dos", c.talk("valentina"))
        assertEquals("uno", c.talk("valentina"))
        assertNull(c.talk("nadie"))
    }

    @Test
    fun talk_fallsBackToIdleLineWhenThereAreNoChatLines() {
        val c = WorldController(map(), listOf(NpcData("a", "A", Speaker.LUCAS, 2, 5, "night1", "espera")), 4.5f, 5.5f)
        assertEquals("espera", c.talk("a"))
    }

    // ---------- Fogata y noche ----------

    @Test
    fun touchingTheFireByDay_doesNothing() {
        val c = controller(4.5f, 6.5f)
        c.walkTo(0f, 1f, 400)
        assertTrue(c.state.value.nearFire)
        assertNull(c.state.value.nightTrigger)
    }

    @Test
    fun touchingTheFireAtNight_startsNightOne() {
        val c = controller(4.5f, 6.5f)
        c.update(0f, 0f, 10_000)
        assertNull(c.state.value.nightTrigger)
        c.walkTo(0f, 1f, 400)
        assertEquals("night1", c.state.value.nightTrigger)
    }

    @Test
    fun acknowledgingTheNight_clearsTheTriggerWithoutRepeating() {
        val c = controller(4.5f, 6.5f)
        c.update(0f, 0f, 10_000)
        c.walkTo(0f, 1f, 400)
        c.acknowledgeNight()
        assertNull(c.state.value.nightTrigger)
        // Sigue junto a la fogata, pero la noche ya empezó: no se dispara otra vez.
        c.update(0f, 0f, 16)
        assertNull(c.state.value.nightTrigger)
    }

    @Test
    fun nextDay_resetsClockHarvestAndPosition_andAdvancesTheNight() {
        val c = controller(4.5f, 3.5f)
        c.walkTo(0f, -1f, 200)
        c.update(0f, 0f, 10_000)
        assertTrue(c.startNextDay())
        val s = c.state.value
        assertEquals(2, s.day)
        assertEquals(DayPhase.DAY, s.phase)
        assertEquals(10_000L, s.dayRemainingMs)
        assertTrue(s.harvested.isEmpty())
        assertEquals(1, s.count(ItemType.COCONUT)) // la mochila se conserva
        assertEquals(4.5f, s.playerX, 0.001f)
        assertEquals(3.5f, s.playerY, 0.001f)
        assertNull(s.nightTrigger)

        c.update(0f, 0f, 10_000)
        c.placeAt(4.5f, 7.0f)
        c.walkTo(0f, 1f, 200)
        assertEquals("night2", c.state.value.nightTrigger)
    }

    @Test
    fun startNextDay_returnsFalseAfterTheLastDay() {
        val c = controller()
        assertTrue(c.startNextDay())
        assertTrue(c.startNextDay())
        assertFalse(c.startNextDay())
        assertEquals(3, c.state.value.day)
    }

    @Test
    fun inventoryIsCapped() {
        val big = WorldMap(
            listOf(
                "~~~~~~~~~~~~~~~~~~~~~~~",
                "~.T.T.T.T.T.T.T.T.T.T.~",
                "~.....................~",
                "~~~~~~~~~~~~~~~~~~~~~~~",
            ).map { it.take(23).padEnd(23, '~') },
        )
        val c = WorldController(big, emptyList(), 1.5f, 2.31f, dayLengthMs = 600_000)
        repeat(400) { c.update(1f, 0f, 16) }
        assertEquals(MAX_PER_ITEM, c.state.value.count(ItemType.COCONUT))
    }
}
