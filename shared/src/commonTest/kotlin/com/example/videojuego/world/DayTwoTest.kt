package com.example.videojuego.world

import com.example.videojuego.data.save.AnswerRecord
import com.example.videojuego.data.save.MinigameRecord
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.data.save.WorldSave
import com.example.videojuego.narrative.Speaker
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DayTwoTest {

    private fun openMap(width: Int = 12, height: Int = 12) = WorldMap(
        List(height) { y ->
            if (y == 0 || y == height - 1) "~".repeat(width) else "~" + ".".repeat(width - 2) + "~"
        },
    )

    private val lucas = NpcData(
        id = "lucas", name = "Lucas", speaker = Speaker.LUCAS, tileX = 5, tileY = 4, nightId = "night2", idleLine = "idle",
        chatLines = listOf("c1", "c2"),
        dayLines = mapOf(2 to listOf("d1", "d2")),
        memoryLines = listOf("n1_sana" to "memoria sana", "pesca_buena" to "memoria pesca"),
    )

    private fun controller(dayMs: Long = 10_000L) = WorldController(openMap(), listOf(lucas), 5.5f, 5.5f, dayMs)

    private fun WorldController.at(day: Int, wood: Int = 0) = apply {
        restore(WorldSave(day = day, bag = if (wood > 0) mapOf("WOOD" to wood) else emptyMap()))
    }

    // ---------- Madera ----------

    /** Un árbol en (3,2) y el jugador pegado debajo. */
    private fun treeController(wood: Set<Pair<Int, Int>>) = WorldController(
        WorldMap(
            listOf(
                "~~~~~~~~",
                "~......~",
                "~..T...~",
                "~......~",
                "~......~",
                "~......~",
                "~~~~~~~~",
            ),
        ),
        emptyList(), 3.5f, 3.4f, woodTrees = wood,
    )

    @Test
    fun day1_markedTreeStillGivesCoconuts() {
        val c = treeController(setOf(3 to 2))
        c.update(0f, 0f, 16)
        assertEquals(1, c.state.value.count(ItemType.COCONUT))
        assertEquals(0, c.state.value.count(ItemType.WOOD))
    }

    @Test
    fun day2_markedTreeGivesWood() {
        val c = treeController(setOf(3 to 2))
        assertTrue(c.startNextDay())
        c.update(0f, 0f, 16)
        assertEquals(1, c.state.value.count(ItemType.WOOD))
        assertEquals(0, c.state.value.count(ItemType.COCONUT))
    }

    @Test
    fun day2_unmarkedTreeStillGivesCoconuts() {
        val c = treeController(emptySet())
        c.startNextDay()
        c.update(0f, 0f, 16)
        assertEquals(1, c.state.value.count(ItemType.COCONUT))
        assertEquals(0, c.state.value.count(ItemType.WOOD))
    }

    @Test
    fun islaWoodTrees_areTreesAndEnoughForTheGoal() {
        for ((x, y) in IslaWorld.woodTrees) assertEquals(TileType.TREE, IslaWorld.map.tileAt(x, y), "($x, $y)")
        assertTrue(IslaWorld.woodTrees.size >= DayGoals.forDay(2)!!.amount)
    }

    // ---------- Objetivo del día ----------

    @Test
    fun goal_existsOnlyOnDay2() {
        val c = controller()
        assertNull(c.currentGoal())
        c.startNextDay()
        assertEquals("lucas", c.currentGoal()?.npcId)
    }

    @Test
    fun canDeliver_needsEnoughWood() {
        assertFalse(controller().at(day = 2, wood = 2).canDeliverGoal("lucas"))
        assertTrue(controller().at(day = 2, wood = 3).canDeliverGoal("lucas"))
    }

    @Test
    fun canDeliver_notOnDay1() {
        assertFalse(controller().at(day = 1, wood = 3).canDeliverGoal("lucas"))
    }

    @Test
    fun canDeliver_notAtNight() {
        val c = controller(dayMs = 1_000L).at(day = 2, wood = 3)
        c.update(0f, 0f, 1_000L)
        assertTrue(c.state.value.isNight)
        assertFalse(c.canDeliverGoal("lucas"))
    }

    @Test
    fun canDeliver_notForAnotherCharacter() {
        assertFalse(controller().at(day = 2, wood = 3).canDeliverGoal("valentina"))
    }

    @Test
    fun deliver_spendsTheWood_andGivesAffinity_once() {
        val c = controller().at(day = 2, wood = 4)
        val result = assertNotNull(c.deliverGoal("lucas"))
        assertEquals(GiftRules.MAX_GIFT_DELTA, result.affinityDelta)
        assertEquals(DayGoals.forDay(2)!!.doneLine, result.line)
        assertEquals(1, c.state.value.count(ItemType.WOOD))
        assertNull(c.currentGoal())
        assertNull(c.deliverGoal("lucas"))
    }

    @Test
    fun goalDone_isSavedAndRestored() {
        val c = controller().at(day = 2, wood = 3)
        c.deliverGoal("lucas")
        val saved = c.toWorldSave()
        assertEquals(listOf(2), saved.goalsDone)
        val other = controller()
        other.restore(saved)
        assertNull(other.currentGoal())
    }

    @Test
    fun oldSaves_withoutGoalsDone_stillLoad() {
        val save = Json.decodeFromString(WorldSave.serializer(), """{"day":2,"bag":{"COCONUT":1}}""")
        assertEquals(emptyList(), save.goalsDone)
        assertEquals(1, controller().apply { restore(save) }.state.value.count(ItemType.COCONUT))
    }

    // ---------- Conversaciones del día 2 ----------

    @Test
    fun day1_usesTheNormalChat() {
        val c = controller().at(day = 1)
        assertEquals("c1", c.talk("lucas"))
        assertEquals("c2", c.talk("lucas"))
        assertEquals("c1", c.talk("lucas"))
    }

    @Test
    fun day2_remembersLastNight_thenAsksForHelp_thenChats() {
        val c = controller().at(day = 2)
        c.setMemory(setOf("n1_sana", "pesca_buena"))
        assertEquals("memoria sana", c.talk("lucas"))
        assertEquals(DayGoals.forDay(2)!!.askLine, c.talk("lucas"))
        assertEquals("d1", c.talk("lucas"))
        assertEquals("d2", c.talk("lucas"))
        assertEquals("d1", c.talk("lucas"))
    }

    @Test
    fun day2_withoutMemory_startsWithTheRequest() {
        val c = controller().at(day = 2)
        assertEquals(DayGoals.forDay(2)!!.askLine, c.talk("lucas"))
        assertEquals("d1", c.talk("lucas"))
    }

    @Test
    fun memory_usesTheFirstMatchingFlagByPriority() {
        val c = controller().at(day = 2)
        c.setMemory(setOf("pesca_buena"))
        assertEquals("memoria pesca", c.talk("lucas"))
    }

    @Test
    fun afterDeliveringTheGoal_theRequestIsNotRepeated() {
        val c = controller().at(day = 2, wood = 3)
        c.deliverGoal("lucas")
        assertEquals("d1", c.talk("lucas"))
    }

    @Test
    fun lucasHasDay2LinesAndMemoriesInTheRealIsland() {
        val real = IslaWorld.npc("lucas")!!
        assertTrue(real.dayLines[2].orEmpty().isNotEmpty())
        assertTrue(real.memoryLines.isNotEmpty())
    }

    // ---------- Banderas de memoria ----------

    private fun answer(id: String, delta: Int, timedOut: Boolean = false) =
        AnswerRecord(id, if (timedOut) null else "${id}a", 4_000L, timedOut, delta)

    @Test
    fun flags_noSave_noFlags() {
        assertTrue(MemoryFlags.from(null).isEmpty())
        assertTrue(MemoryFlags.from(SaveGame()).isEmpty())
    }

    @Test
    fun flags_healthyNight1() {
        val save = SaveGame(answers = listOf(answer("n1q1", 10), answer("n1q2", 5)))
        assertEquals(setOf("n1_sana"), MemoryFlags.from(save))
    }

    @Test
    fun flags_riskyAnswerOnNight1() {
        val save = SaveGame(answers = listOf(answer("n1q1", 10), answer("n1q2", -5)))
        assertEquals(setOf("n1_riesgo"), MemoryFlags.from(save))
    }

    @Test
    fun flags_timeoutIsNotHealthy() {
        val save = SaveGame(answers = listOf(answer("n1q1", 10), answer("n1q2", 0, timedOut = true)))
        assertFalse("n1_sana" in MemoryFlags.from(save))
    }

    @Test
    fun flags_ignoreOtherNights() {
        val save = SaveGame(answers = listOf(answer("n2q1", -10)))
        assertTrue(MemoryFlags.from(save).isEmpty())
    }

    @Test
    fun flags_fishingScore() {
        val good = SaveGame(minigameResults = listOf(MinigameRecord("pesca", 80, 12)))
        val poor = SaveGame(minigameResults = listOf(MinigameRecord("pesca", 20, 3)))
        assertEquals(setOf("pesca_buena"), MemoryFlags.from(good))
        assertEquals(setOf("pesca_floja"), MemoryFlags.from(poor))
    }
}
