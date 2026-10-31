package com.example.videojuego.round

import com.example.videojuego.data.TestFixtures
import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.data.save.SaveGame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Retomar la ronda tras rotar la pantalla o al continuar una partida guardada. */
class QuestionRoundResumeTest {

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

    private fun saveWithAnswers(count: Int, targetNight: com.example.videojuego.data.content.NightData = night): SaveGame {
        var save = ProgressRules.newGame(content)
        for (i in 0 until count) {
            val q = targetNight.questions[i]
            save = ProgressRules.recordAnswer(content, save, targetNight, q, q.answers[i % 2], 1_000)
        }
        return save
    }

    @Test
    fun controller_startsAtTheFirstUnansweredQuestion() {
        val controller = QuestionRoundController(content, night, saveWithAnswers(2))

        val s = controller.state.value
        assertEquals(2, s.questionIndex)
        assertEquals("night1q3", s.question.id)
        assertFalse(s.finished)
        assertNull(s.feedback)
        assertEquals(45, s.affinity) // 30 + 10 + 5 de las dos respuestas ya dadas
    }

    @Test
    fun controller_withEveryQuestionAnswered_startsFinished() {
        val controller = QuestionRoundController(content, night, saveWithAnswers(3))
        assertTrue(controller.state.value.finished)
    }

    @Test
    fun answersFromAnotherNight_doNotAffectTheResumePoint() {
        val night2 = content.nights[1]
        val controller = QuestionRoundController(content, night, saveWithAnswers(2, night2))
        assertEquals(0, controller.state.value.questionIndex)
    }

    @Test
    fun freshGame_startsAtTheFirstQuestion() {
        val controller = QuestionRoundController(content, night, ProgressRules.newGame(content))
        assertEquals(0, controller.state.value.questionIndex)
    }

    @Test
    fun resumedRound_canContinueAndFinish() {
        val controller = QuestionRoundController(content, night, saveWithAnswers(2))
        controller.answer("night1q3a1", 2_000)
        controller.next()

        val s = controller.state.value
        assertTrue(s.finished)
        assertEquals(3, s.save.answers.size)
    }

    // ---------- Instantánea (lo que se guarda al rotar) ----------

    @Test
    fun snapshot_roundTripKeepsProgressAndRemainingTime() {
        val original = RoundSnapshot(saveWithAnswers(2), remainingMs = 7_500)
        val restored = assertNotNull(RoundSnapshotCodec.decode(RoundSnapshotCodec.encode(original)))
        assertEquals(original, restored)
    }

    @Test
    fun snapshot_withoutRemainingTime_roundTrips() {
        val original = RoundSnapshot(saveWithAnswers(1), remainingMs = null)
        val restored = assertNotNull(RoundSnapshotCodec.decode(RoundSnapshotCodec.encode(original)))
        assertNull(restored.remainingMs)
        assertEquals(original.save, restored.save)
    }

    @Test
    fun snapshot_corruptText_returnsNull() {
        assertNull(RoundSnapshotCodec.decode("{esto no es json"))
        assertNull(RoundSnapshotCodec.decode(""))
    }

    @Test
    fun rotation_flow_restoresQuestionAndTime() {
        // 1) El jugador contesta 1 pregunta y en la 2 le quedan 7.5 s...
        val first = QuestionRoundController(content, night, ProgressRules.newGame(content))
        first.answer("night1q1a1", 3_000)
        first.next()
        val encoded = RoundSnapshotCodec.encode(RoundSnapshot(first.state.value.save, remainingMs = 7_500))

        // 2) ...se rota la pantalla: se crea todo de nuevo desde la instantánea.
        val restored = assertNotNull(RoundSnapshotCodec.decode(encoded))
        val second = QuestionRoundController(content, night, restored.save)

        assertEquals("night1q2", second.state.value.question.id)
        assertEquals(7_500, restored.remainingMs)
        assertEquals(40, second.state.value.affinity)
    }
}
