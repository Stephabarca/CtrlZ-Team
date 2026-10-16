package com.example.videojuego.data

import com.example.videojuego.data.rules.ProgressRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgressRulesTest {

    private val content = TestFixtures.content()
    private val night1 = content.nights[0]
    private val night3 = content.nights[2]

    private fun answerOf(night: com.example.videojuego.data.content.NightData, suffix: String) =
        night.questions[0].answers.first { it.id.endsWith(suffix) }

    @Test
    fun newGame_startsEveryCharacterAtStartAffinity() {
        val save = ProgressRules.newGame(content)
        assertEquals(setOf("valentina", "lucas", "lila"), save.affinity.keys)
        assertTrue(save.affinity.values.all { it == 30 })
    }

    @Test
    fun recordAnswer_addsDeltaAndStoresRecord() {
        val save = ProgressRules.recordAnswer(
            content, ProgressRules.newGame(content), night1, night1.questions[0], answerOf(night1, "a1"), 4_000,
        )
        assertEquals(40, save.affinity["valentina"])
        val record = save.answers.single()
        assertEquals("n1q1a1", record.answerId)
        assertEquals(4_000, record.timeSpentMs)
        assertFalse(record.timedOut)
        assertEquals(10, record.affinityDelta)
    }

    @Test
    fun recordAnswer_neverExceedsMax() {
        val start = ProgressRules.newGame(content).let { it.copy(affinity = it.affinity + ("valentina" to 95)) }
        val save = ProgressRules.recordAnswer(content, start, night1, night1.questions[0], answerOf(night1, "a1"), 1_000)
        assertEquals(100, save.affinity["valentina"])
        assertEquals(5, save.answers.single().affinityDelta)
    }

    @Test
    fun recordAnswer_neverGoesBelowMin() {
        val start = ProgressRules.newGame(content).let { it.copy(affinity = it.affinity + ("valentina" to 3)) }
        val save = ProgressRules.recordAnswer(content, start, night1, night1.questions[0], answerOf(night1, "a4"), 1_000)
        assertEquals(0, save.affinity["valentina"])
        assertEquals(-3, save.answers.single().affinityDelta)
    }

    @Test
    fun recordAnswer_timeout_appliesDefaultEffectAndMarksTimedOut() {
        val save = ProgressRules.recordAnswer(
            content, ProgressRules.newGame(content), night1, night1.questions[0], null, 20_000,
        )
        assertEquals(28, save.affinity["valentina"])
        val record = save.answers.single()
        assertTrue(record.timedOut)
        assertEquals(null, record.answerId)
    }

    @Test
    fun recordAnswer_appliesNightWeight() {
        val save = ProgressRules.recordAnswer(
            content, ProgressRules.newGame(content), night3, night3.questions[0], answerOf(night3, "a1"), 3_000,
        )
        assertEquals(42, save.affinity["lila"]) // 30 + 10 * 1.2
    }

    @Test
    fun recordAnswer_onlyChangesTheNightsCharacter() {
        val save = ProgressRules.recordAnswer(
            content, ProgressRules.newGame(content), night1, night1.questions[0], answerOf(night1, "a1"), 1_000,
        )
        assertEquals(30, save.affinity["lucas"])
        assertEquals(30, save.affinity["lila"])
    }

    @Test
    fun recordMinigame_grantsProportionalAffinity() {
        val pesca = content.minigame("pesca")
        val save = ProgressRules.recordMinigame(content, ProgressRules.newGame(content), pesca, 50)
        assertEquals(37, save.affinity["valentina"]) // 30 + 15 * 50 / 100
        assertEquals(7, save.minigameResults.single().affinityGranted)
    }

    @Test
    fun recordMinigame_clampsScoreAndCannotFillTheBondAlone() {
        val pesca = content.minigame("pesca")
        val save = ProgressRules.recordMinigame(content, ProgressRules.newGame(content), pesca, 500)
        assertEquals(100, save.minigameResults.single().score)
        assertTrue(save.affinity.getValue("valentina") < 70)
    }

    @Test
    fun stageFor_returnsStageByThreshold() {
        val cfg = content.config
        assertEquals("desconocidos", ProgressRules.stageFor(0, cfg).id)
        assertEquals("conocidos", ProgressRules.stageFor(25, cfg).id)
        assertEquals("amistad", ProgressRules.stageFor(74, cfg).id)
        assertEquals("cercania", ProgressRules.stageFor(100, cfg).id)
    }

    @Test
    fun resolveEnding_reachesThreeDifferentEndings() {
        val base = ProgressRules.newGame(content)
        fun ending(v: Int, l: Int, li: Int) = ProgressRules.resolveEnding(
            content, base.copy(affinity = mapOf("valentina" to v, "lucas" to l, "lila" to li)),
        ).id

        assertEquals("juntos", ending(80, 75, 50))
        assertEquals("vinculo", ending(80, 30, 30))
        assertEquals("solos", ending(20, 30, 40))
        assertEquals("vinculo", ending(90, 90, 20)) // dos altos, pero uno por debajo de 40
    }

    @Test
    fun finish_setsEndingPhaseAndId() {
        val save = ProgressRules.finish(content, ProgressRules.newGame(content))
        assertEquals(com.example.videojuego.data.save.GamePhase.ENDING, save.phase)
        assertEquals("solos", save.endingId)
    }
}
