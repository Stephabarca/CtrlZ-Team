package com.example.videojuego.round

import com.example.videojuego.data.TestFixtures
import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.rules.ProgressRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuestionRoundControllerTest {

    /** Contenido de prueba con [perNight] preguntas por noche (los IDs quedan únicos). */
    private fun contentWith(perNight: Int): GameContent {
        val base = TestFixtures.content()
        return base.copy(
            nights = base.nights.map { night ->
                val template = night.questions.first()
                night.copy(
                    questions = (1..perNight).map { i ->
                        template.copy(
                            id = "${night.id}q$i",
                            answers = template.answers.mapIndexed { j, a -> a.copy(id = "${night.id}q${i}a${j + 1}") },
                        )
                    },
                )
            },
        )
    }

    private val content = contentWith(3)
    private val night = content.nights[0]

    private fun controller() = QuestionRoundController(content, night, ProgressRules.newGame(content))

    @Test
    fun answering_appliesAffinityAndShowsFeedbackWithoutAdvancing() {
        val c = controller()
        c.answer("night1q1a1", 4_000)

        val s = c.state.value
        val fb = assertNotNull(s.feedback)
        assertEquals(40, s.affinity)
        assertEquals(10, fb.appliedDelta)
        assertFalse(fb.timedOut)
        assertEquals(true, fb.healthy)
        assertEquals(0, s.questionIndex)
        assertEquals(4_000, s.save.answers.single().timeSpentMs)
    }

    @Test
    fun next_afterAnswer_movesToNextQuestion() {
        val c = controller()
        c.answer("night1q1a1", 1_000)
        c.next()

        val s = c.state.value
        assertEquals(1, s.questionIndex)
        assertEquals("night1q2", s.question.id)
        assertNull(s.feedback)
        assertEquals(40, s.affinity) // la afinidad se conserva entre preguntas
    }

    @Test
    fun next_withoutAnswering_doesNothing() {
        val c = controller()
        val before = c.state.value
        c.next()
        assertEquals(before, c.state.value)
    }

    @Test
    fun answering_twice_onlyCountsTheFirstAnswer() {
        val c = controller()
        c.answer("night1q1a1", 1_000)
        c.answer("night1q1a4", 2_000)

        assertEquals(1, c.state.value.save.answers.size)
        assertEquals(40, c.state.value.affinity)
    }

    @Test
    fun timeExpired_appliesDefaultEffectAndMarksNoAnswer() {
        val c = controller()
        c.timeExpired(20_000)

        val s = c.state.value
        val fb = assertNotNull(s.feedback)
        assertTrue(fb.timedOut)
        assertNull(fb.chosenAnswerId)
        assertEquals(-2, fb.appliedDelta)
        assertEquals(28, s.affinity)
        assertEquals(QuestionRoundController.DEFAULT_TIMEOUT_MESSAGE, fb.message)
    }

    @Test
    fun answerAndTimeoutAtTheSameInstant_onlyOneEventIsProcessed() {
        val c = controller()
        c.answer("night1q1a1", 19_900)
        c.timeExpired(20_000)

        val s = c.state.value
        assertEquals(1, s.save.answers.size)
        assertFalse(assertNotNull(s.feedback).timedOut)
        assertEquals(40, s.affinity)
    }

    @Test
    fun unknownAnswerId_isIgnored() {
        val c = controller()
        val before = c.state.value
        c.answer("no-existe", 1_000)
        assertEquals(before, c.state.value)
    }

    @Test
    fun playingAllQuestions_finishesWithEveryAnswerRecorded() {
        val c = controller()
        for (i in 1..3) {
            c.answer("night1q${i}a2", 1_000) // +5 cada una
            c.next()
        }

        val s = c.state.value
        assertTrue(s.finished)
        assertEquals(3, s.save.answers.size)
        assertEquals(45, s.affinity)
    }

    @Test
    fun finished_ignoresFurtherEvents() {
        val c = controller()
        for (i in 1..3) { c.answer("night1q${i}a1", 500); c.next() }
        val done = c.state.value
        c.answer("night1q3a1", 500)
        c.timeExpired(500)
        c.next()
        assertEquals(done, c.state.value)
    }

    @Test
    fun onlyTheNightsCharacterChanges() {
        val c = controller()
        c.answer("night1q1a1", 1_000)
        val affinity = c.state.value.save.affinity
        assertEquals(40, affinity["valentina"])
        assertEquals(30, affinity["lucas"])
        assertEquals(30, affinity["lila"])
    }

    @Test
    fun night3_appliesItsWeight() {
        val night3 = content.nights[2]
        val c = QuestionRoundController(content, night3, ProgressRules.newGame(content))
        c.answer("night3q1a1", 1_000)
        assertEquals(42, c.state.value.affinity) // 30 + 10 * 1.2
    }

    // ---------- Carga controlada (banco vacío o dañado) ----------

    @Test
    fun prepare_validContent_isReady() {
        val result = RoundLoader.prepare(content, "night1", null)
        assertTrue(result is RoundLoadResult.Ready)
    }

    @Test
    fun prepare_unknownNight_isControlledError() {
        assertTrue(RoundLoader.prepare(content, "night9", null) is RoundLoadResult.Error)
    }

    @Test
    fun prepare_emptyQuestionBank_isControlledError() {
        val empty = content.copy(nights = content.nights.map { it.copy(questions = emptyList()) })
        val result = RoundLoader.prepare(empty, "night1", null)
        assertTrue(result is RoundLoadResult.Error)
        assertEquals("Esta noche no tiene preguntas.", result.message)
    }

    @Test
    fun prepare_questionWithTooFewAnswers_isControlledError() {
        val broken = content.copy(
            nights = content.nights.map { n ->
                n.copy(questions = n.questions.map { it.copy(answers = it.answers.take(2)) })
            },
        )
        assertTrue(RoundLoader.prepare(broken, "night1", null) is RoundLoadResult.Error)
    }

    @Test
    fun prepare_invalidContent_isControlledError() {
        val invalid = content.copy(
            nights = content.nights.map { it.copy(characterId = "nadie") },
        )
        assertTrue(RoundLoader.prepare(invalid, "night1", null) is RoundLoadResult.Error)
    }
}
