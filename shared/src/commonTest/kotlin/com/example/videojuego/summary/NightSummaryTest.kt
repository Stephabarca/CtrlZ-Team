package com.example.videojuego.summary

import com.example.videojuego.data.TestFixtures
import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.minigame.fishing.FishingGame
import com.example.videojuego.minigame.fishing.FishingStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NightSummaryTest {

    private val content = TestFixtures.content()
    private val night = content.nights.first { it.id == "night1" }
    private val question = night.questions.first()
    private val start = ProgressRules.newGame(content)

    private fun answered(answerId: String?, timeMs: Long = 4_000L) = ProgressRules.recordAnswer(
        content, start, night, question, question.answers.firstOrNull { it.id == answerId }, timeMs,
    )

    private val perfectFishing = FishingStats(score = 100, perfect = 6, good = 0, weak = 0, lost = 0, early = 0)

    private fun withMinigame(save: com.example.videojuego.data.save.SaveGame, stats: FishingStats) =
        ProgressRules.recordMinigame(content, save, content.minigame("pesca"), stats.score)

    @Test
    fun healthyNight_showsGainsFromQuestionsAndMinigame() {
        val end = withMinigame(answered("n1q1a1"), perfectFishing)
        val s = NightSummaryBuilder.build(content, night, start, end, perfectFishing)
        assertEquals(30, s.affinityBefore)
        assertEquals(55, s.affinityAfter) // +10 pregunta, +15 pesca
        assertEquals(25, s.totalAffinityChange)
        assertEquals(10, s.questionsAffinity)
        assertEquals(1, s.healthyCount)
        assertEquals("Amistad", s.stageName)
        assertEquals(0.55f, s.affinityFraction, 0.001f)
        assertNotNull(s.minigame)
        assertEquals(15, s.minigame.affinityGranted)
    }

    @Test
    fun questionReview_explainsTheChosenAnswer() {
        val end = answered("n1q1a1")
        val review = NightSummaryBuilder.build(content, night, start, end, null).questions.single()
        assertEquals("¿Pregunta?", review.prompt)
        assertEquals("mejor", review.chosenText)
        assertEquals("Escucha", review.motiveLabel)
        assertEquals(true, review.healthy)
        assertEquals(10, review.affinityDelta)
        assertEquals("bien", review.feedback)
    }

    @Test
    fun riskyAnswer_isMarkedAndAddedToImprovements() {
        val end = answered("n1q1a4")
        val s = NightSummaryBuilder.build(content, night, start, end, null)
        assertEquals(false, s.questions.single().healthy)
        assertEquals(0, s.healthyCount)
        assertEquals(listOf("mal"), s.improvements)
        assertEquals(-10, s.totalAffinityChange)
        assertTrue(s.headline.contains("resintió"))
    }

    @Test
    fun timeout_isReportedWithoutAnAnswer() {
        val end = answered(null, 20_000L)
        val s = NightSummaryBuilder.build(content, night, start, end, null)
        val review = s.questions.single()
        assertNull(review.chosenText)
        assertTrue(review.timedOut)
        assertEquals(NightSummaryBuilder.DEFAULT_TIMEOUT_TIP, review.feedback)
        assertNull(s.averageSeconds)
        assertEquals(0, s.answeredCount)
        assertEquals(1, s.improvements.size)
    }

    @Test
    fun averageTime_isInSeconds() {
        val s = NightSummaryBuilder.build(content, night, start, answered("n1q1a2", 6_500L), null)
        assertEquals(6.5f, s.averageSeconds ?: -1f, 0.001f)
    }

    @Test
    fun withoutMinigameStats_noMinigameSection() {
        val s = NightSummaryBuilder.build(content, night, start, answered("n1q1a1"), null)
        assertNull(s.minigame)
    }

    @Test
    fun learningComesFromTheCharacter() {
        val s = NightSummaryBuilder.build(content, night, start, answered("n1q1a1"), null)
        assertEquals(content.character("valentina").learning, s.learning)
    }

    @Test
    fun healthyAnswersEarnAPositiveHeadline() {
        val s = NightSummaryBuilder.build(content, night, start, answered("n1q1a1"), null)
        assertTrue(s.headline.startsWith("¡Gran noche!"))
    }

    // ---------- Consejos de la pesca ----------

    @Test
    fun tips_mentionEarlyPullsAndLostFish() {
        val tips = NightSummaryBuilder.fishingTips(FishingStats(20, 1, 0, 0, 2, 3))
        assertTrue(tips.any { it.contains("3 veces") && it.contains("antes de tiempo") })
        assertTrue(tips.any { it.contains("2 peces") })
    }

    @Test
    fun tips_praiseGoodRhythm() {
        val tips = NightSummaryBuilder.fishingTips(FishingStats(80, 4, 1, 0, 0, 0))
        assertTrue(tips.any { it.contains("buen ritmo") })
    }

    @Test
    fun tips_alwaysHaveAtLeastOne() {
        assertTrue(NightSummaryBuilder.fishingTips(FishingStats(60, 3, 0, 0, 0, 0)).isNotEmpty())
        assertTrue(NightSummaryBuilder.fishingTips(FishingGame.statsOf(emptyList())).isNotEmpty())
    }

    @Test
    fun onlyThisNightsAnswersAreCounted() {
        val night2 = content.nights.first { it.id == "night2" }
        val other = ProgressRules.recordAnswer(content, answered("n1q1a1"), night2, night2.questions.first(), night2.questions.first().answers.first(), 1_000)
        val s = NightSummaryBuilder.build(content, night, start, other, null)
        assertEquals(1, s.questions.size)
    }
}
